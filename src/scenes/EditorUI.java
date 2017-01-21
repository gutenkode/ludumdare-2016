package scenes;

import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
import nullset.Const;
import nullset.Input;
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
            else {
                if (Input.currentLock() != Input.Lock.MENU && Input.isKeyNew(Input.Keys.NO)) {
                    EditorUIManager.openRootMenu();
                }
                EditorUIManager.update();
            }
        }
    }

    @Override
    public void render(double delta) {
        glDisable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT);

        // texture sheet
        ShaderMap.use("texture");
        trans.model.setIdentity();
        trans.model.translate(RootScene.width()-128,128);
        trans.model.scale(128,128,1);
        trans.makeCurrent();
        TextureMap.bindUnfiltered("tileset_1");
        MeshMap.render("quad");

        // current texture position
        TextureMap.bindUnfiltered("ui_square_cursor");
        int[] pos = Editor.currentTileTexInds();

        int x = (int)(pos[0] % Const.TILESHEET_X);
        int y = (int)(pos[0] / Const.TILESHEET_Y);
        trans.model.setIdentity();
        trans.model.translate(RootScene.width()-256+16+32*x,16+32*y);
        trans.model.scale(128/8,128/8,1);
        trans.makeCurrent();
        MeshMap.render("quad");

        x = (int)(pos[1] % Const.TILESHEET_X);
        y = (int)(pos[1] / Const.TILESHEET_Y);
        trans.model.setIdentity();
        trans.model.translate(RootScene.width()-256+16+32*x,16+32*y);
        trans.model.scale(128/8,128/8,1);
        trans.makeCurrent();
        Uniform.varFloat("colorMult",1,1,0,1);
        MeshMap.render("quad");
        Uniform.varFloat("colorMult",1,1,1,1);

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
