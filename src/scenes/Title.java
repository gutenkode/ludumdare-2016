package scenes;

import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 12/30/16.
 */
public class Title implements Scene {

    private Transform trans;

    public Title() {
        trans = new Transform();
    }

    @Override
    public void update(double time, double delta) {
        trans.model.rotate((float)delta*.05f, 0,0,1);
        trans.model.rotate((float)delta*.21f, 0,1,0);
        trans.model.rotate((float)delta*.12f, 1,0,0);
    }

    @Override
    public void render(double time, double delta) {
        glClearColor(0, 0, 0, 0);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        glDisable(GL_DEPTH_TEST);
        ShaderMap.use("quad_titlebg");
        Uniform.vec("offset",(float)(time*.0125));
        TextureMap.bindFiltered("ui_titlebg");
        MeshMap.render("quad");

        glEnable(GL_DEPTH_TEST);
        ShaderMap.use("titlebg");
        Uniform.vec("colorAdd", .1f,.2f,.5f,1);
        Uniform.vec("colorMult", .5f,.5f,.7f,1);
        trans.bind();
        MeshMap.render("hexahedron");
    }

    @Override
    public void framebufferResized(int width, int height) {
        float ratio = (float)width/height;

        trans.projection.setPerspective(width, height, .5f, 6f, 65);

        trans.view.setIdentity();
        trans.view.translate(0,0,-2); // pull camera back
        trans.view.translate(ratio-1f,-.5f,0); // off center

        ShaderMap.use("quad_titlebg");
        Uniform.vec("ratio",height/(float)width);
    }

    @Override
    public void destroy() {}
}
