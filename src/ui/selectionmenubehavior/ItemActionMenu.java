package ui.selectionmenubehavior;

import rpgsystem.Item;
import ui.MenuHandler;

/**
 *
 * @author Peter
 */
public class ItemActionMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private Item item;
    private String title;
    private String[] options = {"Use","Examine","Discard","Exit"};

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
            case "Examine":
                handler.showDialogue("Can't examine.",item.spriteName);
                break;
            case "Discard":
                if (item.canDiscard)
                    handler.openMenu(new ItemDiscardMenu(item, handler));
                else
                    handler.showDialogue("You can't discard this item.",item.spriteName);
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
        handler.showFlavorText(true, item.desc, item.spriteName);
    }

    @Override
    public void onClose() {
        handler.closeMenu();
    }
}
