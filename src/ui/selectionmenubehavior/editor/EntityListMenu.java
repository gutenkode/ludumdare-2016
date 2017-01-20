package ui.selectionmenubehavior.editor;

import entities.Entity;
import scenes.Editor;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

import java.util.ArrayList;

/**
 * Created by Peter on 1/19/17.
 */
public class EntityListMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "ENTITIES";
    private String[] options;
    private ArrayList<Entity> entities;

    public EntityListMenu(MenuHandler h, ArrayList<Entity> entities) {
        handler = h;
        this.entities = entities;
        options = new String[entities.size()+1];
        for (int i = 0; i < entities.size(); i++)
            options[i] = entities.get(i).getName();
        options[options.length-1] = "Exit";
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
            handler.openMenu(new EntityModMenu(handler, entities.get(index)));
        }
    }

    @Override
    public void onHighlight(int index) {
        if (index < entities.size()) {
            Entity e = entities.get(index);
            Editor.lookAt(e);
        }
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
