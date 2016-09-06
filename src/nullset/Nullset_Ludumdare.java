package nullset;

import map.MapLevelManager;
import mote4.scenegraph.Layer;
import mote4.scenegraph.Window;
import mote4.scenegraph.target.EmptyTarget;
import mote4.scenegraph.target.FBO;
import mote4.util.shader.ShaderUtils;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.MeshMap;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import scenes.*;

/**
 *
 * @author Peter
 */
public class Nullset_Ludumdare {
    
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true"); // prevents ImageIO from hanging
        //Window.initFullscreen();
        //Window.initWindowed(1920/2, 1080/2);
        Window.initWindowedPercent(.75, 16/9.0);
        
        Input.createCharCallback();
        Input.createKeyCallback();
        Input.pushLock(Input.Lock.PLAYER);
        loadResources();
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glEnable(GL_CULL_FACE);
        
        glfwSetWindowSizeLimits(Window.getWindowID(), 640, 360, GLFW_DONT_CARE, GLFW_DONT_CARE);
        //glfwSetWindowAspectRatio(Window.getWindowID(), 16, 9);
        
        // important line, this sets up the level loading process
        MapLevelManager.loadIndexFile("index.txt");
        MapLevelManager.setCurrentLevel(1);
        
        //FBO ingame = new FBO(853,480,true,false,null); // 640x480, PS1 resolution
        //FBO ingame = new FBO(1280/2,720/2,true,false,null);
        //FBO ingame = new FBO(1920/3,1080/3,true,false,null);
        //ingame.addToTextureMap("fbo_scene");
        Layer l = new Layer(new EmptyTarget());
        l.addScene(new RootScene());
        Window.addLayer(l);
        
        Window.addScene(new Postprocess());
        Window.loop();
    }
    
    private static void loadResources() {
        ShaderUtils.addProgram("ingame_map.vert", "ingame_map.frag", "ingame_map");
        //ShaderUtils.addProgram("ingame_object.vert", "ingame_object.frag", "ingame_object");
        ShaderUtils.addProgram("texture.vert", "texture.frag", "texture");
        ShaderUtils.addProgram("texture_color.vert", "texture_color.frag", "texture_color");
        ShaderUtils.addProgram("color.vert", "color.frag", "color");
        ShaderUtils.addProgram("quad_dither.vert", "quad_dither.frag", "quad_dither");
        ShaderUtils.addProgram("spritesheet_nolight.vert", "spritesheet_nolight.frag", "spritesheet_nolight");
        ShaderUtils.addProgram("spritesheet_light.vert", "spritesheet_light.frag", "spritesheet_light");
        ShaderUtils.addProgram("battle_bg.vert", "battle_bg.frag", "battle_bg");
        /*
        String vertSource = ShaderUtils.loadSource("shadowCubeMap.vert");
        String geomSource = ShaderUtils.loadSource("shadowCubeMap.geom");
        String fragSource = ShaderUtils.loadSource("shadowCubeMap.frag");
        int vert = ShaderUtils.compileShaderFromSource(vertSource, ShaderUtils.VERTEX);
        int geom = ShaderUtils.compileShaderFromSource(geomSource, ShaderUtils.GEOMETRY);
        int frag = ShaderUtils.compileShaderFromSource(fragSource, ShaderUtils.FRAGMENT);
        int prog = ShaderUtils.addProgram(new int[] {vert, geom, frag}, "shadowCubeMap");
        */
        ShaderUtils.addProgram("shadowMap.vert", "shadowMap.frag", "shadowMap");
        ShaderUtils.addProgram("shadowMap_tex.vert", "shadowMap_tex.frag", "shadowMap_tex");
        
        ShaderUtils.addProgram("quad.vert", "quad.frag", "quad");
        ShaderUtils.addProgram("quad_transition.vert", "quad_transition.frag", "quad_transition");
        ShaderUtils.addProgram("quad.vert", "quad_vertBlur.frag", "quad_vertBlur");
        ShaderUtils.addProgram("quad.vert", "quad_horizBlur.frag", "quad_horizBlur");
        ShaderUtils.addProgram("quad.vert", "quad_hdr.frag", "quad_hdr");
        ShaderUtils.addProgram("quad.vert", "quad_final.frag", "quad_final");
        
        FontUtils.loadMetric("font/misterpixel/misterpixel_metric","font_1");
        FontUtils.loadMetric("font/6px/6px_metric","6px");
        FontUtils.loadMetric("font/terminal/terminal_metric","terminal");
        FontUtils.useMetric("font_1");
        TextureMap.loadIndex("index.txt");
                
        MeshMap.add(StaticMeshBuilder.loadQuadMesh(), "quad");
        MeshMap.add(StaticMeshBuilder.constructVAOFromOBJ("cube", false), "cube");
        MeshMap.add(StaticMeshBuilder.constructVAOFromOBJ("barrel", false), "barrel");
    }
}
