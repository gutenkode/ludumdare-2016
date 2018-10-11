package ui;

import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import main.Vars;
import main.Input;
import main.RootLayer;
import ui.components.selectionMenu.SelectionMenu;
import ui.components.selectionMenu.SingleSelectionMenu;
import ui.components.selectionMenu.TabbedSelectionMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;
import ui.selectionmenubehavior.TitleMenu;

import java.util.Stack;

/**
 * Created by Peter on 12/31/16.
 */
public class TitleUIManager implements MenuHandler {

    private static final TitleUIManager manager;
    private static final Stack<SelectionMenu> selectionMenus;

    static {
        manager = new TitleUIManager();
        selectionMenus = new Stack<>();

        manager.openMenu(new TitleMenu(manager));
    }

    public static void update() {
        if (Input.currentLock() != Input.Lock.FADE)
            selectionMenus.peek().update();
    }
    public static void render(Transform trans) {
        TransformationMatrix model = trans.model;

        ShaderMap.use("texture_uiblur");
        trans.bind();
        TextureMap.bindUnfiltered("font_1");

        model.setIdentity();
        model.translate(RootLayer.width()/2-50, RootLayer.height()/2,0);
        for (SelectionMenu sm : selectionMenus) {
            model.translate(Vars.UI_SCALE/2, Vars.UI_SCALE/2);
            model.bind();
            model.push();
            sm.render(model);
            model.pop();
        }
    }

    // menu methods

    @Override
    public void openMenu(SelectionMenuBehavior b) {
        SelectionMenu sm = new SingleSelectionMenu(b);
        if (!selectionMenus.empty())
            AudioPlayback.playSfx("sfx_menu_open_pane");
        selectionMenus.push(sm);
        sm.onFocus();
    }
    @Override
    public void openTabbedMenu(SelectionMenuBehavior... b) {
        SelectionMenu sm = new TabbedSelectionMenu(b);
        if (!selectionMenus.empty())
            AudioPlayback.playSfx("sfx_menu_open_pane");
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
            AudioPlayback.playSfx("sfx_menu_close_pane");
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
