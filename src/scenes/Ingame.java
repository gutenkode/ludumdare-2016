package scenes;

import map.MapLevelManager;
import map.MapManager;
import mote4.scenegraph.Scene;
import mote4.scenegraph.target.DepthTexture;
import mote4.scenegraph.target.Target;
import mote4.util.shader.Uniform;
import mote4.util.matrix.CubeMapMatrix;
import mote4.util.matrix.GenericMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import rpgbattle.BattleManager;

/**
 * Renders the 3D overworld scene.
 * @author Peter
 */
public class Ingame implements Scene {
    
    private static final DepthTexture depthTexture;

    private static boolean firstPerson = false;
    
    static {
        depthTexture = new DepthTexture(1024,1024);
        depthTexture.addToTextureMap("fbo_depth");
    }
    
    private Transform trans;
    private final GenericMatrix shadowProj;
    private int playerRestoreStaminaDelay;
    private float[] flashlightDir = new float[2];
    
    public Ingame() {
        trans = new Transform();
        shadowProj = new GenericMatrix("depthProj");
    }

    @Override
    public void update(double delta) {
        // update entities
        MapManager.update();
        
        // restore stamina
        if (playerRestoreStaminaDelay <= 0) {
            playerRestoreStaminaDelay = 5;
            BattleManager.getPlayer().restoreStamina(1);
        } else
            playerRestoreStaminaDelay--;

        if (firstPerson) {
            double[] r = MapManager.getPlayer().cameraRot();
            flashlightDir[0] = (float)r[0];
            flashlightDir[1] = (float)r[1];

            trans.view.setIdentity();
            trans.view.rotate(-(float) Math.PI/2, 1, 0, 0); // angle up to be level with horizon
            trans.view.rotate((float)r[1],1,0,0);
            trans.view.rotate(-(float)(r[0]+Math.PI),0,0,1);

            // move to player
            trans.view.translate(-MapManager.getPlayer().posX(), MapManager.getPlayer().posY(), -MapManager.getPlayer().elevatorHeight()-1.2f);
            trans.view.scale(1, -1, 1);
        } else {
            flashlightDir[1] = (float)Math.PI/2;
            flashlightDir[0] = MapManager.getPlayer().facingDirection();
            flashlightDir[0] /= 8;
            flashlightDir[0] *= Math.PI*2;

            // normal camera view
            trans.view.setIdentity();
            trans.view.translate(0, 0, -5); // pull camera back
            trans.view.rotate(-(float) Math.PI / 4, 1, 0, 0); // angle down, otherwise view is top-down

            // dynamic angle changing based on the players location in the current map
            int[] mapSize = MapManager.currentMapSize();
            float xAngle = MapManager.getPlayer().posX() / mapSize[0] - .5f;
            float yAngle = MapManager.getPlayer().posY() / mapSize[1] - .5f;
            trans.view.rotate(yAngle * .2f, 1, 0, 0);
            trans.view.rotate(xAngle * .4f, 0, 0, 1);

            // move to player
            trans.view.translate(-MapManager.getPlayer().posX(), MapManager.getPlayer().posY(), -MapManager.getPlayer().elevatorHeight());
            trans.view.scale(1, -1, 1);
        }
        // shadow map camera view
        shadowProj.setIdentity();
        
        //shadowProj.setOrthographic(left, top, right, bottom, near, far);
        //shadowProj.setOrthographic(-1, -1, 1, 1, 0, 10);
        //shadowProj.rotate(-(float)Math.PI/2+.3f, 1, 0, 0); // face forward, not down
        
        shadowProj.setPerspective(1, 1, .1f, 10, 140); // FOV is in degrees
        shadowProj.scale(-1, 1, 1);
        shadowProj.rotate(-(float)Math.PI/2, 1, 0, 0); // face forward, not down

        //shadowProj.translate(.5f,0,-.25f); // offset for first person view
        shadowProj.rotate(flashlightDir[0], 0, 0, 1); // rotate to match flashlight
        shadowProj.translate(-MapManager.getPlayer().posX(), -MapManager.getPlayer().posY(), -MapManager.getPlayer().elevatorHeight()-1.1f);
        
    }

    @Override
    public void render(double delta) {
        glEnable(GL_DEPTH_TEST);
        Target t = Target.getCurrent();
    
    // render depth data from camera perspective
        depthTexture.makeCurrent();
        glClearColor(1, 1, 1, 1); // for depth buffer values
        glClear(GL_DEPTH_BUFFER_BIT);
        MapManager.renderForShadow(trans.model, shadowProj);
        
    // normal scene, using contents of depth texture for shadow rendering
        t.makeCurrent();
        glClearColor(0, 0, 0, 0); // for regular rendering
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        MapManager.render(trans, shadowProj, flashlightDir);
    }
    
    @Override
    public void framebufferResized(int width, int height) {
        trans.projection.setPerspective(width, height, .5f, 50f, 65);
    }

    @Override
    public void destroy() {
        
    }
    
}