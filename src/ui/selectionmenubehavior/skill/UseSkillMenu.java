package ui.selectionmenubehavior.skill;

import java.util.ArrayList;

import mote4.util.audio.AudioPlayback;
import rpgbattle.PlayerSkills;
import rpgsystem.Skill;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Allows the player to equip and unequip skills they possess.
 * @author Peter
 */
public class UseSkillMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "SKILLS";
    private String[] options;
    private ArrayList<Skill> skills;
    
    public UseSkillMenu(MenuHandler h) {
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
            skills.get(index).useIngame(handler);
        }
    }

    @Override
    public void onHighlight(int index) {
        if (index == options.length-1) {
            handler.closeFlavorText();
        } else {
            handler.showFlavorText(true, skills.get(index).data.getFullInfoString(), skills.get(index).data.spriteName);
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
        skills = PlayerSkills.getAvailableSkills(); // only show skills the player has
        
        options = new String[skills.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            options[i] = skills.get(i).data.name;
    }
}