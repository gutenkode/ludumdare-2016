package rpgbattle;

import mote4.util.FileIO;
import org.json.*;
import rpgbattle.enemyBehavior.*;
import rpgbattle.fighter.EnemyFighter;
import rpgbattle.fighter.Fighter;

/**
 * Provides a blueprint for initializing an EnemyFighter, as well as the sprite
 * and behavior of an Enemy in the overworld.
 * @author Peter
 */
public class EnemyData {
    private static final JSONObject json;
    static {
        json = new JSONObject(FileIO.readFile("/res/files/enemies.json"));
    }
    
    public static String getIngameSprite(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("ingame").getString("sprite");
    }
    public static String getBattleSprite(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("battle").getString("sprite");
    }
    /**
     * The message to display at the star of a battle when encountering this enemy.
     * @param enemyName
     * @return 
     */
    public static String getEncounterString(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("battle").getString("encounter");
    }
    public static String getDeathString(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("battle").getString("death");
    }
    public static String getDisplayName(String enemyName) {
        return json.getJSONObject(enemyName).getString("name");
    }
    /**
     * The time to spend on each frame of an enemy's battle animation.
     * @param enemyName
     * @return 
     */
    public static int[] getFrameDelay(String enemyName) {
        JSONArray arr = json.getJSONObject(enemyName).getJSONObject("battle").getJSONArray("spriteFrames");
        int[] result = new int[arr.length()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = arr.getInt(i);
        }
        return result;
    }
    
    public static void populateStats(String enemyName, Fighter.FighterStats stats) {
        JSONObject enemyJson = json.getJSONObject(enemyName);
        
        stats.health = stats.maxHealth = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("health");
        stats.attack = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("attack");
        stats.defense = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("defense");
        stats.magic = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("magic");
        stats.evasion = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("evasion");
        stats.critrate = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("critrate");
        
        stats.elementMultiplier = new double[5];
        JSONArray arr = enemyJson.getJSONObject("battle").getJSONArray("elementMult");
        for (int i = 0; i < 5; i++)
        {
            stats.elementMultiplier[i] = arr.getDouble(i);
        }
    }
    
    public static float[][] getPatrol(String enemyName, float x, float y) {
        switch (enemyName) {
            case "SLIME":
                return new float[][] {
                    {x,y,0,0},
                    {x-1,y-.5f,2,1},
                    {x,y,0,0},
                    {x,y+1,2,3},
                };
            case "NOISE":
                return new float[][] {
                    {x-3,y,0,2},
                    {x+3,y,0,2},
                };
            case "BOSS":
                return new float[][] {
                    {x,y,0,10}
                };
            default:
                return new float[][] {
                    {x-1,y,0,2},
                    {x,y+1,0,2},
                    {x+1,y,0,2},
                    {x,y-1,0,2}
                };
                //throw new IllegalStateException();
        }
    }
    
    public static EnemyBehavior getBehavior(EnemyFighter fighter) {
        switch (fighter.enemyName) {
            case "SLIME":
            case "SLIME1":
            case "SLIME2":
            case "SLIME3":
            case "SLIME4":
                return new SlimeBehavior(fighter);
            case "BGINR1":
                return new BGinR1Behavior(fighter);
            case "BGINR2":
                return new BGinR2Behavior(fighter);
            case "MIRROR1":
            case "MIRROR2":
                return new MirrorManBehavior(fighter);
            case "VILEDOOR1":
            case "VILEDOOR2":
                return new EvilDoorBehavior(fighter);
            case "BARREL1":
            case "BARREL2":
            case "BARREL3":
                return new ExplodingBarrelBehavior(fighter);
            case "VILEDRINK1":
            case "VILEDRINK2":
                return new PoisonedDrinkBehavior(fighter);
            case "NOISE":
            //    return null;
            case "BOSS":
            //    return null;
            default:
                throw new IllegalStateException();
        }
    }
}
