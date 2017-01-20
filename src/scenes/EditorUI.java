package scenes;

import mote4.scenegraph.Scene;
import mote4.scenegraph.Window;
import mote4.util.matrix.Transform;
import nullset.Input;
import org.lwjgl.glfw.GLFW;
import terminal.TerminalSession;
import ui.EditorUIManager;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 1/19/17.
 */
public class EditorUI implements Scene {

    private TerminalSession session;
    private Transform trans;

    public EditorUI() {
        trans = new Transform();
    }

    @Override
    public void update(double delta) {

        if (Input.currentLock() != Input.Lock.TERMINAL)
        {
            if (Input.isKeyNew(Input.Keys.ESC))
            {
                if (session == null)
                    session = new TerminalSession();
                TerminalScene.openTerminal(session);
            }
            else
                EditorUIManager.update();
        }
    }

    @Override
    public void render(double delta) {
        glDisable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT);

        if (Input.currentLock() != Input.Lock.TERMINAL)
            EditorUIManager.render(trans);
    }

    @Override
    public void framebufferResized(int width, int height) {
        trans.projection.setOrthographic(0, 0, width, height, -1, 1);
    }

    @Override
    public void destroy() {

    }
}
