package ui.selectionmenubehavior;

import ui.MenuHandler;
import ui.selectionmenubehavior.skill.RootSkillMenu;


/**
 * The root pause menu for the overworld.
 * @author Peter
 */
public class RootIngameMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "MENU";
    private String[] options = {"Inventory","Skills","Options","Exit"};

    public RootIngameMenu(MenuHandler h) {
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
            case "Inventory":
                handler.openMenu(new IngameItemMenu(handler));
                break;
            case "Skills":
                handler.openMenu(new RootSkillMenu(handler));
                break;
            case "Options":
                handler.openMenu(new OptionsMenu(handler));
                break;
            case "Exit":
                handler.closeMenu();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {}

    @Override
    public void onFocus() {}

    @Override
    public void onClose() {
        handler.closeMenu();
    }
    @Override
    public void onCloseCleanup() {}
}
