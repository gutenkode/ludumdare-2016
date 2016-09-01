package ui.selectionmenubehavior;

import rpgsystem.Item;
import ui.MenuHandler;

/**
 *
 * @author Peter
 */
public class ItemDiscardMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private Item item;
    private String title = "DISCARD?";
    private String[] options = {"Yes","No"};

    public ItemDiscardMenu(Item i, MenuHandler h) {
        item = i;
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
        switch (index) {
            case 0:
                item.discard();
                handler.closeMenu();
                // no break; is INTENTIONAL
            case 1:
                handler.closeMenu();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {
        //handler.showDialogue(item.desc, item.spriteName);
    }

    @Override
    public void onFocus() {
    }

    @Override
    public void onClose() {
        handler.closeMenu();
    }
}
