package scenes;

import java.util.Random;
import mote4.scenegraph.Scene;
import mote4.scenegraph.target.FBO;
import mote4.scenegraph.target.Target;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
import main.RootLayer;
import main.Vars;

import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Peter
 */
public class Postprocess implements Scene {
    
    private static Runnable callbackFunction; // function to call when the fadeout is halfway done
    private static boolean fadeToBlack = false;
    private float colorMult;

    private static final int NUM_BLOOM_FBOS = 6;
    //private Random random;
    private static int width, height;
    private static float dofCoef, dofCoefTarget;
    private static FBO combineScene, uiUpscaleScene,
                       hdrScene, dofScene1, dofScene2;
    private static FBO[] ditherScene;
    private static FBO[][] bloomScene;
    
    public Postprocess() {
        //random = new Random();
        colorMult = 1;
        dofCoef = dofCoefTarget = 0;
        bloomScene = new FBO[NUM_BLOOM_FBOS][2];
        ditherScene = new FBO[2];
    }

    @Override
    public void update(double time, double delta) {
        // update fade-to-black effect
        // call the specified callback when the fade is complete
        // then unfade
        if (fadeToBlack) {
            colorMult -= (colorMult*.2) * (delta*60);
            if (colorMult < 0.01) {
                fadeToBlack = false;
                callbackFunction.run();
                System.gc(); // attempt to garbage collect after every fade operation
            }
        } else {
            if (colorMult < 1)
                colorMult += (colorMult*.2) * (delta*60);
            else
                colorMult = 1;
        }
        
        dofCoef -= (dofCoef-dofCoefTarget)/10f;
    }

    @Override
    public void render(double time, double delta) {
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        // render3d transition effect, if active
        if (RootLayer.getState() == RootLayer.State.BATTLE_INTRO) {
            ShaderMap.use("quad");
            Uniform.varFloat("colorMult", colorMult,colorMult,colorMult);
            TextureMap.bindFiltered("fbo_transition1");
            MeshMap.render("quad");
            Uniform.varFloat("colorMult", 1,1,1);
            return;
        }

        Target framebuffer = Target.getCurrent();
        
    // render3d 3D scene to dither FBO
    // this is used to create the combined FBO and used as the final pass of the 3D scene
        ditherScene[0].makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderMap.use("quad_dither");
        Uniform.varFloat("screenSize", width, height);
        TextureMap.bindFiltered("fbo_scene");
        MeshMap.render("quad");
        
    // render3d UI scene to UI upscale scene - simple upscale to improve filtering effects
    // same with dither scene
        uiUpscaleScene.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        ShaderMap.use("quad");
        TextureMap.bindUnfiltered("fbo_ui");
        MeshMap.render("quad");

        ditherScene[1].makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        TextureMap.bindUnfiltered("fbo_dither0");
        MeshMap.render("quad");

    // create DOF scene from dithered scene, just a simple blur
        //if (dofCoef > 0)
            createDOFTexture("fbo_dither0");
        
    // render3d scene and UI to the combineScene,
    // which is used to create the bloom scene
        combineScene.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderMap.use("quad");
        TextureMap.bindFiltered("fbo_dither1");
        MeshMap.render("quad");
        TextureMap.bindFiltered("fbo_ui");
        MeshMap.render("quad");
        
    // create a bloom texture from the combined scene
        createBloomTexture("fbo_combine");

    // render3d final mix shader to screen
        framebuffer.makeCurrent();
        ShaderMap.use("quad_final");
        Uniform.varFloat("dofCoef", dofCoef);

        //Uniform.varFloat("bloomCoef", .5f);
        Uniform.varFloat("colorMult", colorMult,colorMult,colorMult); // for fading in/out
        //Uniform.varFloat("rand", random.nextFloat(), random.nextFloat()); // random position for static


        if (Vars.useFiltering())
            TextureMap.bindFiltered("fbo_dither1"); // upscaled/dithered scene, binds to "tex_scene"
        else
            TextureMap.bindUnfiltered("fbo_dither1");

        // all other uniform values are set in initFinalPassShader();
        MeshMap.render("quad");
    }
    
    /**
     * Creates a bloom texture of the specified texture.
     * The result is stored in the texture "fbo_hdr"
     * @param texName The texture to apply bloom to.
     */
    public void createBloomTexture(String texName) {
        // render3d scene FBO to HDR scene FBO
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
            Uniform.varFloat("blurSize", 1f/(width/(i+1)));
            //if (i == 0)
                TextureMap.bindFiltered("fbo_hdr");
            //else
            //    TextureMap.bindFiltered("fbo_bloom"+i+"_1");
            glClear(GL_COLOR_BUFFER_BIT);
            MeshMap.render("quad");

            bloomScene[i][1].makeCurrent();
            ShaderMap.use("quad_vertBlur");
            Uniform.varFloat("blurSize", 1f/(width/(i+1)));
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
        Uniform.varFloat("bgl_RenderedTextureWidth", width);
        Uniform.varFloat("bgl_RenderedTextureHeight", height);
        Uniform.varFloat("focalDepth", 0);
        TextureMap.bindFiltered(texName);
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render3d("quad");
        */

    // FIRST PASS

        dofScene1.makeCurrent();
        ShaderMap.use("quad_horizBlur");
        Uniform.varFloat("blurSize", 1f/width);
        TextureMap.bindFiltered(texName);
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");
        
        dofScene2.makeCurrent();
        ShaderMap.use("quad_vertBlur");
        Uniform.varFloat("blurSize", 1f/height);
        TextureMap.bindFiltered("fbo_dof1");
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");
        
    // SECOND PASS

        dofScene1.makeCurrent();
        ShaderMap.use("quad_horizBlur");
        Uniform.varFloat("blurSize", 1f/width*2);
        TextureMap.bindFiltered("fbo_dof2");
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");
        
        dofScene2.makeCurrent();
        ShaderMap.use("quad_vertBlur");
        Uniform.varFloat("blurSize", 1f/height*2);
        TextureMap.bindFiltered("fbo_dof1");
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");

    }

    @Override
    public void framebufferResized(int width, int height) {}
    
    /**
     * This method is called with the width/height of the internal resolution.
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

        // ditherScene1 and uiUpscaleScene are double the resolution of the scene,
        // to improve texture filtering effects

        if (ditherScene[0] != null)
            ditherScene[0].destroy();
        ditherScene[0] = new FBO(width,height,false,false,null);
        TextureMap.delete("fbo_dither0");
        ditherScene[0].addToTextureMap("fbo_dither0");
        if (ditherScene[1] != null)
            ditherScene[1].destroy();
        ditherScene[1] = new FBO(width*2,height*2,false,false,null);
        TextureMap.delete("fbo_dither1");
        ditherScene[1].addToTextureMap("fbo_dither1");
        
        if (uiUpscaleScene != null)
            uiUpscaleScene.destroy();
        uiUpscaleScene = new FBO(width*2,height*2,false,false,null);
        TextureMap.delete("fbo_ui_upscale");
        uiUpscaleScene.addToTextureMap("fbo_ui_upscale");

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
        
        ShaderMap.use("quad_final");
        Uniform.varFloat("aspectRatio", (float)width/height);
        
        initFinalPassShader();
    }

    /**
     * Sets all non-changing uniform values for the final pass shader.
     */
    public static void initFinalPassShader() {
        ShaderMap.use("quad_final");

        if (Vars.useFiltering()) {
            //Uniform.samplerAndTextureFiltered("tex_scene", 9, "fbo_dither1"); // dithered 3D scene
            Uniform.samplerAndTextureFiltered("tex_ui", 4, "fbo_ui_upscale"); // upscaled UI
        } else {
            //Uniform.samplerAndTextureUnfiltered("tex_scene", 9, "fbo_dither1"); // dithered 3D scene
            Uniform.samplerAndTextureUnfiltered("tex_ui", 4, "fbo_ui_upscale"); // upscaled UI
        }
        Uniform.samplerAndTextureFiltered("tex_bloom", 5, "fbo_hdr"); // bloom scene
        Uniform.samplerAndTextureFiltered("tex_dof", 6, "fbo_dof2"); // scene blur
        Uniform.samplerAndTextureUnfiltered("tex_dofvalue", 7, "fbo_dofvalue"); // scene blur mix data
        //Uniform.samplerAndTextureFiltered("tex_noise", -1, "post_noise"); // postprocess
        Uniform.samplerAndTextureFiltered("tex_vignette", 8, "post_vignette"); // postprocess
        //Uniform.samplerAndTextureFiltered("tex_scanlines", -1, "post_scanlines");
    }

    @Override
    public void destroy() {
    }
    
    /**
     * Global weight to the depth-of-field interpolation.
     * @param val 1 forces full blur, -1 forces no blur, 0 is default.
     */
    public static void setDOFCoef(float val) {
        dofCoefTarget = val;
    }
    public static void fadeOut(Runnable function) {
        // I really hope I know what I'm doing...
        fadeToBlack = true;
        callbackFunction = function;
    }
}
