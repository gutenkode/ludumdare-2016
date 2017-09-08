package ui.selectionmenubehavior.editor;

import scenes.Editor;
import ui.MenuHandler;
import ui.selectionmenubehavior.OptionsMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Created by Peter on 1/19/17.
 */
public class RootEditorMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "EDITOR";
    private String[] options = {"File", "Entities", "Options"};

    public RootEditorMenu(MenuHandler h) {
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
            case "File":
                handler.openMenu(new ToolsMenu(handler));
                break;
            case "Entities":
                if (Editor.getMapEditor() != null)
                    handler.openMenu(new EntityListMenu(handler, Editor.getMapEditor().getEntities()));
                break;
            case "Options":
                handler.openMenu(new OptionsMenu(handler));
                break;
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