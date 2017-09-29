package scenes;

import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
import ui.TitleUIManager;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 12/31/16.
 */
public class TitleUI implements Scene {

    private Transform sceneTrans, quadTrans;

    public TitleUI() {
        quadTrans = new Transform();
        sceneTrans = new Transform();
    }

    @Override
    public void update(double time, double delta) {
        TitleUIManager.update();
    }

    @Override
    public void render(double time, double delta) {
        glDisable(GL_DEPTH_TEST);
        //glClear(GL_COLOR_BUFFER_BIT);

        ShaderMap.use("texture");
        quadTrans.bind();
        TextureMap.bindUnfiltered("ui_title");
        MeshMap.render("quad");

        TitleUIManager.render(sceneTrans);
    }

    @Override
    public void framebufferResized(int width, int height) {
        float ratio = (float)width/height;
        quadTrans.projection.setOrthographic(-ratio,-1,ratio,1, -1,1);

        sceneTrans.projection.setOrthographic(0, 0, width, height, -1, 1);
    }

    @Override
    public void destroy() {

    }
}
