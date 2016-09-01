package scenes;

import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import static org.lwjgl.opengl.GL11.*;
import rpgbattle.BattleManager;
import ui.BattleUIManager;

/**
 * Renders 2D UI elements in a battle.
 * @author Peter
 */
public class BattleUI implements Scene {
    
    private Transform trans;
    
    public BattleUI() {
        trans = new Transform();
    }

    @Override
    public void update(double delta) {
        BattleManager.update();
        BattleUIManager.update();
    }

    @Override
    public void render(double delta) {
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        
        ShaderMap.use("texture");
        trans.model.setIdentity();
        trans.makeCurrent();
        
        BattleUIManager.render(trans);
    }
    
    @Override
    public void framebufferResized(int width, int height) {
        // origin in top left
        trans.projection.setOrthographic(0, 0, width, height, -1, 1);
        BattleUIManager.refreshEnemies();
    }

    @Override
    public void destroy() {
    
    }
}
