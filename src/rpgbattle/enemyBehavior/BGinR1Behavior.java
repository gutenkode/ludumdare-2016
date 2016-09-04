package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.StatEffect;
import ui.BattleUIManager;

/**
 *
 * @author Chance
 */
public class BGinR1Behavior extends EnemyBehavior {
    
    private float deathCycle;
    
    public BGinR1Behavior(EnemyFighter f) {
        super(f);
        deathCycle = 1;
    }
    
    @Override
    public void initAct() {
        actDelay = 50;
        BattleUIManager.logMessage("B. Gin R. flails wildly!\n...But nothing happens!");
    }

    @Override
    void performAct() {}

    @Override
    public void runDeathAnimation(ModelMatrix model) {
        model.translate(0, (1-deathCycle)*96);
        model.scale(1, deathCycle, 1);
        //if (deathCycle > .1)
            deathCycle *= .95;
    }
}