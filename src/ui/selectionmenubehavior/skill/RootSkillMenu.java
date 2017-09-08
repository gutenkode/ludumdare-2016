package ui.selectionmenubehavior.skill;

import ui.IngameUIManager;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 *
 * @author Peter
 */
public class RootSkillMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "SKILLS";
    private String[] options = {"Use","Equip","Modifiers","Exit"};

    public RootSkillMenu(MenuHandler h) {
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
                handler.openMenu(new UseSkillMenu(handler));
                break;
            case "Equip":
                handler.openMenu(new EquipSkillMenu(handler));
                break;
            case "Modifiers":
                handler.openMenu(new SelectModifierMenu(handler));
                break;
            case "Exit":
                handler.closeMenu();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {
        switch (getElementName(index)) {
            case "Use":
                handler.showFlavorText(false, "Use skills outside of battle.");
                break;
            case "Equip":
                handler.showFlavorText(false, "Choose skills to use during battle.");
                break;
            case "Modifiers":
                handler.showFlavorText(false, "Apply modifiers to skills.");
                break;
            case "Exit":
                handler.closeFlavorText();
                break;
        }
    }

    @Override
    public void onFocus() {
        IngameUIManager.showSkillCostMenu(true);
    }

    @Override
    public void onClose() {
        handler.closeMenu();
        IngameUIManager.showSkillCostMenu(false);
    }
    @Override
    public void onCloseCleanup() {}
}