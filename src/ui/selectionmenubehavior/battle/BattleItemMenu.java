package ui.selectionmenubehavior.battle;

import java.util.ArrayList;
import rpgbattle.BattleManager;
import rpgbattle.fighter.Fighter;
import rpgsystem.BattleEffect;
import rpgsystem.Inventory;
import rpgsystem.Item;
import ui.BattleUIManager;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * In-battle inventory menu.
 * @author Peter
 */
public class BattleItemMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "INVENTORY";
    private String[] options;
    ArrayList<Item> items;

    private Item currentItem;

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
        else {
            currentItem = items.get(index);
            boolean targetEnemies = false;
            if (currentItem.battleEffect == BattleEffect.ATTACK)
                targetEnemies = true;
            handler.openMenu(new EnemySelectionMenu(handler, this::itemCallback, false, targetEnemies, !targetEnemies));
        }
    }
    public void itemCallback(Fighter... f) {
        if (BattleManager.getPlayer().useItem(handler, currentItem, f)) {
            BattleUIManager.endPlayerTurn();
            currentItem = null;
        }
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
    @Override
    public void onCloseCleanup() {}

    private void rebuildMenu() {
        ArrayList<Item> allItems = Inventory.get();
        items = new ArrayList<>();
        // only list items that can be used in battle
        for (Item i : allItems)
            if (i.battleEffect != BattleEffect.NONE)
                items.add(i);
        options = new String[items.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            options[i] = items.get(i).name;
    }
}