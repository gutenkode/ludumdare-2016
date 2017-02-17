package scenes;

import map.MapManager;
import mote4.scenegraph.Scene;
import mote4.scenegraph.target.DepthBuffer;
import mote4.scenegraph.target.DepthCubeBuffer;
import mote4.scenegraph.target.Target;
import mote4.util.matrix.CubeMapMatrix;
import mote4.util.matrix.GenericMatrix;
import mote4.util.matrix.Transform;
import mote4.util.matrix.ViewMatrix;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renders the 3D overworld scene.
 * @author Peter
 */
public class Ingame implements Scene {
    
    private static final DepthCubeBuffer depthTexture;

    public static final boolean firstPerson = false;
    
    static {
        depthTexture = new DepthCubeBuffer(1024);
        depthTexture.addToTextureMap("fbo_depth");
        MapManager.initShaders();
    }
    
    private Transform trans;
    private final ViewMatrix shadowView;
    private final CubeMapMatrix shadowProj; // defines the shadow camera, combines projection with view and model transforms
    private float[] flashlightDir = new float[2];

    public Ingame() {
        trans = new Transform();
        shadowView = new ViewMatrix();
        float shadowNearPlane = .1f;
        float shadowFarPlane = 10;
        shadowProj = new CubeMapMatrix("depthProj",shadowNearPlane,shadowFarPlane);
        ShaderMap.use("ingame_map");
        Uniform.varFloat("shadowNearPlane",shadowNearPlane);
        Uniform.varFloat("shadowFarPlane",shadowFarPlane);
        ShaderMap.use("spritesheet_light");
        Uniform.varFloat("shadowNearPlane",shadowNearPlane);
        Uniform.varFloat("shadowFarPlane",shadowFarPlane);
    }

    @Override
    public void update(double delta) {
        // update entities
        MapManager.update();

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
            trans.view.rotate(-(float) Math.PI / 3.5f, 1, 0, 0); // angle down, otherwise view is top-down

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
        /*
        shadowProj.pop();
        shadowProj.push();
        //shadowProj.translate(.5f,0,-.25f); // offset for first person view
        shadowProj.rotate(flashlightDir[0], 0, 0, 1); // rotate to match flashlight
        // translate to player position
        shadowProj.translate(-MapManager.getPlayer().posX(),
                             -MapManager.getPlayer().posY(),
                             -MapManager.getPlayer().elevatorHeight()-1.1f);
        */
        shadowView.setIdentity();
        shadowView.translate(-MapManager.getPlayer().posX(),
                -MapManager.getPlayer().posY()-MapManager.getPlayer().hitboxH(),
                -MapManager.getPlayer().elevatorHeight()-1f);
    }

    @Override
    public void render(double delta) {
        glEnable(GL_DEPTH_TEST);
        Target t = Target.getCurrent();
    
    // render depth data from camera perspective
        depthTexture.makeCurrent();
        //glClearColor(1,1,1,1); // for depth buffer values
        glClear(GL_DEPTH_BUFFER_BIT);
        MapManager.renderForShadow(shadowProj, shadowView);
        
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