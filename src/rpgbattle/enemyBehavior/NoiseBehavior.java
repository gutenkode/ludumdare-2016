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
public class NoiseBehavior extends EnemyBehavior {

    private int action;
    private float deathCycle;

    public NoiseBehavior(EnemyFighter f) {
        super(f);
        deathCycle = 1;
    }

    @Override
    public void initAct() {
        actDelay = 60;
        performActTime = 40;

        if (fighter.stats.health < fighter.stats.maxHealth/2
                && Math.random() > .5)
        {
            BattleUIManager.logMessage("The Noise lets out a piercing screech.");
            action = 1;
        }
        else if (!BattleManager.getPlayer().hasStatus(StatEffect.FATIGUE)
                && Math.random() > .85)
        {
            BattleUIManager.logMessage("The Noise casts an oppressive aura of static.");
            action = 2;
        }
        else
        {
            BattleUIManager.logMessage("The Noise shivers wildly.");
            action = 0;
        }
    }

    @Override
    void performAct() {
        switch (action) {
            case 0:
                useAttack();
                break;
            case 1:
                useAttack(2.0);
                break;
            case 2:
                BattleManager.getPlayer().inflictStatus(StatEffect.FATIGUE, 80);
                break;
        }
    }

    @Override
    public void runDeathAnimation(ModelMatrix model) {
        model.scale(deathCycle, 1, 1);
        deathCycle *= .9;
    }
}