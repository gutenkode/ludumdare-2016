package scenes;

import mote4.scenegraph.Scene;
import mote4.scenegraph.target.FBO;
import mote4.util.matrix.ModelMatrix;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
import main.RootLayer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renders the effect for transitioning to a battle.
 * @author Peter
 */
public class BattleTransition implements Scene {
    
    private FBO fbo1, fbo2;
    private double delay;
    private ModelMatrix model;
    private static boolean bufferSwitch;
    
    public BattleTransition() {
        delay = 135/60.0;
        
        fbo1 = new FBO(512,512,false,false,null);
        TextureMap.delete("fbo_transition1");
        fbo1.addToTextureMap("fbo_transition1");
        
        fbo2 = new FBO(512,512,false,false,null);
        TextureMap.delete("fbo_transition2");
        fbo2.addToTextureMap("fbo_transition2");
        
        model = new ModelMatrix();
        
        bufferSwitch = true;
        
        fbo2.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        fbo1.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        ShaderMap.use("quad");
        TextureMap.bindFiltered("fbo_combine");
        MeshMap.render("quad");
    }

    @Override
    public void update(double time, double delta) {
        //model.setIdentity();
        model.scale(1.001f, 1.001f, 1);
        //model.translate(.0001f, .0001f);
        model.rotate(.0003f, 0, 0, 1);
        
        //model.scale(1.04f, 1.045f, 1);
        //model.rotate(.02f, 0, 0, 1);
        delay -= delta;
        if (delay <= 0)
            Postprocess.fadeOut(RootLayer::startBattle);
    }

    @Override
    public void render(double time, double delta) {
        glDisable(GL_DEPTH_TEST);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        //glBlendFunc(GL_ONE, GL_ONE);
        
        if (bufferSwitch) {
            fbo2.makeCurrent();
            TextureMap.bindUnfiltered("fbo_transition1");
            ShaderMap.use("quad_transition");
        } else {
            fbo1.makeCurrent();
            TextureMap.bindUnfiltered("fbo_transition2");
            ShaderMap.use("quad");
        }
        //glClear(GL_COLOR_BUFFER_BIT);
        model.bind();
        MeshMap.render("quad");
        
        bufferSwitch = !bufferSwitch;
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void framebufferResized(int width, int height) {}

    @Override
    public void destroy() {
        fbo1.destroy();
        fbo2.destroy();
    }
    
}
