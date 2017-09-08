package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.battleAction.BattleAction;
import rpgbattle.battleAction.BuffAction;
import rpgbattle.battleAction.HealAction;
import rpgbattle.battleAction.ScriptAction;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.Element;
import ui.BattleUIManager;
import ui.components.BattleAnimation;

/**
 *
 * @author Peter
 */
public class BossBehavior extends EnemyBehavior {

    private int numPotions, numGrenades;
    private boolean scriptTriggered;

    public BossBehavior(EnemyFighter f) {
        super(f);
        numPotions = 2;
        numGrenades = 3;
        scriptTriggered = false;
    }

    @Override
    public int initAct() {

        if (!scriptTriggered && fighter.stats.health <= fighter.stats.maxHealth*.4) {
            scriptTriggered = true;
            BattleManager.addAction(new ScriptAction("bossFight1"));
            return BattleAction.STD_INIT_DELAY;
        }

        if (numPotions > 0 && fighter.stats.health < fighter.stats.maxHealth/2
                && Math.random() > .75)
        {
            numPotions--;
            BattleUIManager.logMessage("They use a potion.");
            BattleManager.addAction(new HealAction(fighter, 60));
        }
        else if (numGrenades > 0 && Math.random() > .8)
        {
            numGrenades--;
            BattleUIManager.logMessage("They throw a grenade.");
            useAttack(Element.BOMB, new BattleAnimation(BattleAnimation.Type.FIRE), 2, BattleAction.STD_ACTION_DELAY);
        }
        else if (fighter.stats.getAtkBuff() < 1
                && Math.random() > .8)
        {
            BattleUIManager.logMessage("They charge up for an attack.");
            BattleManager.addAction(new BuffAction(fighter, BuffAction.Stat.ATK, 2));
        }
        else
        {
            if (fighter.stats.getAtkBuff() > 0) { // if attack is boosted, lower attack by one stage
                BattleUIManager.logMessage("They lunge wildly!");
                useAttack(15);
                BattleManager.addAction(new BuffAction(fighter, BuffAction.Stat.ATK, -1));
            } else {
                BattleUIManager.logMessage("They attack.");
                useAttack();
            }
        }
        return BattleAction.STD_INIT_DELAY;
    }

    @Override
    public int act() { return 0; }
}