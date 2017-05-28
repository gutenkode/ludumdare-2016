package rpgbattle;

import nullset.RootLayer;
import rpgbattle.fighter.Fighter;
import java.util.ArrayList;
import rpgbattle.fighter.EnemyFighter;
import rpgbattle.fighter.PlayerFighter;
import rpgsystem.StatEffect;
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
    private static ArrayList<EnemyFighter> enemies; // all enemy fighters
    private static int stateDelay;
    private static BattleState currentState;
    private static final PlayerFighter playerFighter;

    private enum BattleState {
        START_BATTLE,
        START_TURN,
        FIGHTER_TURN,
        END_TURN,
        END_BATTLE;
    }
    
    static {
        playerFighter = new PlayerFighter();
    }
    
    public static void initEnemies(String... enemyNames) {
        fighters = new ArrayList<>();
        enemies = new ArrayList<>();
        
        fighters.add(playerFighter);
        
        for (String s : enemyNames) {
            EnemyFighter enemy = new EnemyFighter(s);
            fighters.add(enemy);
            enemies.add(enemy);
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
        if (stateDelay > 0) // global delay for advancing to the next state
            stateDelay--;
        else {
            switch (currentState) {
                case START_BATTLE: // actions to perform at the start of a battle
                    currentFighter = playerFighter;
                    currentState = BattleState.START_TURN;
                    break;
                case START_TURN: // actions to perform at the start of a fighter's turn
                    stateDelay = currentFighter.initAct();
                    currentState = BattleState.FIGHTER_TURN;
                    break;
                case FIGHTER_TURN: // actions to perform during a fighter's turn
                    // call act() on the current fighter until it returns
                    // a value to be the delay until the next state
                    int val = currentFighter.act();
                    if (val != -1) {
                        stateDelay = val;
                        currentState = BattleState.END_TURN;
                    }
                    break;
                case END_TURN: // actions to perform at the end of a fighter's turn
                    // poison damage happens at the end of a turn
                    if (currentFighter.statEffects.contains(StatEffect.POISON)) {
                        currentFighter.poisonDamage();
                        stateDelay = 60;
                    }

                    // advance to the next fighter in the list
                    int ind = fighters.indexOf(currentFighter);
                    ind++;
                    ind %= fighters.size();
                    currentFighter = fighters.get(ind);

                    currentState = BattleState.START_TURN;
                    break;
                case END_BATTLE: // actions to perform at the end of a battle
                    Postprocess.fadeOut(RootLayer::exitBattle);
                    playerFighter.statEffects.clear(); // remove status effects after battle
                    break;
            }
        }

        /*
        if (exitBattle) 
        {
            stateDelay--;
            if (stateDelay == 0) {
                Postprocess.fadeOut(RootLayer::exitBattle);
                playerFighter.statEffects.clear(); // remove status effects after battle
            }
                //RootScene.setState(RootScene.State.INGAME);
        }
        else if (currentFighter.act())
        {
            // poison damage happens at the end of a turn
            if (currentFighter.statEffects.contains(StatEffect.POISON))
                currentFighter.poisonDamage();

            // advance to the next fighter in the list
            int ind = fighters.indexOf(currentFighter);
            ind++;
            ind %= fighters.size();
            currentFighter = fighters.get(ind);
            currentFighter.initAct();
        }*/
    }
    
    /**
     * When an EnemyFighter's HP hits zero, it will call this function.
     * @param f 
     */
    public static void enemyDied(EnemyFighter f) {
        if (enemies.contains(f)) 
        {
            BattleUIManager.logMessage(f.deathString);
            
            if (f == currentFighter) {
                int ind = fighters.indexOf(currentFighter);
                ind++;
                ind %= fighters.size();
                currentFighter = fighters.get(ind);
                currentFighter.initAct();
            }
            
            enemies.remove(f);
            fighters.remove(f);
            
            if (enemies.isEmpty()) {
                currentState = BattleState.END_BATTLE;
                stateDelay = 150;
                BattleUIManager.logMessage("You win!");
            }
        }
    }

    public static void runFromBattle() {
        currentState = BattleState.END_BATTLE;
        stateDelay = 75;
    }

    public static ArrayList<Fighter> getFighters() { return fighters; }
    public static ArrayList<EnemyFighter> getEnemies() { return enemies; }
    public static PlayerFighter getPlayer() { return playerFighter; }
}