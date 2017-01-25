package ui.selectionmenubehavior.editor;

import entities.Entity;
import map.MapLevelManager;
import map.MapLoader;
import scenes.Editor;
import scenes.EditorUI;
import ui.EditorUIManager;
import ui.MenuHandler;
import ui.selectionmenubehavior.ConfirmMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Peter on 1/19/17.
 */
public class MapListMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title;
    private String[] options;
    private Action action;

    private String actionMap;

    public enum Action {
        LOAD,
        DELETE;
    }

    public MapListMenu(MenuHandler h, Action a) {
        handler = h;
        action = a;
        switch (action) {
            case LOAD: title = "LOAD MAP"; break;
            case DELETE: title = "DELETE MAP"; break;
        }
        refreshList();
    }
    private void refreshList() {
        File file = new File("src/res/maps/"+MapLevelManager.levelPath()+"/");
        System.out.println(file.getAbsolutePath());
        if (file.exists() && file.isDirectory()) {
            String[] files = file.list();
            int numFiles = 0;
            for (String s : files)
                if (s.endsWith(MapLoader.FILE_EXTENSION))
                    numFiles++;
            options = new String[numFiles+1];
            int filesWritten = 0;
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(MapLoader.FILE_EXTENSION)) {
                    options[filesWritten] = files[i].replace(MapLoader.FILE_EXTENSION,"");
                    filesWritten++;
                }
            }
            options[options.length-1] = "Exit";
        } else {
            options = new String[] {"Invalid directory: "+MapLevelManager.levelPath()};
        }
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
        if (index == options.length-1)
            handler.closeMenu();
        else {
            switch (action) {
                case LOAD:
                    actionMap = options[index];
                    handler.openMenu(new ConfirmMenu(handler, "LOAD?", this::loadCallback));
                    break;
                case DELETE:
                    actionMap = options[index];
                    handler.openMenu(new ConfirmMenu(handler, "DELETE?", this::deleteCallback));
                    break;
            }
        }
    }
    private void loadCallback(boolean b) {
        if (b)
            Editor.loadMap(actionMap);
    }
    private void deleteCallback(boolean b) {
        if (b) {
            if (MapLoader.deleteMapFile(actionMap))
                EditorUIManager.logMessage("File deleted.");
            else
                EditorUIManager.logMessage("Could not delete file.");
            refreshList();
            handler.forceMenuRefocus();
        }
    }

    @Override
    public void onHighlight(int index) {
        if (index == options.length-1)
            EditorUI.setMapPreview(null);
        else {
            EditorUI.setMapPreview(MapLoader.getMap(options[index]));
        }
    }

    @Override
    public void onFocus() {
    }

    @Override
    public void onClose() {
        EditorUI.setMapPreview(null);
        handler.closeMenu();
    }

    @Override
    public void onCloseCleanup() {}
}
