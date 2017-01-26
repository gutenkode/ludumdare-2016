package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.StatEffect;
import ui.BattleUIManager;

/**
 *
 * @author Peter and Chance
 */

public class EvilDoorBehavior extends EnemyBehavior {
    private int action;
    private float deathCycle;
    
    public EvilDoorBehavior(EnemyFighter f){
        super(f);
        performActTime = 40;
        deathCycle = 1;
    }
    
    @Override
    public void initAct() {
        actDelay = 60;
        if (!BattleManager.getPlayer().hasStatus(StatEffect.FATIGUE) && Math.random() > 0.50){
            BattleUIManager.logMessage("The door opens and closes very rapidly!");
            action = 0;
        }
        else if (Math.random() > .85){
            BattleUIManager.logMessage("The door just looks at you, waiting.");
            action = -1;
        }
        else if (Math.random() > .7){
            BattleUIManager.logMessage("The door falls on you.");
            action = 1;
        }
        else{
            BattleUIManager.logMessage("The door slams itself on your hand.");
            action = 2;
        }
    }
    
    @Override
    void performAct() {
        switch (action) {
            case 0:
                BattleManager.getPlayer().inflictStatus(StatEffect.FATIGUE, 80);
                break;
            case 1:
                useAttack(); //Yes, I am aware that there is no break there. There shouldn't be one.
            case 2:
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