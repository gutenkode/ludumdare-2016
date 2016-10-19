package rpgsystem;

import java.util.ArrayList;
import rpgbattle.BattleManager;
import rpgbattle.PlayerSkills;
import rpgbattle.fighter.Fighter;
import rpgbattle.fighter.Fighter.FighterStats;
import ui.BattleUIManager;
import ui.MenuHandler;
import ui.components.BattleAnimation;

import static rpgsystem.DefaultTarget.*;

/**
 * Special attack that can be used in battle.
 * @author Peter
 */
public enum Skill {
    FIRE("Fireball",
        "Reliable medium strength skill.", "skill_fire",
        ENEMY, BattleAnimation.Type.FIRE,
        30,     100,    5),
    BOLT("Thunderbolt",
        "High power, low accuracy.", "skill_fire",
        ENEMY, BattleAnimation.Type.ELEC,
        35,     80,     7),
    ICE("Ice",
        "Weak, but high chance to crit.", "skill_fire",
        ENEMY, BattleAnimation.Type.ICE,
        25,     100,    7),
    RUIN("Ruin",
        "Does damage based on\ntarget's remaining HP.", "skill_fire",
        ENEMY, BattleAnimation.Type.FIRE,
        50,     100,    6),
    CURE("Cure",
        "Restores HP.", "skill_fire",
        PLAYER, BattleAnimation.Type.FIRE,
        50,     0,      10);

    public final String name,
                        desc, spriteName;
    public final DefaultTarget defaultTarget;
    public final BattleAnimation.Type animType;
    public final int basePower, baseAccuracy, baseCost;
    Skill(String n,
          String d, String s,
          DefaultTarget t, BattleAnimation.Type at,
          int p, int a, int c) {
        name = n;

        desc = d;
        spriteName = s;

        defaultTarget = t;
        animType = at;

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
            case MULTI_TARGET:
                return (int)(baseCost*1.5);
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
        switch (this) {
            case FIRE:
                BattleUIManager.logMessage("You cast Fireball!");
                for (Fighter f : targets) {
                    f.damage(Element.FIRE, magicStat, power(), accuracy(), false);
                    f.addAnim(new BattleAnimation(animType));
                }
                break;
            case BOLT:
                BattleUIManager.logMessage("You cast Thunderbolt!");
                for (Fighter f : targets) {
                    f.damage(Element.ELEC, magicStat, power(), accuracy(), false);
                    f.addAnim(new BattleAnimation(animType));
                }
                break;
            case ICE:
                BattleUIManager.logMessage("You cast Ice!");
                for (Fighter f : targets) {
                    boolean crit = Math.random() > .35;
                    f.damage(Element.ICE, magicStat, power(), accuracy(), crit);
                    f.addAnim(new BattleAnimation(animType));
                }
                break;
            case RUIN:
                BattleUIManager.logMessage("You cast Ruin!");
                for (Fighter f : targets) {
                    f.cutHealth(Element.RUIN, power()/100.0, accuracy());
                    f.addAnim(new BattleAnimation(animType));
                }
                break;
            case CURE:
                BattleUIManager.logMessage("You cast Cure!");
                for (Fighter f : targets) {
                    f.restoreHealth(power());
                    f.addAnim(new BattleAnimation(animType));
                }
                break;
            default:
                handler.showDialogue("You can't use this here.");
        }
    }
}