package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.battleAction.BattleAction;
import rpgbattle.battleAction.BuffAction;
import rpgbattle.battleAction.StatusAction;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.StatusEffect;
import ui.BattleUIManager;

/**
 *
 * @author Peter
 */
public class SlimeBehavior extends EnemyBehavior {

    public SlimeBehavior(EnemyFighter f) {
        super(f);
    }

    @Override
    public int initAct() {
        if (fighter.stats.getDefBuff() < 2
            && Math.random() > .6)
        {
            BattleUIManager.logMessage("The Slime congeals on the floor.");
            BattleManager.addAction(new BuffAction(fighter, BuffAction.Stat.DEF, 1));
        } 
        else if (!BattleManager.getPlayer().hasStatus(StatusEffect.POISON)
            && Math.random() > .85) 
        {
            BattleUIManager.logMessage("The Slime spits acid!");
            BattleManager.addAction(new StatusAction(BattleManager.getPlayer(), StatusEffect.POISON, 80));
        }
        else if (Math.random() > .8)
        {
            BattleUIManager.logMessage("The Slime eyes you carefully.");
            return BattleAction.STD_INIT_DELAY+BattleAction.STD_ACTION_DELAY;
        } 
        else 
        {
            BattleUIManager.logMessage("The Slime attacks!");
            useAttack();
        }
        return BattleAction.STD_INIT_DELAY;
    }

    @Override
    public int act() { return 0; }
}