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
public class EquipSkillMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "SKILLS";
    private String[] options;
    private Skill[] skills;
    
    public EquipSkillMenu(MenuHandler h) {
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
            AudioPlayback.playSfx("sfx_menu_equip");
            PlayerSkills.toggleEquipped(skills[index]);
            handler.forceMenuRefocus();
        }
    }

    @Override
    public void onHighlight(int index) {
        if (index == options.length-1) {
            handler.closeFlavorText();
        } else {
            handler.showFlavorText(true, skills[index].data.getFullInfoString(), skills[index].data.spriteName);
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
        skills = new Skill[PlayerSkills.getAvailableSkills().size()];
        
        options = new String[PlayerSkills.getAvailableSkills().size()+1];
        options[options.length-1] = "Exit";

        int i = 0;
        for (Skill s : PlayerSkills.getEquippedSkills()) {
            options[i] = ">"+s.data.name;
            skills[i] = s;
            i++;
        }
        for (Skill s : PlayerSkills.getAvailableSkills()) {
            if (!PlayerSkills.isSkillEquipped(s)) {
                options[i] = s.data.name;
                skills[i] = s;
                i++;
            }
        }
    }
}