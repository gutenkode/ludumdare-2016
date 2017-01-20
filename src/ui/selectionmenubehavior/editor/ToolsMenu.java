package ui.selectionmenubehavior.editor;

import scenes.Editor;
import ui.MenuHandler;
import ui.selectionmenubehavior.OptionsMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Created by Peter on 1/19/17.
 */
public class ToolsMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "TOOLS";
    private String[] options = {"Tile Editor", "Save", "Exit"};

    public ToolsMenu(MenuHandler h) {
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
            case "Exit":
                handler.closeMenu();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {
    }

    @Override
    public void onFocus() {
    }

    @Override
    public void onClose() {
        handler.closeMenu();
    }

    @Override
    public void onCloseCleanup() {}
}
