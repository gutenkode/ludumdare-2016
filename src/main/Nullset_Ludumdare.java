package main;

import map.MapLevelManager;
import mote4.scenegraph.Window;
import mote4.util.ErrorUtils;
import mote4.util.audio.ALContext;
import mote4.util.audio.AudioLoader;
import mote4.util.audio.AudioPlayback;
import mote4.util.shader.ShaderUtils;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.MeshMap;
import scenes.Postprocess;

import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeLimits;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Peter
 */
public class Nullset_Ludumdare {

    private static RootLayer rootLayer;
    
    public static void main(String[] args) {
        //ErrorUtils.debug(true);
        //Window.initFullscreen();
        //Window.initWindowed(1920/2, 1080/2);
        Window.setVsync(false);
        Window.setTitle("Nullset");
        Window.initWindowedPercent(.666, 16/9.0);
        ALContext.initContext();
        AudioPlayback.enableMusic(false);
        
        Input.createCharCallback();
        Input.createKeyCallback();
        Input.pushLock(Input.Lock.PLAYER);
        loadResources();
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //glfwSetInputMode(Window.getWindowID(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetWindowSizeLimits(Window.getWindowID(), 640, 360, GLFW_DONT_CARE, GLFW_DONT_CARE);
        //glfwSetWindowAspectRatio(Window.getWindowID(), 16, 9);
        
        // important line, this sets up the level loading process
        MapLevelManager.loadIndexFile("index.txt");
        MapLevelManager.setCurrentLevel(1);

        rootLayer = RootLayer.getInstance();
        Window.addLayer(rootLayer);
        Window.addScene(new Postprocess()); // Postprocess is on the default layer and renders to the screen
        Window.loop(60);
    }
    
    private static void loadResources() {
        TextureMap.loadIndex("index.txt");
        ShaderUtils.loadIndex("index.txt");
        AudioLoader.loadIndex("index.txt");

        TextureMap.loadCubemap("object/chain", "cubemap");

        FontUtils.loadMetric("font/misterpixel/misterpixel","font_1");
        FontUtils.loadMetric("font/6px/6px","6px");
        FontUtils.loadMetric("font/terminal/terminal","terminal");
        FontUtils.useMetric("font_1");
                
        MeshMap.add(StaticMeshBuilder.loadQuadMesh(), "quad");
        MeshMap.add(StaticMeshBuilder.constructVAOFromOBJ("cube", false), "cube");
        MeshMap.add(StaticMeshBuilder.constructVAOFromOBJ("barrel", false), "barrel");
        MeshMap.add(StaticMeshBuilder.constructVAOFromOBJ("pipe", false), "pipe");
        MeshMap.add(StaticMeshBuilder.constructVAOFromOBJ("hexahedron", false), "hexahedron");

        ErrorUtils.checkGLError();
    }

    public static RootLayer rootLayer() { return rootLayer; }
}
