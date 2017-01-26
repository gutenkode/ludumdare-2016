package rpgbattle.fighter;

import rpgsystem.StatEffect;

/**
 * Manages the stats for a Fighter, safely encapsulates
 * base stats and can return modified stat values.
 * Created by Peter on 1/25/17.
 */

public class FighterStats {

    private Fighter fighter;

    public int health, maxHealth,
            stamina, maxStamina,
            mana, maxMana;
    private int attack, defense, magic;
    private double evasion, critrate;
    private double[] elementMultiplier;

    public FighterStats(Fighter f,
                        int h, int s, int m,
                        int atk, int def, int mag,
                        double ev, double crit,
                        double[] emult)
    {
        fighter = f;
        health = maxHealth = h;
        stamina = maxStamina = s;
        mana = maxMana = m;
        attack = atk; defense = def; magic = mag;
        evasion = ev; critrate = crit;
        elementMultiplier = emult;
    }

    public int attack() { return attack; }
    public int defense() {
        if (fighter.hasStatus(StatEffect.DEF_UP))
            return (int)(defense*1.5);
        return defense;
    }
    public int magic() { return magic; }
    public double evasion() { return evasion; }
    public double critrate() { return critrate; }
    public double elementMultiplier(int i) { return elementMultiplier[i]; }

    public void changeBaseAtk(int amount) { attack += amount; }
    public void changeBaseDef(int amount) { defense += amount; }
}
