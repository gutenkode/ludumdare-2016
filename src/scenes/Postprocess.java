package scenes;

import java.util.Random;
import mote4.scenegraph.Scene;
import mote4.scenegraph.target.FBO;
import mote4.scenegraph.target.Target;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
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
    private Random random;
    private static boolean finalPassShaderInitialized = false;
    private static int width, height;
    private static float dofCoef, dofCoefTarget;
    private static FBO combineScene, ditherScene, uiUpscaleScene,
                       hdrScene, dofScene1, dofScene2;
    private static FBO[][] bloomScene;
    
    public Postprocess() {
        random = new Random();
        colorMult = 1;
        dofCoef = dofCoefTarget = 0;
        bloomScene = new FBO[NUM_BLOOM_FBOS][2];
    }

    @Override
    public void update(double delta) {
        if (fadeToBlack) {
            colorMult *= 0.8;
            if (colorMult < 0.01) {
                fadeToBlack = false;
                callbackFunction.run();
            }
        } else {
            if (colorMult < 1)
                colorMult *= 1.2;
            else
                colorMult = 1;
        }
        
        dofCoef -= (dofCoef-dofCoefTarget)/10f;
    }

    @Override
    public void render(double delta) {
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        Target framebuffer = Target.getCurrent();
        
    // render 3D scene to dither FBO
    // this is used to create the combined FBO and used as the final pass of the 3D scene
        ditherScene.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderMap.use("quad_dither");
        Uniform.varFloat("screenSize", width, height);
        TextureMap.bindFiltered("fbo_scene");
        MeshMap.render("quad");
        
    // render UI scene to UI upscale scene - simple upscale to improve filtering effects
        uiUpscaleScene.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderMap.use("quad");
        TextureMap.bindUnfiltered("fbo_ui");
        MeshMap.render("quad");

    // create DOF scene from dithered scene
        createDOFTexture("fbo_dither");
        
    // render DOF scene and UI to the combined FBO
    // used to create the bloom scene
        combineScene.makeCurrent();
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderMap.use("quad_dither");
        Uniform.varFloat("screenSize", width, height);
        TextureMap.bindFiltered("fbo_dof2");
        MeshMap.render("quad");
        
        ShaderMap.use("quad");
        TextureMap.bindFiltered("fbo_ui");
        MeshMap.render("quad");
        
    // create a bloom texture from the combined scene
        createBloomTexture("fbo_combine");
        //createBloomTexture("fbo_dof2");
        
   // render final mix shader to screen
        framebuffer.makeCurrent();
        ShaderMap.use("quad_final");
        
        Uniform.varFloat("dofCoef", dofCoef);
        //Uniform.varFloat("bloomCoef", .5f);
        Uniform.varFloat("colorMult", colorMult,colorMult,colorMult);
        Uniform.varFloat("rand", random.nextFloat(), random.nextFloat());
        
        // TEMPORARY
        //Uniform.samplerAndTextureFiltered("tex_scene", 1, "fbo_dither");
        Uniform.samplerAndTextureFiltered("tex_ui", 2, "fbo_ui_upscale");
        Uniform.samplerAndTextureFiltered("tex_bloom", 3, "fbo_hdr");
        Uniform.samplerAndTextureFiltered("tex_dof", 4, "fbo_dof2");
        //Uniform.samplerAndTextureFiltered("tex_dofvalue", 5, "fbo_dofvalue");
        Uniform.samplerAndTextureFiltered("tex_noise", 6, "post_noise");
        Uniform.samplerAndTextureFiltered("tex_vignette", 7, "post_vignette");
        //Uniform.samplerAndTextureFiltered("tex_scanlines", 8, "post_scanlines");

        TextureMap.bindFiltered("fbo_combine");
        MeshMap.render("quad");
        
    // render transition effect, if active
        if (RootScene.currentState() == RootScene.State.BATTLE_INTRO) {
            //glBlendFunc(GL_ONE, GL_ONE);
            ShaderMap.use("quad");
            Uniform.varFloat("colorMult", colorMult,colorMult,colorMult);
            if (BattleTransition.bufferSwitch())
                TextureMap.bindUnfiltered("fbo_transition1");
            else
                TextureMap.bindUnfiltered("fbo_transition2");
            MeshMap.render("quad");
            Uniform.varFloat("colorMult", 1,1,1);
            //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }
        
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

        dofScene2.makeCurrent();
        ShaderMap.use("quad_dofBlur");
        Uniform.samplerAndTextureFiltered("bgl_DepthTexture", 1, "fbo_dofvalue");
        Uniform.varFloat("bgl_RenderedTextureWidth", width);
        Uniform.varFloat("bgl_RenderedTextureHeight", height);
        Uniform.varFloat("focalDepth", 0);
        TextureMap.bindFiltered(texName);
        glClear(GL_COLOR_BUFFER_BIT);
        MeshMap.render("quad");
        /*
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
        */
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
        
        if (ditherScene != null)
            ditherScene.destroy();
        ditherScene = new FBO(width*2,height*2,false,false,null);
        TextureMap.delete("fbo_dither");
        ditherScene.addToTextureMap("fbo_dither");
        
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
        
        if (!finalPassShaderInitialized)
            initFinalPassShader();
    }
    private static void initFinalPassShader() {
        finalPassShaderInitialized = true;
        
        // set uniforms here
    }

    @Override
    public void destroy() {
    }
    
    /**
     * Global weight to the depth-of-field interpolation.
     * @param val -1 forces full blur, 1 forces no blur, 0 is default.
     */
    public static void setDOFCoef(float val) { dofCoefTarget = val; }
    public static void fadeOut(Runnable function) {
        // I really hope I know what I'm doing...
        fadeToBlack = true;
        callbackFunction = function;
    }
}
