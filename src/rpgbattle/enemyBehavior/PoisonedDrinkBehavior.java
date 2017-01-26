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
public class PoisonedDrinkBehavior extends EnemyBehavior {
    private int action;
    private float deathCycle;
    
    public PoisonedDrinkBehavior(EnemyFighter f){
        super(f);
        performActTime = 40;
        deathCycle = 1;
    }
    
    @Override
    public void initAct() {
        actDelay = 60;
        if ((!BattleManager.getPlayer().hasStatus(StatEffect.FATIGUE) || !BattleManager.getPlayer().hasStatus(StatEffect.POISON)) && fighter.stats.health > fighter.stats.maxHealth/4){
            BattleUIManager.logMessage("The drink force-feeds itself to you!");
            action = 0;
        }
        else if (Math.random() > .25){
            BattleUIManager.logMessage("The drink laughs at your suffering!");
            action = -1;
        }
        else{
            BattleUIManager.logMessage("The drink kicks you while you're down!");
            action = 1;
        }
    }
    
    @Override
    void performAct() {
        switch (action) {
            case 0:
                BattleManager.getPlayer().inflictStatus(StatEffect.POISON, 80);
                BattleManager.getPlayer().inflictStatus(StatEffect.FATIGUE, 80);
                fighter.stats.health -= fighter.stats.maxHealth/4;
                break;
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
