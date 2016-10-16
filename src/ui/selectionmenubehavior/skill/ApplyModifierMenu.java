package ui.selectionmenubehavior.skill;

import java.util.ArrayList;
import rpgbattle.PlayerSkills;
import rpgsystem.Skill;
import rpgsystem.SkillModifier;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Allows the player to apply a modifier to a skill.
 * @author Peter
 */
public class ApplyModifierMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "APPLY TO";
    private String[] options;
    private ArrayList<Skill> skills;
    private SkillModifier modifier;
    
    public ApplyModifierMenu(SkillModifier m, MenuHandler h) {
        handler = h;
        modifier = m;
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
            PlayerSkills.applyModifier(modifier, skills.get(index));
            handler.closeMenu();
        }
    }

    @Override
    public void onHighlight(int index) {
        if (index == options.length-1) {
            handler.closeFlavorText();
        } else {
            handler.showFlavorText(false, skills.get(index).getFullInfoString(), skills.get(index).spriteName);
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
        skills = PlayerSkills.getEquippedSkills(); // only equipped skills can have modifiers
        
        options = new String[skills.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            if (PlayerSkills.getLinkedModifier(skills.get(i)) == modifier)
                options[i] = ">"+skills.get(i).name;
            else
                options[i] = skills.get(i).name;
    }
}