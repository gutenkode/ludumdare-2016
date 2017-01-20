package scenes;

import entities.Entity;
import map.*;
import mote4.scenegraph.Scene;
import mote4.scenegraph.Window;
import mote4.scenegraph.target.DepthTexture;
import mote4.util.matrix.GenericMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 1/19/17.
 */
public class Editor implements Scene {

    private static MapEditor me;
    private static float r;

    private static boolean lookAt;
    private static float[] lookAtPos, defaultPos, cameraPos;
    private static Entity lookAtEntity;
    private static double lookAtGlow;

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

        defaultPos = new float[] {me.getMapData().width/2, me.getMapData().height/2, 0};
        cameraPos = new float[] {me.getMapData().width/2, me.getMapData().height/2, 0};
    }
    public static void unloadMap() { me = null; }
    public static MapEditor getMapEditor() { return me; }

    public static void lookAt(Entity e) {
        lookAtEntity = e;
        lookAtPos = new float[] {e.posX(), e.posY(), e.tileHeight()};
        lookAt = true;
    }

    @Override
    public void update(double delta) {
        if (me == null)
            return;

        for (Entity e : me.getEntities())
            e.update();

        if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS)
            r -= .02;
        else if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS)
            r += .02;

        lookAtGlow += .1;

        if (lookAt) {
            for (int i = 0; i < 3; i++) {
                cameraPos[i] -= (cameraPos[i]-lookAtPos[i])/15;
            }
        } else {
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
    public void render(double delta) {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0, 0, 0, 0);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        if (me != null)
        {
            // render static map mesh
            ShaderMap.use("ingame_nolight");
            TextureMap.bindUnfiltered("tileset_1");
            Uniform.samplerAndTextureUnfiltered("tex_shade", 2, "tileset_shade");
            //Uniform.samplerAndTextureUnfiltered("tex_bump", 3, "tileset_1_NRM");
            trans.model.setIdentity();
            trans.makeCurrent();
            me.getMapData().render();

            if (me.getEntities() != null && !me.getEntities().isEmpty())
            {
                // render entity tilesheets
                ShaderMap.use("spritesheet_nolight");
                trans.makeCurrent();
                for (Entity e : me.getEntities()) {
                    trans.model.setIdentity();
                    e.render(trans.model);
                }

                // render hitboxes
                ShaderMap.use("color");
                Uniform.varFloat("colorMult", 1, 1, 1, 1);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
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
