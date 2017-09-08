package ui.selectionmenubehavior.ingame;

import mote4.util.audio.AudioPlayback;
import rpgsystem.Item;
import ui.MenuHandler;
import ui.selectionmenubehavior.ConfirmMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 *
 * @author Peter
 */
public class ItemActionMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private Item item;
    private String title;
    private String[] options = {"Use","Discard","Exit"};

    public ItemActionMenu(Item i, MenuHandler h) {
        item = i;
        title = item.name.toUpperCase();
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
            case "Use":
                item.useIngame(handler);
                break;
            case "Discard":
                if (item.itemType == Item.ItemType.CONSUMABLE)
                    handler.openMenu(new ConfirmMenu(handler, "DISCARD?", this::discardCallback));
                else {
                    AudioPlayback.playSfx("sfx_menu_invalid");
                    handler.showDialogue("You can't discard this item.", item.spriteName);
                }
                break;
            case "Exit":
                handler.closeMenu();
                break;
        }
    }
    private void discardCallback(boolean b) {
        if (b) {
            item.discard();
            handler.closeMenu();
        }
    }

    @Override
    public void onHighlight(int index) {
    }

    @Override
    public void onFocus() {
        handler.showFlavorText(true, item.desc, item.spriteName);
    }

    @Override
    public void onClose() {
        handler.closeMenu();
    }
    @Override
    public void onCloseCleanup() {}
}
