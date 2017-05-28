package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.StatEffect;
import ui.BattleUIManager;

/**
 *
 * @author Peter
 */
public class BossBehavior extends EnemyBehavior {

    private int action;
    private float deathCycle;

    public BossBehavior(EnemyFighter f) {
        super(f);
        deathCycle = 1;
    }

    @Override
    public int initAct() {

        if (fighter.stats.health < fighter.stats.maxHealth/2
                && Math.random() > .75)
        {
            BattleUIManager.logMessage("They use a potion.");
            action = 1;
        }
        else if (Math.random() > .8)
        {
            BattleUIManager.logMessage("A piercing stare.");
            action = -1;
        }
        else  if (Math.random() > .8)
        {
            BattleUIManager.logMessage("They throw a grenade.");
            action = 2;
        }
        else
        {
            BattleUIManager.logMessage("They attack.");
            action = 0;
        }

        return 40;
    }

    @Override
    public int act() {
        switch (action) {
            case 0:
                useAttack();
                return 40;
            case 1:
                fighter.restoreHealth(40);
                return 40;
            case 2:
                useAttack(1.5);
                return 40;
            default:
                return 0;
        }
    }

    @Override
    public void runDeathAnimation(ModelMatrix model) {
        model.translate(0, (1-deathCycle)*96);
        model.scale(1, deathCycle, 1);
        //if (deathCycle > .1)
        deathCycle *= .95;
    }
}