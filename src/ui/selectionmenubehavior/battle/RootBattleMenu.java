package ui.selectionmenubehavior.battle;

import rpgbattle.BattleManager;
import rpgbattle.fighter.Fighter;
import ui.BattleUIManager;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;
import ui.selectionmenubehavior.battle.BattleItemMenu;
import ui.selectionmenubehavior.battle.BattleSkillMenu;
import ui.selectionmenubehavior.battle.EnemySelectionMenu;

/**
 * The root menu for battles.
 * A list of actions for the player to take in a battle.
 * @author Peter
 */
public class RootBattleMenu implements SelectionMenuBehavior {
    
    private MenuHandler handler;
    
    private String title = "MENU";
    private String[] options = {"Attack"/*,"Guard"*/,"Skills","Items","Run"};

    public RootBattleMenu(MenuHandler h) {
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
            case "Attack":
                handler.openMenu(new EnemySelectionMenu(handler, this::attackCallback, false,true,false));
                break;
            case "Guard":
                break;
            case "Skills":
                handler.openMenu(new BattleSkillMenu(handler));
                break;
            case "Items":
                handler.openMenu(new BattleItemMenu(handler));
                break;
            case "Run":
                if (BattleManager.getPlayer().useRun(handler))
                    BattleUIManager.endPlayerTurn();
                break;
        }
    }
    private void attackCallback(Fighter... f) {
        if (BattleManager.getPlayer().useAttack(handler, f))
            BattleUIManager.endPlayerTurn();
    }

    @Override
    public void onHighlight(int index) {
        handler.closeDialogue();
        switch (getElementName(index)) {
            case "Attack":
                handler.showFlavorText(false, "Power: "+BattleManager.getPlayer().getAttackPower());
                break;
            case "Guard":
                handler.showFlavorText(false, "Defense up this turn.");
                break;
            case "Skills":
                handler.showFlavorText(false, "Use a skill.");
                break;
            case "Items":
                handler.showFlavorText(false, "Use an item.");
                break;
            case "Run":
                handler.showFlavorText(false, "Success chance: 100%");
                break;
            default:
                handler.closeFlavorText();
        }
    }

    @Override
    public void onFocus() {
    }

    @Override
    public void onClose() {
        // can't close the root menu in a battle
    }
    @Override
    public void onCloseCleanup() {}
}
