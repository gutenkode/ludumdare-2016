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
    private static int exitBattleDelay;
    private static boolean exitBattle;
    private static final PlayerFighter playerFighter;
    
    static {
        playerFighter = new PlayerFighter();
    }
    
    public static void initEnemies(String... enemyNames) {
        exitBattle = false;
        
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

        Battle.setBackground(EnemyData.getBattleBackground(enemyNames[0]));
        
        currentFighter = playerFighter;
        currentFighter.initAct();
    }
    
    public static void update() {
        if (exitBattle) 
        {
            exitBattleDelay--;
            if (exitBattleDelay == 0) {
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

            int ind = fighters.indexOf(currentFighter);
            ind++;
            ind %= fighters.size();
            currentFighter = fighters.get(ind);
            currentFighter.initAct();
        }
        /*
        for (Fighter f : fighters)
            if (f.statEffects.contains(StatEffect.POISON))
                f.poisonDamage();*/
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
                exitBattle = true;
                exitBattleDelay = 150;
                BattleUIManager.logMessage("You win!");
            }
        }
    }

    public static void runFromBattle() {
        exitBattle = true;
        exitBattleDelay = 75;
    }

    public static ArrayList<Fighter> getFighters() { return fighters; }
    public static ArrayList<EnemyFighter> getEnemies() { return enemies; }
    public static PlayerFighter getPlayer() { return playerFighter; }
}