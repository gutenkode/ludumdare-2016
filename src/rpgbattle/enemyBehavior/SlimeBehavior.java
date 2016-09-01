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
public class SlimeBehavior extends EnemyBehavior {
    
    private int action;
    private float deathCycle;

    public SlimeBehavior(EnemyFighter f) {
        super(f);
        performTime = 40;
        deathCycle = 1;
    }

    @Override
    public void initAct() {
        actDelay = 60;
        
        if (fighter.stats.health < fighter.stats.maxHealth/2 
            && Math.random() > .85) 
        {
            BattleUIManager.logMessage("The Slime heals itself.");
            action = 1;
        } 
        else if (!BattleManager.getPlayer().hasStatus(StatEffect.POISON)
            && Math.random() > .85) 
        {
            BattleUIManager.logMessage("The Slime spits acid!");
            action = 2;
        } 
        else if (!BattleManager.getPlayer().hasStatus(StatEffect.FATIGUE)
            && Math.random() > .85) 
        {
            BattleUIManager.logMessage("The Slime casts an oppressive aura!");
            action = 3;
        } 
        else if (Math.random() > .8) 
        {
            BattleUIManager.logMessage("The Slime eyes you carefully.");
            action = -1;
        } 
        else 
        {
            BattleUIManager.logMessage("The Slime attacks!");
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
                fighter.restoreHealth(40);
                break;
            case 2: 
                BattleManager.getPlayer().inflictStatus(StatEffect.POISON);
                break;
            case 3: 
                BattleManager.getPlayer().inflictStatus(StatEffect.FATIGUE);
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