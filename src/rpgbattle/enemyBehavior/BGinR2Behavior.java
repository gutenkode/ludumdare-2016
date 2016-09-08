package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.fighter.EnemyFighter;
import ui.BattleUIManager;

/**
 *
 * @author Peter
 */
public class BGinR2Behavior extends EnemyBehavior {
    private int action;
    private boolean hasRaised;
    private float deathCycle;
    
    public BGinR2Behavior(EnemyFighter f){
        super(f);
        performActTime = 40;
        hasRaised = false;
        deathCycle = 1;
    }
    
    @Override
    public void initAct() {
        actDelay = 60;
        if (hasRaised){
            hasRaised = false;
            fighter.stats.defense -= 1;
        }
        if (Math.random() > (fighter.stats.health/fighter.stats.maxHealth)){
            BattleUIManager.logMessage("B. Gin R. stands his ground, fearing the end!");
            action = 0;
        }
        else{
            BattleUIManager.logMessage("B. Gin R. flails more carefully.");
            action = 1;
        }
    }
    @Override
    void performAct() {
        switch (action) {
            case 0:
                fighter.stats.defense += 1;
                hasRaised = true;
            case 1:
                useAttack();
                break;
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