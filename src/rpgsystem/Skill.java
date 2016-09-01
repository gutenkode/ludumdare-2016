package rpgsystem;

import java.util.ArrayList;
import rpgbattle.BattleManager;
import rpgbattle.PlayerSkills;
import rpgbattle.fighter.Fighter;
import rpgbattle.fighter.Fighter.FighterStats;
import ui.BattleUIManager;
import ui.MenuHandler;
import static rpgsystem.DefaultTarget.*;

/**
 * Special attack that can be used in battle.
 * @author Peter
 */
public enum Skill {
    FIRE("Fireball",
        "Reliable medium strength skill.", "skill_fire", "anim_ice",
        ENEMY,
        30,     100,    5),
    BOLT("Thunderbolt",
        "High power, low accuracy.", "skill_fire", "anim_ice",
        ENEMY,
        35,     80,     7),
    ICE("Ice",
        "Weak, but high chance to crit.", "skill_fire", "anim_ice",
        ENEMY,
        25,     100,    7),
    CURE("Cure",
        "Restores HP.", "skill_fire", "anim_ice",
        PLAYER,
        50,     0,      10);

    public final String name, desc, spriteName, animName;
    public final int basePower, baseAccuracy, baseCost;
    public final DefaultTarget defaultTarget;
    Skill(String n, String d, String s, String an, DefaultTarget t, int p, int a, int c) {
        name = n;
        desc = d;
        spriteName = s;
        animName = an;
        defaultTarget = t;
        basePower = p;
        baseAccuracy = a;
        baseCost = c;
    }
    
    public String getFullInfoString() {
        StringBuilder sb = new StringBuilder(desc);
        sb.append("\n");
        
        sb.append("\nPower: ").append(basePower);
        if (basePower != power())
            sb.append(" > ").append(power());
        
        sb.append("\nAccuracy: ").append(baseAccuracy);
        if (baseAccuracy != accuracy())
            sb.append(" > ").append(accuracy());
        
        sb.append("\nCost: ").append(baseCost);
        if (baseCost != cost())
            sb.append(" > ").append(cost());
        
        SkillModifier m = PlayerSkills.getLinkedModifier(this);
        if (m != null)
            sb.append("\nModifier: ").append(m.name);
        else
            sb.append("\nModifier: ---");
        return sb.toString();
    }
    
    public int power() {
        SkillModifier m = PlayerSkills.getLinkedModifier(this);
        if (m == null)
            return basePower;
        switch (m) {
            case DAMAGE_BOOST:
                return (int)(basePower * 1.5);
            case POWER_BOOST:
                return (int)(basePower * 2);
            default: 
                return basePower;
        }
    }
    public int accuracy() {
        SkillModifier m = PlayerSkills.getLinkedModifier(this);
        if (m == null)
            return baseAccuracy;
        switch (m) {
            case MULTI_TARGET:
                return (int)(baseAccuracy*.75);
            case ACCURACY:
                return Math.min(baseAccuracy * 2, 100);
            default: 
                return baseAccuracy;
        }
    }
    public int cost() {
        SkillModifier m = PlayerSkills.getLinkedModifier(this);
        if (m == null)
            return baseCost;
        switch (m) {
            case EFFICIENCY:
                return baseCost/2;
            case POWER_BOOST:
                return baseCost*2;
            default:
                return baseCost;
        }
    }
    
    public void useIngame(MenuHandler handler) {
        switch (this) {
            case CURE:
                FighterStats stats = BattleManager.getPlayer().stats;
                if (stats.health == stats.maxHealth) {
                    handler.showDialogue("Your health is full!", spriteName);
                } else if (cost() > stats.mana) {
                    handler.showDialogue("You don't have enough mana!", spriteName);
                } else {
                    BattleManager.getPlayer().drainMana(cost());
                    BattleManager.getPlayer().restoreHealth(power());
                    handler.showDialogue("You regain health.", spriteName);
                }
                break;
            default:
                handler.showDialogue("You can't use this here.", spriteName);
                break;
        }
    }
    
    public void useBattle(MenuHandler handler, int magicStat, Fighter... targets) {
        // TODO: check for MUTLI_TARGET modifier
        switch (this) {
            case FIRE:
                BattleUIManager.logMessage("You cast Fireball!");
                for (Fighter f : targets)
                    f.damage(Element.FIRE, magicStat, power(), accuracy(), false);
                break;
            case BOLT:
                BattleUIManager.logMessage("You cast Thunderbolt!");
                for (Fighter f : targets)
                    f.damage(Element.ELEC, magicStat, power(), accuracy(), false);
                break;
            case ICE:
                BattleUIManager.logMessage("You cast Ice!");
                for (Fighter f : targets) {
                    boolean crit = Math.random() > .35;
                    f.damage(Element.ICE, magicStat, power(), accuracy(), crit);
                }
                break;
            case CURE:
                BattleUIManager.logMessage("You cast Cure!");
                for (Fighter f : targets)
                    f.restoreHealth(power());
                break;
            default:
                handler.showDialogue("You can't use this here.");
        }
    }
}