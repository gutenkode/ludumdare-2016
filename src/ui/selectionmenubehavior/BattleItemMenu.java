package ui.selectionmenubehavior;

import java.util.ArrayList;
import rpgbattle.BattleManager;
import rpgsystem.Inventory;
import rpgsystem.Item;
import ui.BattleUIManager;
import ui.MenuHandler;

/**
 * In-battle inventory menu.
 * @author Peter
 */
public class BattleItemMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "INVENTORY";
    private String[] options;
    ArrayList<Item> items;
        

    public BattleItemMenu(MenuHandler h) {
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
            if (BattleManager.getPlayer().useItem(handler, items.get(index)))
                BattleUIManager.endPlayerTurn();
    }

    @Override
    public void onHighlight(int index) {
        handler.closeDialogue();
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

    private void rebuildMenu() {
        items = Inventory.get();
        options = new String[items.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            options[i] = items.get(i).name;
    }
}