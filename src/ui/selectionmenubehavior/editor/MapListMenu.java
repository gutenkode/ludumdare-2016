package ui.selectionmenubehavior.editor;

import entities.Entity;
import map.MapLevelManager;
import map.MapLoader;
import scenes.Editor;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Peter on 1/19/17.
 */
public class MapListMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "MAPS";
    private String[] options;

    public MapListMenu(MenuHandler h) {
        handler = h;

        File file = new File("src/res/maps/"+MapLevelManager.levelPath()+"/");
        System.out.println(file.getAbsolutePath());
        if (file.exists() && file.isDirectory()) {
            String[] files = file.list();
            int numFiles = 0;
            for (String s : files)
                if (s.endsWith(".rf2"))
                    numFiles++;
            options = new String[numFiles+1];
            int filesWritten = 0;
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(".rf2")) {
                    options[filesWritten] = files[i].replace(".rf2","");
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
            Editor.loadMap(options[index]);
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
