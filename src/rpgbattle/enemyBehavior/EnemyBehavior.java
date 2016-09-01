package rpgbattle.enemyBehavior;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.Element;

/**
 * Contains enemy behavior for battles.  Determines when enemies use which attacks.
 * Since enemies need to run arbitrary code for behavior and external scripts
 * are not an option, this will do for now.
 * @author Peter
 */
public abstract class EnemyBehavior {
    
    EnemyFighter fighter;
    int actDelay, 
        performTime; // the time at which performAct() will be called
    
    public EnemyBehavior(EnemyFighter f) {
        fighter = f;
        actDelay = 0;
    }
    
    public void useAttack() {
        int power = (int)(Math.random()*5)+7;
        BattleManager.getPlayer().damage(Element.PHYS, fighter.stats.attack, power, 100, false);
    }
    
    /**
     * Called when the enemy turn starts.  This is the same frame as the 
     * "turn start" flash, and this method should print to the log the action
     * that is about to happen.
     */
    public abstract void initAct();
    public final boolean act() {
        if (actDelay == performTime)
            performAct();
        actDelay--;
        return actDelay <= 0;
    }
    /**
     * The action decided on in initAct() should be performed when this is called.
     */
    abstract void performAct();
    
    /**
     * Will process the model matrix to apply any animations upon death for
     * this enemy.
     * @param model
     */
    public abstract void runDeathAnimation(ModelMatrix model);
}