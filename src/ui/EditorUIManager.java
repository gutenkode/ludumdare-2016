package ui;

import mote4.util.matrix.ModelMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.ScrollingText;
import main.Vars;
import main.Input;
import main.RootLayer;
import ui.components.FlavorTextMenu;
import ui.components.selectionMenu.SelectionMenu;
import ui.components.selectionMenu.SingleSelectionMenu;
import ui.selectionmenubehavior.editor.RootEditorMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

import java.util.Stack;

/**
 * Created by Peter on 12/31/16.
 */
public class EditorUIManager implements MenuHandler {

    private static final EditorUIManager manager;
    private static final Stack<SelectionMenu> selectionMenus;

    private static boolean showFlavorText = false, lockFlavorText = false;

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

        ShaderMap.use("texture_uiblur");
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
        model.translate(Vars.UI_SCALE/2, Vars.UI_SCALE/2,0);
        for (SelectionMenu sm : selectionMenus) {
            model.translate(Vars.UI_SCALE/2, Vars.UI_SCALE/2);
            model.makeCurrent();
            model.push();
            sm.render(model);
            model.pop();
        }
        if (!selectionMenus.empty() && showFlavorText)
        {
            float yOffset;
            if (lockFlavorText)
                yOffset = 0;
            else
                yOffset = selectionMenus.peek().cursorPos()+.75f;
            yOffset *= Vars.UI_SCALE;

            model.translate(selectionMenus.peek().width()+ Vars.UI_SCALE*2,yOffset);
            model.makeCurrent();
            FlavorTextMenu.render();
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
        logMessage = new ScrollingText(message, "font_1", 0, 0, Vars.UI_SCALE, Vars.UI_SCALE, 1);
        logMessageTimeout = 100;
    }

    // menu methods

    @Override
    public void openMenu(SelectionMenuBehavior b) {
        closeFlavorText();
        if (selectionMenus.empty())
            Input.pushLock(Input.Lock.MENU);
        SelectionMenu sm = new SingleSelectionMenu(b);
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
        if (Input.currentLock() != Input.Lock.MENU)
            return;
        closeFlavorText();
        if (selectionMenus.size() > 0) {
            selectionMenus.pop();
            if (selectionMenus.empty())
                Input.popLock(Input.Lock.MENU);
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
        lockFlavorText = lock;
        FlavorTextMenu.setText(s);
        showFlavorText = true;
    }
    @Override
    public void showFlavorText(boolean lock, String s, String sprite) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setFlavorTextLock(boolean lock) {
        lockFlavorText = lock;
    }
    @Override
    public void closeFlavorText() {
        showFlavorText = false;
    }
}
