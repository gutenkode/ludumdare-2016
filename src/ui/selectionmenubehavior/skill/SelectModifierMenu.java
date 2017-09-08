package ui.selectionmenubehavior.skill;

import java.util.ArrayList;
import rpgbattle.PlayerSkills;
import rpgsystem.SkillModifier;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Allows the player to select a modifier they possess to apply to a skill.
 * @author Peter
 */
public class SelectModifierMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "MODIFIERS";
    private String[] options;
    private ArrayList<SkillModifier> modifiers;
    
    public SelectModifierMenu(MenuHandler h) {
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
            handler.openMenu(new ApplyModifierMenu(modifiers.get(index), handler));
        }
    }

    @Override
    public void onHighlight(int index) {
        if (index == options.length-1) {
            handler.closeFlavorText();
        } else {
            handler.showFlavorText(true, modifiers.get(index).getFullInfoString());
        }
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
        modifiers = PlayerSkills.getAvailableModifiers(); // only show skills the player has
        
        options = new String[modifiers.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            if (PlayerSkills.isModifierApplied(modifiers.get(i)))
                options[i] = ">"+modifiers.get(i).name;
            else
                options[i] = modifiers.get(i).name;
    }
}