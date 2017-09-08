package ui.selectionmenubehavior.editor;

import map.MapLoader;
import main.RootLayer;
import scenes.Editor;
import ui.EditorUIManager;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Created by Peter on 1/19/17.
 */
public class ToolsMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "TOOLS";
    private String[] options = {"New","Save", "Load", "Delete", "Quit Editor", "Exit"};

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
            case "New":
                EditorUIManager.logMessage("Not supported yet.");
                break;
            case "Save":
                if (Editor.getMapEditor() != null) {
                    if (MapLoader.saveMapFile(Editor.getMapEditor().getMapData()))
                        EditorUIManager.logMessage("File written successfully.");
                    else
                        EditorUIManager.logMessage("Error writing file.");
                } else
                    EditorUIManager.logMessage("No file loaded.");
                break;
            case "Load":
                handler.openMenu(new MapListMenu(handler, MapListMenu.Action.LOAD));
                break;
            case "Delete":
                handler.openMenu(new MapListMenu(handler, MapListMenu.Action.DELETE));
                break;
            case "Quit Editor":
                Editor.unloadMap();
                EditorUIManager.closeAllMenus();
                RootLayer.loadTitle();
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
