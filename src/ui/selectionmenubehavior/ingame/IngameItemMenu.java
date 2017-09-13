package ui.selectionmenubehavior.ingame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rpgsystem.BattleEffect;
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
    List<Item> items;
    List<Item.ItemType> typeList;

    /**
     * List inventory items in the overworld.
     * @param h
     * @param name Will be appended to "ITEMS".
     * @param types Only the specified types of items will be in this list.
     */
    public IngameItemMenu(MenuHandler h, String name, Item.ItemType... types) {
        handler = h;
        typeList = new ArrayList<>();
        for (Item.ItemType t : types)
            typeList.add(t);
        if (name != null && !name.isEmpty())
            title += ": "+name;
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
        Map<Item, Integer> allItems = Inventory.get();
        items = new ArrayList<>();
        // only list items of the specified types
        for (Item i : allItems.keySet())
            if (typeList.isEmpty() || typeList.contains(i.itemType))
                items.add(i);

        options = new String[items.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++) {
            Item item = items.get(i);
            options[i] = item.name;
            int numItem = allItems.get(item);
            if (numItem > 1)
                options[i] += "  ...  x"+numItem;
        }
    }
}