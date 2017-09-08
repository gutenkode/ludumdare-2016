package ui.selectionmenubehavior.editor;

import entities.Entity;
import scenes.Editor;
import ui.MenuHandler;
import ui.selectionmenubehavior.OptionsMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Created by Peter on 1/19/17.
 */
public class EntityModMenu implements SelectionMenuBehavior {

    private MenuHandler handler;
    private Entity entity;

    private String title;
    private String[] options = {"Move", "Attributes", "Delete", "Exit"};

    public EntityModMenu(MenuHandler h, Entity e) {
        title = e.getName();
        entity = e;
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
            case "Move":
                break;
            case "Attributes":
                break;
            case "Delete":
                break;
            case "Exit":
                handler.closeMenu();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {
        handler.showFlavorText(false, "Attributes:\n"+entity.getAttributeString()+"\n\nSerialized:\n"+entity.serialize());
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
