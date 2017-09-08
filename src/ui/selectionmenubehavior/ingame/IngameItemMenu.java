package ui.selectionmenubehavior.ingame;

import java.util.ArrayList;
import rpgsystem.Inventory;
import rpgsystem.Item;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * The player's inventory.
 * @author Peter
 */
public class IngameItemMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "ITEMS";
    private String[] options;
    ArrayList<Item> items;
        

    public IngameItemMenu(MenuHandler h) {
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
        if (index == options.length-1)
            handler.closeMenu();
        else
            handler.openMenu(new ItemActionMenu(items.get(index), handler));
    }

    @Override
    public void onHighlight(int index) {
        if (index == options.length-1)
            handler.closeFlavorText();
        else
            handler.showFlavorText(false, items.get(index).desc, items.get(index).spriteName);
    }

    @Override
    public void onFocus() {
        rebuildMenu();
    }

    @Override
    public void onClose() {
        handler.closeMenu();
    }
    @Override
    public void onCloseCleanup() {}

    private void rebuildMenu() {
        items = Inventory.get();
        options = new String[items.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            options[i] = items.get(i).name;
    }
}