package ui;

import mote4.util.matrix.ModelMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import nullset.Const;
import scenes.RootScene;
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

    static {
        manager = new EditorUIManager();
        selectionMenus = new Stack<>();

        manager.openMenu(new RootEditorMenu(manager));
    }

    public static void update() {
        selectionMenus.peek().update();
    }
    public static void render(Transform trans) {
        ModelMatrix model = trans.model;

        ShaderMap.use("texture");
        trans.makeCurrent();
        TextureMap.bindUnfiltered("font_1");

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

    // menu methods

    @Override
    public void openMenu(SelectionMenuBehavior b) {
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
        if (selectionMenus.size() > 1) {
            selectionMenus.pop();
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
