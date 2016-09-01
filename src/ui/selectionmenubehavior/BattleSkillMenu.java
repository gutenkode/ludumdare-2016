package ui.selectionmenubehavior;

import java.util.ArrayList;
import rpgbattle.BattleManager;
import rpgbattle.PlayerSkills;
import rpgsystem.Skill;
import ui.BattleUIManager;
import ui.MenuHandler;
import ui.components.PlayerStatBar;

/**
 * A list of skills usable by the player in battle.
 * @author Peter
 */
public class BattleSkillMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "SKILLS";
    private String[] options;
    private ArrayList<Skill> skills;
    
    public BattleSkillMenu(MenuHandler h) {
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
            if (BattleManager.getPlayer().useSkill(handler, skills.get(index))) {
                PlayerStatBar.stopManaPreview();
                BattleUIManager.endPlayerTurn();
            }
        }
    }

    @Override
    public void onHighlight(int index) {
        handler.closeDialogue(); // remove "not enough mana" message
        if (index == options.length-1) {
            PlayerStatBar.stopManaPreview();
            handler.closeFlavorText();
        } else {
            PlayerStatBar.previewManaCost(skills.get(index).cost());
            handler.showFlavorText(false, skills.get(index).getFullInfoString(), skills.get(index).spriteName);
        }
    }

    @Override
    public void onFocus() {
        rebuildMenu();
    }

    @Override
    public void onClose() {
        PlayerStatBar.stopManaPreview();
        handler.closeMenu();
    }

    private void rebuildMenu() {
        skills = PlayerSkills.getEquippedSkills(); // only show equipped skills
        
        options = new String[skills.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            options[i] = skills.get(i).name;
    }
}