package rpgbattle.fighter;

import rpgsystem.Element;

import java.util.HashMap;

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
    private int atkBuf, defBuf, magBuf;
    private HashMap<Element,Element.Resistance> elementMultiplier;

    public FighterStats(Fighter f,
                        int h, int s, int m,
                        int atk, int def, int mag,
                        HashMap<Element,Element.Resistance> emult)
    {
        fighter = f;
        health = maxHealth = h;
        stamina = maxStamina = s;
        mana = maxMana = m;
        attack = atk; defense = def; magic = mag;
        elementMultiplier = emult;
    }

    public int baseAttack() { return attack; }
    public int attack() {
        if (atkBuf > 0)
            return (int)(attack * (1+atkBuf*.25)); // up to 2x
        else if (atkBuf < 0)
            return (int)(attack * (1-atkBuf*.125)); // down to 0.5x
        return attack;
    }
    public int baseDefense() { return defense; }
    public int defense() {
        if (defBuf > 0)
            return (int)(defense * (1+defBuf*.25)); // up to 2x
        else if (defBuf < 0)
            return (int)(defense * (1-defBuf*.125)); // down to 0.5x
        return defense;
    }
    public int baseMagic() { return magic; }
    public int magic() {
        if (magBuf > 0)
            return (int)(magic * (1+magBuf*.25)); // up to 2x
        else if (magBuf < 0)
            return (int)(magic * (1-magBuf*.125)); // down to 0.5x
        return magic;
    }

    public Element.Resistance elementResistance(Element e) {
        Element.Resistance r = elementMultiplier.get(e);
        if (r == null)
            return Element.Resistance.N;
        return r;
    }

    public void resetBuffs() {
        atkBuf = defBuf = magBuf = 0;
    }
    public void buffAtk(int amount) {
        int lastAtk = atkBuf;
        atkBuf = Math.min(4, Math.max(-4, atkBuf+amount));
        if (atkBuf > lastAtk)
            fighter.addToast("ATK UP");
        else if (atkBuf < lastAtk)
            fighter.addToast("ATK DOWN");
        else
            fighter.addToast("NO EFFECT");

    }
    public void buffDef(int amount) {
        int lastDef = defBuf;
        defBuf = Math.min(4, Math.max(-4, defBuf+amount));
        if (defBuf > lastDef)
            fighter.addToast("DEF UP");
        else if (defBuf < lastDef)
            fighter.addToast("DEF DOWN");
        else
            fighter.addToast("NO EFFECT");
    }
    public void buffMag(int amount) {
        int lastMag = magBuf;
        magBuf = Math.min(4, Math.max(-4, magBuf+amount));
        if (magBuf > lastMag)
            fighter.addToast("MAG UP");
        else if (magBuf < lastMag)
            fighter.addToast("MAG DOWN");
        else
            fighter.addToast("NO EFFECT");
    }
    public int getAtkBuff() { return atkBuf; }
    public int getDefBuff() { return defBuf; }
    public int getMagBuff() { return magBuf; }


    public void changeBaseAtk(int amount) { attack += amount; }
    public void changeBaseDef(int amount) { defense += amount; }
}
