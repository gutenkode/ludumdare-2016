package ui;

import mote4.util.matrix.ModelMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.ScrollingText;
import nullset.Const;
import nullset.Input;
import nullset.RootLayer;
import ui.components.SelectionMenu;
import ui.selectionmenubehavior.editor.RootEditorMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

import java.util.Stack;

/**
 * Created by Peter on 12/31/16.
 */
public class EditorUIManager implements MenuHandler {

    private static final EditorUIManager manager;
    private static final Stack<SelectionMenu> selectionMenus;

    private static ScrollingText logMessage; // simple string that appears to announce events in the overworld
    private static int logMessageTimeout = 0;

    static {
        manager = new EditorUIManager();
        selectionMenus = new Stack<>();
    }

    public static void update() {
        if (!selectionMenus.empty())
            selectionMenus.peek().update();
    }
    public static void render(Transform trans) {
        ModelMatrix model = trans.model;

        ShaderMap.use("texture");
        trans.makeCurrent();
        TextureMap.bindUnfiltered("font_1");

        if (logMessageTimeout > 0) {
            logMessageTimeout--;
            model.setIdentity();
            model.translate(80, RootLayer.height()-80);
            model.makeCurrent();
            TextureMap.bindUnfiltered("font_1");
            Uniform.varFloat("colorMult",0,0,0,1);
            logMessage.render();

            model.translate(-1, -1);
            model.makeCurrent();
            Uniform.varFloat("colorMult",1,1,1,1);
            logMessage.render();
        }

        model.setIdentity();
        model.translate(Const.UI_SCALE/2, Const.UI_SCALE/2,0);
        for (SelectionMenu sm : selectionMenus) {
            model.translate(Const.UI_SCALE/2, Const.UI_SCALE/2);
            model.makeCurrent();
            model.push();
            sm.render(model);
            model.pop();
        }
    }

    public static void openRootMenu() {
        if (selectionMenus.empty() && Input.currentLock() != Input.Lock.MENU)
            manager.openMenu(new RootEditorMenu(manager));
    }
    public static void closeAllMenus() {
        if (Input.currentLock() == Input.Lock.MENU) {
            while (!selectionMenus.empty())
                manager.closeMenu();
        }
    }
    public static void logMessage(String message) {
        if (logMessage != null)
            logMessage.destroy();
        logMessage = new ScrollingText(message, "font_1", 0, 0, Const.UI_SCALE, Const.UI_SCALE, 1);
        logMessageTimeout = 100;
    }

    // menu methods

    @Override
    public void openMenu(SelectionMenuBehavior b) {
        if (selectionMenus.empty())
            Input.pushLock(Input.Lock.MENU);
        SelectionMenu sm = new SelectionMenu(b);
        selectionMenus.push(sm);
        sm.onFocus();
    }
    @Override
    public void setMenuCursorPos(int i) {
        selectionMenus.peek().setCursorPos(i);
    }
    @Override
    public void forceMenuRefocus() {
        selectionMenus.peek().onFocus();
    }
    @Override
    public void closeMenu() {
        if (selectionMenus.size() > 0) {
            selectionMenus.pop();
            if (selectionMenus.empty())
                Input.popLock();
            else
                selectionMenus.peek().onFocus();
        }
    }

    @Override
    public void showDialogue(String s) {

    }
    @Override
    public void showDialogue(String s, String sprite) {

    }
    @Override
    public void closeDialogue() {

    }
    @Override
    public void displayScriptChoice(String[] s) {

    }
    @Override
    public void loadScriptLine(String s) {

    }
    @Override
    public void endScript(boolean b) {

    }
    @Override
    public void showFlavorText(boolean lock, String s) {

    }
    @Override
    public void showFlavorText(boolean lock, String s, String sprite) {

    }
    @Override
    public void setFlavorTextLock(boolean lock) {

    }
    @Override
    public void closeFlavorText() {

    }
}
