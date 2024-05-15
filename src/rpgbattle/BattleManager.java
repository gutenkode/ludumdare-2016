package rpgbattle;

import entities.Enemy;
import mote4.scenegraph.Window;
import mote4.util.audio.AudioPlayback;
import main.RootLayer;
import rpgbattle.battleAction.BattleAction;
import rpgbattle.fighter.Fighter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import rpgbattle.fighter.EnemyFighter;
import rpgbattle.fighter.PlayerFighter;
import rpgsystem.StatusEffect;
import scenes.Battle;
import scenes.Postprocess;
import ui.BattleUIManager;

/**
 * Root logic handler for battles.
 * @author Peter
 */
public class BattleManager {
    
    private static ArrayList<Fighter> fighters; // all fighters, currently just the enemy list + the player
    private static Fighter currentFighter;
    private static ArrayList<EnemyFighter> enemies, removeEnemies; // all enemy fighters
    private static double stateDelay;
    private static BattleState currentState;
    private static final PlayerFighter playerFighter;
    private static final Queue<BattleAction> actions;

    private enum BattleState {
        START_BATTLE,   // actions to perform at the start of a battle
        START_TURN,     // actions to perform at the start of a fighter's turn
        FIGHTER_TURN,   // actions to perform during a fighter's turn
        ACTION,         // BattleActions created during the fighter's turn, to be finished before the end of the turn
        END_TURN,       // actions to perform at the end of a fighter's turn
        END_BATTLE,     // actions to perform at the end of a battle
        PLAYER_LOOSE;   // actions to perform when the player hits 0 HP
    }
    
    static {
        playerFighter = new PlayerFighter();
        actions = new LinkedList<>();
    }

    /**
     * Calling this method with a list of enemy names will start a battle.
     * This is the root method for starting the battle load procedure.
     * @param enemyNames List of enemy names as Strings to fight.
     */
    public static void startBattle(String... enemyNames) {
        AudioPlayback.playSfx("sfx_battle_start");
        // RootScene -> go to battle
        RootLayer.getInstance().transitionToBattle();

        fighters = new ArrayList<>();
        enemies = new ArrayList<>();
        removeEnemies = new ArrayList<>();
        
        fighters.add(playerFighter);
        
        for (String s : enemyNames) {
            EnemyFighter enemy = new EnemyFighter(s);
            fighters.add(enemy);
            enemies.add(enemy);
            Battle.setEnemies(enemies);
            BattleUIManager.initEnemies(enemies);
            BattleUIManager.logMessage(enemy.encounterString);
        }

        // TODO: sort fighters list by speed stat

        Battle.setBackground(EnemyData.getBattleBackground(enemyNames[0]));

        // initialize the state for the battle
        currentState = BattleState.START_BATTLE;
        stateDelay = 0;
    }
    
    public static void update() {
        removeDeadEnemies();
        int val;
        if (stateDelay > 0) // global delay for advancing to the next state
            stateDelay -= Window.delta()*60;
        else {
            switch (currentState) {
                case START_BATTLE:
                    currentFighter = playerFighter;
                    currentState = BattleState.START_TURN;
                    break;
                case START_TURN:
                    if (currentFighter == playerFighter && areAllEnemiesDead()) {
                        // win state
                        currentState = BattleState.END_BATTLE;
                        stateDelay = 100;
                        BattleUIManager.logMessage("You win!");
                    } else {
                        Battle.lookAtEnemy(enemies.indexOf(currentFighter), false);
                        actions.clear();
                        stateDelay = currentFighter.initAct();
                        currentState = BattleState.FIGHTER_TURN;
                    }
                    break;
                case FIGHTER_TURN:
                    /*
                     Call act() on the current fighter until it returns
                     a value to be the delay until the next state.
                     Generally speaking, BattleActions should be added
                     to the queue during this step.
                     */
                    val = currentFighter.act();
                    if (val != -1) {
                        stateDelay = val;
                        currentState = BattleState.ACTION;
                    }
                    break;
                case ACTION:
                    // perform all actions until the queue is empty
                    if (actions.isEmpty())
                        currentState = BattleState.END_TURN;
                    else {
                        val = actions.peek().act();
                        if (val != -1) { // if we have a delay from an action, apply it
                            stateDelay = val;
                            actions.poll();
                        }
                    }
                    break;
                case END_TURN:
                    // poison damage happens at the end of a turn
                    if (currentFighter.statusEffects.contains(StatusEffect.POISON)) {
                        if (currentFighter.equals(playerFighter) && areAllEnemiesDead()) {
                            // player doesn't take poison damage at the end of their turn if all enemies are dead
                        } else {
                            currentFighter.poisonDamage();
                            stateDelay = 50;
                        }
                    }

                    if (playerFighter.stats.health <= 0) {
                        // the player ran out of HP, game over...
                        BattleUIManager.logMessage("You lost...");
                        currentState = BattleState.PLAYER_LOOSE;
                    } else {
                        // advance to the next fighter in the list
                        advanceToNextFighter();

                        currentState = BattleState.START_TURN;
                    }
                    break;
                case END_BATTLE:
                    Postprocess.fadeOut(RootLayer::exitBattle);
                    playerFighter.statusEffects.clear(); // remove status effects after battle
                    playerFighter.stats.resetBuffs();  // reset stats after battle
                    break;
                case PLAYER_LOOSE:
                    // do nothing for now
                    break;
            }
        }
    }
    private static void advanceToNextFighter() {
        do {
            int ind = fighters.indexOf(currentFighter);
            ind++;
            ind %= fighters.size();
            currentFighter = fighters.get(ind);
            // skip any dead fighters, but to prevent infinite loops don't skip the player
        } while (currentFighter.isDead() && currentFighter != playerFighter);
    }
    public static boolean areAllEnemiesDead() {
        for (EnemyFighter f : enemies)
            if (!f.isDead())
                return false;
        return true;
    }
    public static void removeDeadEnemies() {
        // remove enemies that have finished their animations
        for (int i = 0; i < removeEnemies.size(); i++) {
            EnemyFighter f = removeEnemies.get(i);
            if (!f.isDead())
                throw new IllegalStateException();
            if (!f.hasAnimationsLeft() && !f.hasToastsLeft()) {
                removeEnemies.remove(i);
                enemies.remove(f);
                fighters.remove(f);
                i--;
            }
        }
    }

    public static void addAction(BattleAction a) {
        actions.add(a);
    }
    /**
     * When an EnemyFighter's HP hits zero, it will call this function.
     * @param f 
     */
    public static void enemyDied(EnemyFighter f) {
        if (enemies.contains(f)) 
        {
            if (f == currentFighter) {
                advanceToNextFighter();
            }
            removeEnemies.add(f);
        }
    }
    /**
     * End the current battle instantly. No run percentage is performed here -
     * this function ends the battle upon a successful escape.
     */
    public static void runFromBattle() {
        currentState = BattleState.END_BATTLE;
        stateDelay = 75;
    }

    public static ArrayList<Fighter> getFighters() { return fighters; }
    public static ArrayList<EnemyFighter> getEnemies() { return enemies; }
    public static PlayerFighter getPlayer() { return playerFighter; }
}