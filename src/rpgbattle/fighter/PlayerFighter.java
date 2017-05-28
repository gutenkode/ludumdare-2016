package rpgbattle.fighter;

import java.util.ArrayList;
import java.util.Arrays;

import rpgbattle.BattleManager;
import rpgbattle.PlayerSkills;
import rpgbattle.fighter.Fighter.Toast.ToastType;
import rpgsystem.Element;
import rpgsystem.Item;
import rpgsystem.Skill;
import rpgsystem.SkillModifier;
import rpgsystem.StatEffect;
import ui.BattleUIManager;
import ui.MenuHandler;

/**
 * Contains logic for player actions in a battle.
 * @author Peter
 */
public class PlayerFighter extends Fighter {

    private int delay;
    private final int attackDelay = 45, skillDelay = 55, itemDelay = 55, runDelay = 45; // delay times for different actions
    
    public PlayerFighter() {

        double[] emult = new double[Element.values().length];
        Arrays.fill(emult, 1);
        stats = new FighterStats(this,
                100,80,80,
                20,10,10,
                0.2,0.05,
                emult);
    }
    
    @Override
    public int initAct() {
        BattleUIManager.startPlayerTurn();
        //BattleUIManager.logMessage("It's your turn.");
        int startDelay = 30;
        delay = -1;
        
        // stamina regen
        if (statEffects.contains(StatEffect.FATIGUE)) {
            addToast(ToastType.STAMINA.color+"FATIGUE -"+(stats.attack()/2));
            drainStamina(stats.attack()/2);
            startDelay += 30;
        } else if (stats.stamina != stats.maxStamina) {
            //addToast(ToastType.STAMINA.color+"REGEN");
            restoreStamina(stats.attack()/3);
            startDelay += 30;
        }
        return startDelay;
    }
    @Override
    public int act() {
        return delay;
        /*
        if (turnUsed) {
            delay--;
            if (delay <= 0)
                return true;
        }
        return false;*/
    }
    
    @Override
    public void damage(Element e, int stat, int atkPower, int accuracy, boolean crit) {
        if (calculateHit(accuracy)) {
            int dmg = calculateDamage(e,stat*atkPower,crit);

            if (dmg != 0) {
                // actually do health subtraction
                lastHealth = stats.health;
                stats.health -= (int)dmg;
                stats.health = Math.max(0, stats.health);
                addToast("-" + dmg);
                shakeVel = dmg;
            }
        } else {
            addToast("MISS");
        }
    }
    @Override
    public void cutHealth(Element e, double percent, int accuracy) {
        if (calculateHit(accuracy)) {
            int dmg = (int)(stats.health*percent);

            if (dmg != 0) {
                // actually do health subtraction
                lastHealth = stats.health;
                stats.health -= dmg;
                stats.health = Math.max(0, stats.health);
                addToast("-" + dmg);
                shakeVel = dmg;
            }
        } else {
            addToast("MISS");
        }
    }
    @Override
    public boolean restoreHealth(int amount) {
        if (stats.health == stats.maxHealth)
            return false;
        
        int delta = stats.health;
        stats.health += amount;
        stats.health = Math.min(stats.health, stats.maxHealth);
        addToast(ToastType.HEAL, stats.health-delta);
        lastHealth = stats.health;
        return true;
    }
    @Override
    public boolean restoreStamina(int amount) {
        if (stats.stamina == stats.maxStamina)
            return false;
        
        int delta = stats.stamina;
        stats.stamina += amount;
        stats.stamina = Math.min(stats.stamina, stats.maxStamina);
        addToast(ToastType.STAMINA, "+"+(stats.stamina-delta));
        lastStamina = stats.stamina;
        return true;
    }
    @Override
    public boolean restoreMana(int amount) {
        if (stats.mana == stats.maxMana)
            return false;
        
        int delta = stats.mana;
        stats.mana += amount;
        stats.mana = Math.min(stats.mana, stats.maxMana);
        addToast(ToastType.MANA, "+"+(stats.mana-delta));
        lastMana = stats.mana;
        return true;
    }
    
    
    /** Whether all the player's stats are full.
     * @return 
     */
    public boolean areStatsFull() {
        return stats.health == stats.maxHealth &&
               stats.stamina == stats.maxStamina &&
               stats.mana == stats.maxMana;
    }
    /**
     * Will use the specified amount of stamina.
     * Does NOT print a toast.
     * @param amount 
     */
    public void drainStamina(int amount) {
        lastStamina = stats.stamina;
        stats.stamina -= amount;
        stats.stamina = Math.max(0, stats.stamina);
    }
    /**
     * Will use the specified amount of mana.
     * Does NOT print a toast.
     * @param amount
     */
    public void drainMana(int amount) {
        lastMana = stats.mana;
        stats.mana -= amount;
        stats.mana = Math.max(0, stats.mana);
    }
    // functions called from RootBattleMenu or submenus
    
    public boolean useAttack(MenuHandler handler, Fighter... targets) {
        BattleUIManager.logMessage("You attack!");
        targets[0].damage(Element.PHYS, stats.attack(), getAttackPower(), 100, false);
        
        lastStamina = stats.stamina;
        stats.stamina -= stats.attack()/2;
        stats.stamina = Math.max(0, stats.stamina);
        addToast(ToastType.STAMINA, "-"+stats.attack());
        
        delay = attackDelay;
        return true;
    }
    public int getAttackPower() {
        return (int)(10*
                Math.min(1,
                .25+(stats.stamina/(float)stats.maxStamina)
                ));
    }
    public boolean canUseSkill(MenuHandler handler, Skill skill) {
        if (skill.cost() > stats.mana) {
            handler.showDialogue("You don't have enough mana!",skill.spriteName);
            return false;
        }
        return true;
    }
    public boolean useSkill(MenuHandler handler, Skill skill, Fighter... targets) {
        if (!canUseSkill(handler, skill))
            return false;
        
        lastMana = stats.mana;
        stats.mana -= skill.cost();
        addToast(ToastType.MANA, "-"+skill.cost());
        /*
        if (PlayerSkills.getLinkedModifier(skill) == SkillModifier.MOD_MULTI_TARGET) {
            Fighter[] targets = new Fighter[BattleManager.getEnemies().size()];
            for (int i = 0; i < targets.length; i++)
                targets[i] = BattleManager.getEnemies().get(i);
            skill.useBattle(handler,  stats.magic, targets);
        } else {
            Fighter target;
            switch (skill.defaultTarget) {
                case PLAYER:
                    target = this;
                    break;
                case ENEMY:
                    target = BattleManager.getEnemies().get(0);
                    break;
                default:
                    target = null;
                    break;
            }
            skill.useBattle(handler,  stats.magic, target);
        }*/
        for (Fighter f : targets)
            skill.useBattle(handler,  stats.magic(), f);

        delay = skillDelay;
        return true;
    }
    public boolean useItem(MenuHandler handler, Item item, Fighter... targets) {
        boolean turnUsed = false;
        for (Fighter f : targets)
            if (item.useBattle(handler, f)) {
                turnUsed = true;
                delay = itemDelay;
            }

        return turnUsed;
    }
    public boolean useRun(MenuHandler handler) {
        BattleUIManager.logMessage("You escape safely!");
        BattleManager.runFromBattle();

        delay = runDelay;
        return true;
    }
    
    public boolean canUseSkill(Skill s) {
        return s.cost() <= stats.mana;
    }

    @Override
    protected String getStatusEffectString(StatEffect e) {
        switch (e) {
            case POISON:
                return "You are now poisoned!";
            case FATIGUE:
                return "You are now fatigued!";
            case DEF_UP:
                return "Your defense increased!";
            default:
                return "You are now [" + e.name() + "]!";
        }
    }
}