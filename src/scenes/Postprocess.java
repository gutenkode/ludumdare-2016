package scenes;

import java.util.Random;
import mote4.scenegraph.Scene;
import mote4.scenegraph.target.FBO;
import mote4.scenegraph.target.Target;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.ShaderUtils;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
import main.RootLayer;
import main.Vars;

import static mote4.util.shader.ShaderUtils.FRAGMENT;
import static mote4.util.shader.ShaderUtils.VERTEX;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Peter
 */
public class Postprocess implements Scene {
    
    private static Runnable callbackFunction; // function to call when the fadeout is halfway done, e.g. all black
    private static boolean fadeToBlack = false;
    private float colorMult;
    private static Vars.Filter lastFilterType = null;

    private static final int NUM_BLOOM_FBOS = 2;
    private static int width, height;
    private static float dofCoef, dofCoefTarget;
    private static FBO combineScene,
                       hdrScene, dofScene1, dofScene2;
    private static FBO ditherScene;
    private static FBO[][] bloomScene;
    
    public Postprocess() {
        colorMult = 1;
        dofCoef = dofCoefTarget = 0;
        bloomScene = new FBO[NUM_BLOOM_FBOS][2];
    }

    @Override
    public void update(double time, double delta) {
        // update fade-to-black effect:
        // call the specified callback when the fade is complete,
        // then unfade
        if (fadeToBlack) {
            colorMult -= (colorMult*.2) * (delta*60);
            if (colorMult < 0.01) {
                fadeToBlack = false;
                callbackFunction.run();
                System.gc(); // attempt garbage collection while the screen is black
            }
        } else {
            if (colorMult < 1)
                colorMult += (colorMult*.2) * (delta*60);
            else
                colorMult = 1;
        }
        
        //dofCoef -= (dofCoef-dofCoefTarget)/10f; // TODO still framelocked
    }

    @Override
    public void render(double time, double delta) {
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        // render transition effect, if active
        if (RootLayer.getState() == RootLayer.State.BATTLE_INTRO) {
            ShaderMap.use("quad");
            Uniform.vec("colorMult", colorMult,colorMult,colorMult);
            TextureMap.bindFiltered("fbo_transition1");
            MeshMap.render("quad");
            Uniform.vec("colorMult", 1,1,1);
            return;
        }

        Target framebuffer = Target.getCurrent();
        
    // render 3D scene to the dither FBO
    // the 3D scene might be upscaled, this enforces the correct resolution while also applying dithering
        ditherScene.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderMap.use("quad_dither");
        Uniform.vec("screenSize", width, height);
        TextureMap.bindFiltered("fbo_scene");
        MeshMap.render("quad");
        
    // create DOF scene from dithered scene, just a simple blur
        createDOFTexture("fbo_dither");
        
    // render dithered 3D scene and UI to combineScene,
    // which is rendered in the final step and used to create bloom
        combineScene.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderMap.use("quad_combine");
        Uniform.vec("dofCoef", dofCoef);
        TextureMap.bindUnfiltered("fbo_dither");
        MeshMap.render("quad");
        
    // create a bloom texture from the combined scene
        createBloomTexture("fbo_combine");

    // render final mix to screen
        framebuffer.makeCurrent();
        ShaderMap.use("quad_final");

        //Uniform.vec("bloomCoef", .5f);
        Uniform.vec("colorMult", colorMult,colorMult,colorMult); // for fading in/out
        //Uniform.vec("rand", random.nextFloat(), random.nextFloat()); // random position for static

        if (Vars.currentFilter() != Vars.Filter.NEAREST) {
            TextureMap.bindFiltered("fbo_combine");
            //Uniform.samplerAndTextureFiltered("tex_scene", 9, "fbo_dither"); // dithered 3D scene
            //Uniform.samplerAndTextureFiltered("tex_ui", 4, "fbo_ui"); // upscaled UI
        } else {
            TextureMap.bindUnfiltered("fbo_combine");
            //Uniform.samplerAndTextureUnfiltered("tex_scene", 9, "fbo_dither"); // dithered 3D scene
            //Uniform.samplerAndTextureUnfiltered("tex_ui", 4, "fbo_ui"); // upscaled UI
        }
        // all other uniform values are set in initFinalPassShader() since they are static
        MeshMap.render("quad");
    }
    
    /**
     * Creates a bloom texture of the specified texture.
     * The result is stored in the texture "fbo_hdr"
     * @param texName The texture to apply bloom to.
     */
    public void createBloomTexture(String texName) {
        // render scene FBO to HDR scene FBO
        // this cuts off the darker part of the image so only
        // the bright part of the image is blurred for HDR effects
        hdrScene.makeCurrent();
        ShaderMap.use("quad_hdr");
        TextureMap.bindFiltered(texName);
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");

        glBlendFunc(GL_ONE, GL_ONE);

        for (int i = 0; i < bloomScene.length; i++) {
            bloomScene[i][0].makeCurrent();
            ShaderMap.use("quad_horizBlur");
            Uniform.vec("blurSize", 1f/(width/(i+1)));
            //if (i == 0)
                TextureMap.bindFiltered("fbo_hdr");
            //else
            //    TextureMap.bindFiltered("fbo_bloom"+i+"_1");
            glClear(GL_COLOR_BUFFER_BIT);
            MeshMap.render("quad");

            bloomScene[i][1].makeCurrent();
            ShaderMap.use("quad_vertBlur");
            Uniform.vec("blurSize", 1f/(width/(i+1)));
            TextureMap.bindFiltered("fbo_bloom"+(i+1)+"_0");
            glClear(GL_COLOR_BUFFER_BIT);
            MeshMap.render("quad");
        }
        // add all the passes to the hdr buffer for rendering
        hdrScene.makeCurrent();
        //glClear(GL_COLOR_BUFFER_BIT);
        ShaderMap.use("quad");
        for (int i = 0; i < bloomScene.length; i++) {
            TextureMap.bindFiltered("fbo_bloom"+(i+1)+"_1");
            MeshMap.render("quad");
        }
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    /**
     * Creates a blurred texture of the specified texture.
     * Used for the depth-of-field effect.
     * The result is stored in the texture "fbo_dof2"
     * @param texName The texture to blur.
     */
    public void createDOFTexture(String texName) {
        /*
        dofScene2.makeCurrent();
        ShaderMap.use("quad_dofBlur");
        Uniform.samplerAndTextureFiltered("bgl_DepthTexture", 1, "fbo_dofvalue");
        Uniform.vec("bgl_RenderedTextureWidth", width);
        Uniform.vec("bgl_RenderedTextureHeight", height);
        Uniform.vec("focalDepth", 0);
        TextureMap.bindFiltered(texName);
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render3d("quad");
        */

    // FIRST PASS

        dofScene1.makeCurrent();
        ShaderMap.use("quad_horizBlur");
        Uniform.vec("blurSize", 1f/width);
        TextureMap.bindFiltered(texName);
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");
        
        dofScene2.makeCurrent();
        ShaderMap.use("quad_vertBlur");
        Uniform.vec("blurSize", 1f/height);
        TextureMap.bindFiltered("fbo_dof1");
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");
        
    // SECOND PASS

        dofScene1.makeCurrent();
        ShaderMap.use("quad_horizBlur");
        Uniform.vec("blurSize", 1f/width*2);
        TextureMap.bindFiltered("fbo_dof2");
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");
        
        dofScene2.makeCurrent();
        ShaderMap.use("quad_vertBlur");
        Uniform.vec("blurSize", 1f/height*2);
        TextureMap.bindFiltered("fbo_dof1");
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");

    }

    @Override
    public void framebufferResized(int width, int height) {}
    
    /**
     * This method is called with the width/height of the internal resolution.
     * framebufferResized is called with the width/height of the window.
     * @param width
     * @param height 
     */
    public static void resizeBuffers(int width, int height) {
        Postprocess.width = width;
        Postprocess.height = height;
        
        if (combineScene != null)
            combineScene.destroy();
        combineScene = new FBO(width,height,false,false,null);
        TextureMap.delete("fbo_combine");
        combineScene.addToTextureMap("fbo_combine");

        if (ditherScene != null)
            ditherScene.destroy();
        ditherScene = new FBO(width,height,false,false,null);
        TextureMap.delete("fbo_dither");
        ditherScene.addToTextureMap("fbo_dither");

        if (hdrScene != null)
            hdrScene.destroy();
        hdrScene = new FBO(width,height,false,false,null);
        TextureMap.delete("fbo_hdr");
        hdrScene.addToTextureMap("fbo_hdr");
        
        for (int i = 0; i < bloomScene.length; i++) {
            if (bloomScene[i][0] != null)
                bloomScene[i][0].destroy();
            if (bloomScene[i][1] != null)
                bloomScene[i][1].destroy();
            bloomScene[i][0] = new FBO(width/(i+1),height/(i+1),false,false,null);
            bloomScene[i][1] = new FBO(width/(i+1),height/(i+1),false,false,null);
            TextureMap.delete("fbo_bloom"+(i+1)+"_0");
            TextureMap.delete("fbo_bloom"+(i+1)+"_1");
            bloomScene[i][0].addToTextureMap("fbo_bloom"+(i+1)+"_0");
            bloomScene[i][1].addToTextureMap("fbo_bloom"+(i+1)+"_1");
        }

        if (dofScene1 != null)
            dofScene1.destroy();
        dofScene1 = new FBO(width,height,false,false,null);
        TextureMap.delete("fbo_dof1");
        dofScene1.addToTextureMap("fbo_dof1");
        
        if (dofScene2 != null)
            dofScene2.destroy();
        dofScene2 = new FBO(width,height,false,false,null);
        TextureMap.delete("fbo_dof2");
        dofScene2.addToTextureMap("fbo_dof2");

        initFinalPassShader();
    }

    /**
     * Sets all non-changing uniform values for the final pass shader.
     */
    public static void initFinalPassShader() {
        // if the shader hasn't been compiled yet or the current filter doesn't match the compiled one,
        // delete and recompile the final pass shader
        if (ShaderMap.get("quad_final") == -1 || Vars.currentFilter() != lastFilterType) {
            lastFilterType = Vars.currentFilter();
            ShaderMap.delete("quad_final");
            String vertSrc = ShaderUtils.loadSource("quad.vert");
            String fragSrc = ShaderUtils.loadSource("quad_final.frag");
            if (lastFilterType != Vars.Filter.CRT)
                fragSrc = fragSrc.replace("#define CRT", "");
            if (lastFilterType != Vars.Filter.QUILEZ)
                fragSrc = fragSrc.replace("#define QUILEZ", "");
            int vertShader = ShaderUtils.compileShaderFromSource(vertSrc, VERTEX);
            int fragShader = ShaderUtils.compileShaderFromSource(fragSrc, FRAGMENT);
            ShaderUtils.addProgram(new int[]{vertShader, fragShader}, "quad_final");
        }

        ShaderMap.use("quad_final");
        Uniform.sampler("tex_bloom", 4, "fbo_hdr", true); // bloom scene
        Uniform.vec("bloomCoef",0.5f);
        if (lastFilterType == Vars.Filter.CRT) {
            Uniform.sampler("tex_vignette", 8, "post_vignette", true); // postprocess
            Uniform.sampler("tex_scanlines", 9, "post_scanlines", true);
        }
        Uniform.vec("texSize", width,height);

        ShaderMap.use("quad_combine");
        Uniform.sampler("tex_ui", 5, "fbo_ui", false); // upscaled UI
        Uniform.sampler("tex_post_values", 6, "tex_post_values", false); // scene blur mix data
        Uniform.sampler("tex_dof", 7, "fbo_dof2", true); // scene blur

        // also initialize the HDR calculation shader
        ShaderMap.use("quad_hdr");
        Uniform.sampler("tex_bloomvalue", 6, "tex_post_values", false); // scene blur mix data
    }

    @Override
    public void destroy() {}
    
    /**
     * Global weight to the depth-of-field interpolation.
     * @param val 1 forces full blur, -1 forces no blur, 0 is default.
     */
    public static void setDOFCoef(float val) {
        dofCoefTarget = val;
    }
    public static void fadeOut(Runnable function) {
        fadeToBlack = true;
        callbackFunction = function;
    }
}
