package rpgbattle.enemyBehavior;

import rpgbattle.BattleManager;
import rpgbattle.battleAction.AttackAction;
import rpgbattle.battleAction.BattleAction;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.Element;
import ui.components.BattleAnimation;

/**
 * Contains enemy behavior for battles.  Determines when enemies use which attacks.
 * Since enemies need to run arbitrary code for behavior and external scripts
 * are not an option, this will do for now.
 * @author Peter
 */
public abstract class EnemyBehavior {
    
    EnemyFighter fighter;
    //int actDelay = 0, // used as a clock for determining sequence of events
    //    performActTime = 0; // the time at which performAct() will be called

    public EnemyBehavior(EnemyFighter f) {
        fighter = f;
    }

    /**
     * Use a basic attack, with this fighter's base power and crit rate.
     */
    public void useAttack() {
        useAttack(BattleAction.STD_ACTION_DELAY);
    }
    public void useAttack(int delay) {
        int power = (int)(Math.random()*5)+7;
        BattleManager.addAction(new AttackAction(BattleManager.getPlayer(), fighter.stats.attack(), power, delay));
    }

    /**
     * Use a basic attack, but scale the base attack stat.
     * @param attackMult Scale for attack stat, 1 is regular power, 2 is double power, etc.
     */
    public void useAttack(Element e, BattleAnimation an, double attackMult, int delay) {
        int power = (int)(10*attackMult);
        BattleManager.addAction(new AttackAction(BattleManager.getPlayer(), e, an, fighter.stats.attack(), power, 100, false, delay));
    }
    
    /**
     * Called when the enemy turn starts.  This is the same frame as the 
     * "turn start" flash, and this method should print to the log the action
     * that is about to happen.
     * This method should generally also create any BattleActions needed.
     * @return Delay time, in frames, before calling act()
     */
    public abstract int initAct();

    /**
     * Called repeatedly while -1 is returned, until a number >= 0 is returned.
     * @return Delay time, in frames, before ending the turn for this enemy.
     */
    public abstract int act();
}