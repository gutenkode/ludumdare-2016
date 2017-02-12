package scenes;

import map.MapData;
import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
import nullset.Const;
import nullset.Input;
import nullset.RootLayer;
import terminal.TerminalSession;
import terminal.filesystem.DefaultFilesystem;
import ui.EditorUIManager;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 1/19/17.
 */
public class EditorUI implements Scene {

    private static MapData mapPreview = null;

    private TerminalSession session;
    private Transform trans;

    public EditorUI() {
        trans = new Transform();
    }

    public static void setMapPreview(MapData md) {
        mapPreview = md;
    }

    @Override
    public void update(double delta) {

        if (Input.currentLock() != Input.Lock.TERMINAL)
        {
            if (Input.isKeyNew(Input.Keys.ESC))
            {
                if (session == null)
                    session = new TerminalSession(DefaultFilesystem.getDefaultFilesystem());
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

        renderTilesetPreview();
        renderMapPreview();

        if (Input.currentLock() != Input.Lock.TERMINAL)
            EditorUIManager.render(trans);
    }
    private void renderMapPreview() {
        if (mapPreview != null) {
            ShaderMap.use("color");
            trans.makeCurrent();
            trans.model.setIdentity();
            trans.model.translate(160,60);
            trans.model.scale(3,3,1);
            for (int[] arr : mapPreview.heightData) {
                trans.model.push();
                for (int i : arr) {
                    trans.model.makeCurrent();
                    if (i == 0)
                        Uniform.varFloat("colorMult",1,1,1,1);
                    else if (i > 0)
                        Uniform.varFloat("colorMult",1-i/4f,0,0,1);
                    else
                        Uniform.varFloat("colorMult",0,0,1+i/4f,1);
                    MeshMap.render("quad");
                    trans.model.translate(0,2);
                }
                trans.model.pop();
                trans.model.translate(2,0);
            }
        }
    }
    private void renderTilesetPreview() {
        // texture sheet
        ShaderMap.use("texture");
        trans.model.setIdentity();
        trans.model.translate(RootLayer.width()-128,128);
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
        trans.model.translate(RootLayer.width()-256+16+32*x,16+32*y);
        trans.model.scale(128/8,128/8,1);
        trans.makeCurrent();
        MeshMap.render("quad");

        x = (int)(pos[1] % Const.TILESHEET_X);
        y = (int)(pos[1] / Const.TILESHEET_Y);
        trans.model.setIdentity();
        trans.model.translate(RootLayer.width()-256+16+32*x,16+32*y);
        trans.model.scale(128/8,128/8,1);
        trans.makeCurrent();
        Uniform.varFloat("colorMult",1,1,0,1);
        MeshMap.render("quad");
        Uniform.varFloat("colorMult",1,1,1,1);
    }

    @Override
    public void framebufferResized(int width, int height) {
        trans.projection.setOrthographic(0, 0, width, height, -1, 1);
    }

    @Override
    public void destroy() {

    }
}
