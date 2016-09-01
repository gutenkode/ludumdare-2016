package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.Element;
import rpgsystem.StatEffect;
import ui.BattleUIManager;

/**
 *
 * @author Peter
 */
public class ExplodingBarrelBehavior extends EnemyBehavior {
    
    private int action;
    private float deathCycle;
    
    public ExplodingBarrelBehavior(EnemyFighter f){
        super(f);
        performTime = 40;
        deathCycle = 1;
    }
    
    @Override
    public void initAct() {
        actDelay = 60;
        if (Math.random() > .90 || fighter.stats.health < fighter.stats.maxHealth/10){
            BattleUIManager.logMessage("The barrel explodes!");
            action = 0;
        }
        else if (Math.random() > .25){
            BattleUIManager.logMessage("The barrel is very careful to attack softly.");
            action = 1;
        }
        else{
            BattleUIManager.logMessage("The barrel doesn't believe in itself...");
            action = -1;
        }
    }
    @Override
    void performAct() {
        switch (action) {
            case 0:
                BattleManager.getPlayer().damage(Element.FIRE, 10, 80, 100, false);
                fighter.stats.health = 0;
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