package rpgbattle;

import mote4.util.FileIO;
import org.json.*;
import rpgbattle.enemyBehavior.*;
import rpgbattle.fighter.EnemyFighter;
import rpgbattle.fighter.Fighter;
import rpgbattle.fighter.FighterStats;
import rpgsystem.Element;

import java.util.HashMap;
import java.util.IllegalFormatException;

/**
 * Provides a blueprint for initializing an EnemyFighter, as well as the sprite
 * and behavior of an Enemy in the overworld.
 * @author Peter
 */
public class EnemyData {
    private static final JSONObject json;
    static {
        json = new JSONObject(FileIO.getString("/res/files/enemies.json"));
    }
    
    public static String getIngameSprite(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("ingame").getString("sprite");
    }
    public static String getBattleSprite(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("battle").getString("sprite");
    }
    public static String getBattleBackground(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("battle").getString("background");
    }
    public static int getMaxNumEnemies(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("battle").getInt("maxEnemies");
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
    public static String getBattleMusic(String enemyName) {
        return json.getJSONObject(enemyName).getJSONObject("battle").getString("music");
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

    /**
     * Populate and return a FighterStats object with the stats of this enemy.
     * @param enemyName
     * @return
     */
    public static FighterStats populateStats(String enemyName, Fighter f) {
        JSONObject enemyJson = json.getJSONObject(enemyName);
        int h = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("health");
        int atk = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("attack");
        int def = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("defense");
        int mag = enemyJson.getJSONObject("battle").getJSONObject("stats").getInt("magic");

        // build the elemental resistances hashmap
        // currently quite messy
        HashMap<Element, Element.Resistance> emult = new HashMap<>();
        JSONObject elements = enemyJson.getJSONObject("battle").getJSONObject("element");
        for (String key : elements.keySet()) {
            String val = elements.getString(key);
            emult.put(Element.valueOf(key), Element.Resistance.valueOf(val));
        }

        FighterStats stats = new FighterStats(f,
                h,0,0,
                atk,def,mag,
                emult);
        return stats;
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
                /*
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
            */
            case "NOISE":
                return new NoiseBehavior(fighter);
            case "BOSS":
                return new BossBehavior(fighter);
            default:
                throw new IllegalStateException();
        }
    }
}
