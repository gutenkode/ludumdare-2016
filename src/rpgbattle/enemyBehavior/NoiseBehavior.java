package rpgbattle.enemyBehavior;

import rpgbattle.BattleManager;
import rpgbattle.battleAction.BattleAction;
import rpgbattle.battleAction.BuffAction;
import rpgbattle.battleAction.StatusAction;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.Element;
import rpgsystem.StatusEffect;
import ui.BattleUIManager;

/**
 *
 * @author Peter
 */
public class NoiseBehavior extends EnemyBehavior {

    public NoiseBehavior(EnemyFighter f) {
        super(f);
    }

    @Override
    public int initAct() {
        if (fighter.stats.health < fighter.stats.maxHealth
                && Math.random() > .5)
        {
            BattleUIManager.logMessage("The Noise lets out a piercing screech.");
            useAttack(Element.PHYS, null, 2.0, 10);
            BattleManager.addAction(new BuffAction(BattleManager.getPlayer(), BuffAction.Stat.DEF, -1));
        }
        else if (!BattleManager.getPlayer().hasStatus(StatusEffect.FATIGUE)
                && Math.random() > .85)
        {
            BattleUIManager.logMessage("The Noise casts an oppressive aura of static.");
            BattleManager.addAction(new StatusAction(BattleManager.getPlayer(), StatusEffect.FATIGUE, 80));
        }
        else
        {
            BattleUIManager.logMessage("The Noise shivers wildly.");
            useAttack(40);
        }
        return BattleAction.STD_INIT_DELAY;
    }

    @Override
    public int act() { return 0; }
}