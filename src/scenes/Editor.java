package scenes;

import entities.Entity;
import map.*;
import mote4.scenegraph.Scene;
import mote4.scenegraph.Window;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.MeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.MeshMap;
import main.Vars;
import main.Input;
import org.lwjgl.glfw.GLFW;
import ui.EditorUIManager;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 1/19/17.
 */
public class Editor implements Scene {

    private static final Mesh cursor;
    private static float cursorRot;
    private static int xPos, yPos, zPos;
    private static boolean keyPressed = true;

    private static MapEditor me;
    private static float r;

    private static boolean lookAt;
    private static float[] lookAtPos, defaultPos, cameraPos;
    private static Entity lookAtEntity;
    private static double lookAtGlow;

    static {
        // cursor mesh
        MeshBuilder mb = new MeshBuilder(3);
        // sides
        mb.vertices(0,0,0);
        mb.vertices(1,0,1);
        mb.vertices(0,.5f,1);

        mb.vertices(0,0,0);
        mb.vertices(0,-.5f,1);
        mb.vertices(1,0,1);

        mb.vertices(0,0,0);
        mb.vertices(0,.5f,1);
        mb.vertices(-1,0,1);

        mb.vertices(0,0,0);
        mb.vertices(-1,0,1);
        mb.vertices(0,-.5f,1);

        // top
        mb.vertices(0,-.5f,1);
        mb.vertices(-1,0,1);
        mb.vertices(0,.5f,1);

        mb.vertices(0,-.5f,1);
        mb.vertices(0,.5f,1);
        mb.vertices(1,0,1);
        cursor = mb.constructVAO(GL_TRIANGLES);
    }

    private Transform trans;

    public Editor() {
        trans = new Transform();
    }

    public static void loadMap(String mapName) {
        MapData md = MapLoader.getMap(mapName);
        me = new MapEditor(md);

        r = (float)Math.PI;
        lookAt = false;
        lookAtEntity = null;

        xPos = me.getMapData().width/2;
        yPos = me.getMapData().height/2;
        zPos = me.getMapData().heightData[xPos][yPos];
        cameraPos = new float[] {me.getMapData().width/2, me.getMapData().height/2, 0};

        EditorUIManager.logMessage("Loaded map: "+mapName);
    }
    public static void unloadMap() { me = null; }
    public static MapEditor getMapEditor() { return me; }

    /**
     * The texture indices for the current tile, used by EditorUI
     * to preview the tilemap.
     * @return
     */
    public static int[] currentTileTexInds() {
        int[] ans = new int[] {0,0};
        if (me == null) return ans;
        ans[0] = me.getMapData().tileData[xPos][yPos][0];
        ans[1] = me.getMapData().tileData[xPos][yPos][2];
        return ans;
    }

    public static void lookAt(Entity e) {
        if (e == null) {
            lookAt = false;
            lookAtEntity = null;
        } else {
            lookAtEntity = e;
            lookAtPos = new float[]{e.posX(), e.posY(), e.tileHeight()};
            lookAt = true;
        }
    }

    @Override
    public void update(double time, double delta) {
        if (me == null)
            return;

        for (Entity e : me.getEntities())
            e.update();

        if (Input.currentLock() != Input.Lock.TERMINAL)
        {
            // rotate camera, can rotate scene while in a menu
            if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS)
                r -= .02;
            else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS)
                r += .02;
        }
        if (Input.currentLock() != Input.Lock.TERMINAL &&
            Input.currentLock() != Input.Lock.MENU)
        {
            // cursor movement
            if (Input.isKeyNew(Input.Keys.LEFT)) {
                if (xPos > 0)
                    xPos--;
            } else if (Input.isKeyNew(Input.Keys.RIGHT)) {
                if (xPos < me.getMapData().width-1)
                    xPos++;
            }
            if (Input.isKeyNew(Input.Keys.UP)) {
                if (yPos > 0)
                    yPos--;
            } else if (Input.isKeyNew(Input.Keys.DOWN)) {
                if (yPos < me.getMapData().height-1)
                    yPos++;
            }
            zPos = me.getMapData().heightData[xPos][yPos];

            // tile height
            if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_EQUAL) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.editHeight(xPos,yPos,1);
                keyPressed = true;
            } else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_MINUS) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.editHeight(xPos,yPos,-1);
                keyPressed = true;
            }
            // tile shape
            else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_LEFT_BRACKET) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.toggleTileShapeBit(xPos,yPos,0);
                keyPressed = true;
            } else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_RIGHT_BRACKET) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.toggleTileShapeBit(xPos,yPos,1);
                keyPressed = true;
            } else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_BACKSLASH) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.toggleTileShapeBit(xPos,yPos,2);
                keyPressed = true;
            }
            // texture coords
            else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_J) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.editTileInd1(xPos,yPos,-1);
                keyPressed = true;
            } else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_L) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.editTileInd1(xPos,yPos,1);
                keyPressed = true;
            } else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_I) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.editTileInd1(xPos,yPos,-(int) Vars.TILESHEET_X);
                keyPressed = true;
            } else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_K) == GLFW.GLFW_PRESS) {
                if (!keyPressed)
                    me.editTileInd1(xPos,yPos,(int) Vars.TILESHEET_Y);
                keyPressed = true;
            } else
                keyPressed = false;
        }

        lookAtGlow += .1;
        cursorRot += .05;

        if (lookAt) {
            for (int i = 0; i < 3; i++) {
                cameraPos[i] -= (cameraPos[i]-lookAtPos[i])/15;
            }
        } else {
            defaultPos = new float[] {xPos+.5f,yPos+.5f,zPos};

            for (int i = 0; i < 3; i++) {
                cameraPos[i] -= (cameraPos[i]-defaultPos[i])/15;
            }
        }

        // normal camera view
        trans.view.setIdentity();
        trans.view.scale(-1,1,1);
        trans.view.translate(0, 0, -5); // pull camera back
        trans.view.rotate(-(float) Math.PI / 4, 1, 0, 0); // angle down, otherwise view is top-down

        // translate to correct position
        trans.view.rotate(r, 0, 0, 1);
        trans.view.translate(-cameraPos[0], -cameraPos[1], -cameraPos[2]);
    }

    @Override
    public void render(double time, double delta) {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0, 0, 0, 0);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        if (me != null)
        {
            // render3d static map mesh
            ShaderMap.use("ingame_nolight");
            TextureMap.bindUnfiltered("tileset_1");
            Uniform.samplerAndTextureUnfiltered("tex_shade", 2, "tileset_shade");
            //Uniform.samplerAndTextureUnfiltered("tex_bump", 3, "tileset_1_NRM");
            trans.model.setIdentity();
            trans.makeCurrent();
            me.getMapData().render();

            // render3d cursor, BEFORE entities
            ShaderMap.use("color");
            float cval = ((float)Math.cos(cursorRot)+1)/4+.25f;
            Uniform.varFloat("colorMult", cval, 0, 0, cval);
            trans.model.setIdentity();
            trans.model.translate(xPos+.5f,yPos+.5f,zPos+.05f);
            //trans.model.rotate(cursorRot,0,0,1);
            trans.model.scale(.5f,.5f,1);
            trans.makeCurrent();
            MeshMap.render("quad");

            if (me.getEntities() != null && !me.getEntities().isEmpty())
            {
                // render3d entity tilesheets
                ShaderMap.use("spritesheet_nolight");
                trans.makeCurrent();
                for (Entity e : me.getEntities()) {
                    trans.model.setIdentity();
                    e.render(trans.model);
                }

                // render3d hitboxes
                ShaderMap.use("color");
                Uniform.varFloat("colorMult", 1, 1, 1, 1);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                glEnable(GL_CULL_FACE);
                trans.makeCurrent();
                trans.model.setIdentity();
                for (Entity e : me.getEntities()) {
                    trans.model.setIdentity();
                    trans.model.makeCurrent();
                    if (e == lookAtEntity) {
                        Uniform.varFloat("colorMult", 1, (float) Math.sin(lookAtGlow), (float) Math.sin(lookAtGlow), 1);
                        e.renderHitbox(trans.model);
                        Uniform.varFloat("colorMult", 1, 1, 1, 1);
                    } else
                        e.renderHitbox(trans.model);
                }
                glDisable(GL_CULL_FACE);
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            }
        }
    }

    @Override
    public void framebufferResized(int width, int height) {
        trans.projection.setPerspective(width, height, .5f, 50f, 65);
    }

    @Override
    public void destroy() {

    }
}
