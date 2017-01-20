package ui.selectionmenubehavior;

import mote4.scenegraph.Window;
import scenes.RootScene;
import ui.MenuHandler;

/**
 * Created by Peter on 12/31/16.
 */
public class TitleMenu implements  SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "MAIN MENU";
    private String[] options = {"New Game","Options","Editor","Quit"};

    public TitleMenu(MenuHandler h) {
        handler = h;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getNumElements() {
        return options.length;
    }

    @Override
    public String getElementName(int index) {
        return options[index];
    }

    @Override
    public void onAction(int index) {
        switch (getElementName(index)) {
            case "New Game":
                RootScene.setState(RootScene.State.INGAME);
                break;
            case "Options":
                handler.openMenu(new OptionsMenu(handler));
                break;
            case "Editor":
                RootScene.setState(RootScene.State.EDITOR);
                break;
            case "Quit":
                Window.destroy();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {}

    @Override
    public void onFocus() {}

    @Override
    public void onClose() {}

    @Override
    public void onCloseCleanup() {}
}
