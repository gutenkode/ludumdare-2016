package ui.selectionmenubehavior.battle;

import java.util.ArrayList;
import rpgbattle.BattleManager;
import rpgbattle.PlayerSkills;
import rpgbattle.fighter.Fighter;
import rpgsystem.BattleEffect;
import rpgsystem.Skill;
import rpgsystem.SkillModifier;
import ui.BattleUIManager;
import ui.MenuHandler;
import ui.components.PlayerStatBar;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * A list of skills usable by the player in battle.
 * @author Peter
 */
public class BattleSkillMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "SKILLS";
    private String[] options;
    private ArrayList<Skill> skills;

    private Skill currentSkill;
    
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
            if (BattleManager.getPlayer().canUseSkill(handler, skills.get(index))) {
                currentSkill = skills.get(index);
                boolean isMultiTarget = (PlayerSkills.getLinkedModifier(currentSkill) == SkillModifier.MOD_MULTI_TARGET);
                boolean isEnemyTarget = currentSkill.data.effect != BattleEffect.HEAL;
                handler.openMenu(new EnemySelectionMenu(handler, this::skillCallback, isMultiTarget, isEnemyTarget, !isEnemyTarget));
            }
        }
    }
    public void skillCallback(Fighter... f) {
        if (BattleManager.getPlayer().useSkill(handler, currentSkill, f)) {
            currentSkill = null;
            PlayerStatBar.stopStatPreview();
            BattleUIManager.endPlayerTurn();
        }
    }

    @Override
    public void onHighlight(int index) {
        handler.closeDialogue(); // remove "not enough mana" message
        if (index == options.length-1) {
            PlayerStatBar.stopStatPreview();
            handler.closeFlavorText();
        } else {
            if (skills.get(index).data.usesSP)
                PlayerStatBar.previewStaminaCost(skills.get(index).data.cost(), false);
            else
                PlayerStatBar.previewManaCost(skills.get(index).data.cost());
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
    public void onCloseCleanup() { PlayerStatBar.stopStatPreview(); }

    private void rebuildMenu() {
        skills = PlayerSkills.getEquippedSkills(); // only show equipped skills
        
        options = new String[skills.size()+1];
        options[options.length-1] = "Exit";
        for (int i = 0; i < options.length-1; i++)
            options[i] = skills.get(i).data.name;
    }
}