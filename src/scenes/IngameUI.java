package scenes;

import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import nullset.Input;
import static org.lwjgl.opengl.GL11.*;
import ui.IngameUIManager;

/**
 * Renders 2D UI elements in the overworld.
 * @author Peter
 */
public class IngameUI implements Scene {
    
    private Transform trans;
    
    public IngameUI() {
        trans = new Transform();
    }

    @Override
    public void update(double delta) {
        IngameUIManager.update();
        
        if (Input.currentLock() == Input.Lock.PLAYER)
            if (Input.isKeyNew(Input.Keys.NO))
                IngameUIManager.pauseGame();
    }

    @Override
    public void render(double delta) {
        //glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        
        IngameUIManager.render(trans);
    }
    
    @Override
    public void framebufferResized(int width, int height) {
        // origin in top left
        trans.projection.setOrthographic(0, 0, width, height, -1, 1);
    }

    @Override
    public void destroy() {
    
    }
}