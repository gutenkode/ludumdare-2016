package rpgbattle.fighter;

import java.util.HashMap;

import mote4.util.audio.AudioPlayback;
import rpgbattle.BattleManager;
import rpgbattle.battleAction.BattleAction;
import rpgbattle.fighter.Fighter.Toast.ToastType;
import rpgsystem.Element;
import rpgsystem.Item;
import rpgsystem.Skill;
import rpgsystem.StatusEffect;
import scenes.Battle;
import ui.BattleUIManager;
import ui.MenuHandler;

/**
 * Contains logic for player actions in a battle.
 * @author Peter
 */
public class PlayerFighter extends Fighter {

    private int delay; // used for timing actions during battle
    
    public PlayerFighter() {
        // an empty map will default to normal resistance for all elements
        HashMap<Element, Element.Resistance> emult = new HashMap<>();
        stats = new FighterStats(this,
                100,100,100,
                10,10,10,
                emult);
    }
    
    @Override
    public int initAct() {
        BattleUIManager.startPlayerTurn();
        BattleUIManager.logMessage("It's your turn.");
        int startDelay = 30;
        delay = -1;
        
        // stamina regen
        if (statusEffects.contains(StatusEffect.FATIGUE)) {
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
    }
    
    @Override
    public void damage(Element e, int stat, int atkPower, int accuracy, boolean crit) {
        if (calculateHit(accuracy)) {
            int dmg = calculateDamage(e, stat, atkPower,crit);

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
            AudioPlayback.playSfx("sfx_skill_miss");
        }
    }
    @Override
    public void darkDamage(Element e, double percent, int accuracy) {
        if (calculateHit(accuracy))
        {
            Element.Resistance res = stats.elementResistance(e);
            switch (res) {
                case WEAK:
                    addToast("WEAK");
                    percent *= 1.5;
                    break;
                case RES:
                    addToast("RESIST...");
                    percent *= 0.5;
                    break;
                case NULL:
                    addToast("NULL");
                    percent = 0;
                    break;
            }

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
    public void lightDamage(Element e, int accuracy) {
        Element.Resistance res = stats.elementResistance(e);
        switch (res) {
            case WEAK:
                addToast("WEAK");
                accuracy *= 1.5;
                break;
            case RES:
                addToast("RESIST...");
                accuracy *= 0.5;
                break;
            case NULL:
                addToast("NULL");
                accuracy = 0;
                break;
        }
        if (calculateHit(accuracy)) {
            lastHealth = stats.health;
            stats.health = 0;
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

    ///////// Functions called from RootBattleMenu or its submenus
    
    public boolean useAttack(MenuHandler handler, Fighter... targets) {
        BattleUIManager.logMessage("You attack!");
        for (int i = 0; i < targets.length; i++) {
            int delay = 10; // multi-target moves will hit in a staggered pattern
            if (i == targets.length-1)
                delay = BattleAction.STD_ACTION_DELAY;
            BattleManager.addAction(new PlayerAttack(targets[i], delay, this));
        }
        delay = BattleAction.STD_INIT_DELAY/2;
        return true;
    }
    public int getAttackPower() {
        return (int)(stats.attack() * getAttackPowerPercent());
    }
    public double getAttackPowerPercent() {
        // returns a value 0.3 to 1.0,
        // based on the amount of remaining stamina
        return Math.min(1,.3+.7*(stats.stamina/(double)stats.maxStamina));
    }
    /**
     * The amount of stamina that a standard attack will use.
     * @return
     */
    public int getAttackStaminaCost() {
        return stats.attack();
    }
    public boolean canUseSkill(MenuHandler handler, Skill skill) {
        if (skill.data.usesSP) {
            //if (skill.data.cost() > stats.stamina) {
            if (stats.stamina <= 0) {
                // stamina skills can be used until stamina is zero
                handler.showDialogue("You don't have enough stamina!", skill.data.spriteName);
                return false;
            }
        } else {
            if (skill.data.cost() > stats.mana) {
                // magic skills cannot be used unless there's enough MP to cover the cost
                handler.showDialogue("You don't have enough mana!", skill.data.spriteName);
                return false;
            }
        }
        return true;
    }
    public boolean useSkill(MenuHandler handler, Skill skill, Fighter... targets) {
        if (!canUseSkill(handler, skill))
            return false;
        BattleUIManager.logMessage("You cast "+skill.data.name+"!");

        for (int i = 0; i < targets.length; i++) {
            int delay = 10; // multi-target moves will hit in a staggered pattern
            if (i == targets.length-1)
                delay = BattleAction.STD_ACTION_DELAY;
            BattleManager.addAction(new PlayerSkill(skill, handler, targets[i], delay, this, i==0)); // only the first move will drain the stat
        }

        delay = BattleAction.STD_INIT_DELAY;
        return true;
    }
    public boolean useItem(MenuHandler handler, Item item, Fighter... targets) {
        if (!item.checkCanUseInBattle(handler, targets[0]))
            return false;
        BattleUIManager.logMessage(item.useString);

        for (int i = 0; i < targets.length; i++) {
            int delay = 10; // multi-target moves will hit in a staggered pattern
            if (i == targets.length-1)
                delay = BattleAction.STD_ACTION_DELAY;
            BattleManager.addAction(new PlayerItem(item, handler, targets[i], delay));
        }
        delay = BattleAction.STD_INIT_DELAY;
        return true;
    }
    public double getRunPercent() {
        return (stats.stamina/(float)stats.maxStamina)*.5+.25;
    }
    public boolean useRun(MenuHandler handler) {
        BattleUIManager.logMessage("You try to escape...");
        boolean successful = Math.random() < getRunPercent();
        BattleManager.addAction(new PlayerRun(successful));
        delay = BattleAction.STD_INIT_DELAY + BattleAction.STD_ACTION_DELAY;
        return true;
    }

    @Override
    protected String getStatusEffectString(StatusEffect e) {
        switch (e) {
            case POISON:
                return "You are now poisoned!";
            case FATIGUE:
                return "You are now fatigued!";
            default:
                return "You are now [" + e.name() + "]!";
        }
    }
}

/////////// BattleActions for the player

class PlayerAttack extends BattleAction {

    private Fighter target;
    private int delay;
    private PlayerFighter p;

    public PlayerAttack(Fighter target, int delay, PlayerFighter p) {
        this.target = target;
        this.delay = delay;
        this.p = p;
    }
    @Override
    public int act() {
        AudioPlayback.playSfx("sfx_skill_normalhit");
        target.damage(Element.PHYS, p.stats.attack(), p.getAttackPower(), 100, false);

        p.lastStamina = p.stats.stamina;
        p.stats.stamina -= p.getAttackStaminaCost();
        p.stats.stamina = Math.max(0, p.stats.stamina);
        //p.addToast(ToastType.STAMINA, "-"+p.stats.attack());

        return delay;
    }
}
class PlayerSkill extends BattleAction {

    private Fighter target;
    private int delay;
    private PlayerFighter p;
    private Skill skill;
    private MenuHandler handler;
    private boolean drainStat;

    public PlayerSkill(Skill skill, MenuHandler handler, Fighter target, int delay, PlayerFighter p, boolean drainStat) {
        this.target = target;
        this.delay = delay;
        this.p = p;
        this.skill = skill;
        this.handler = handler;
        this.drainStat = drainStat;
    }

    @Override
    public int act() {
        if (skill.data.usesSP) {
            if (drainStat) {
                p.lastStamina = p.stats.stamina;
                p.stats.stamina -= skill.data.cost();
                p.stats.stamina = Math.max(0, p.stats.stamina);
            }
            //p.addToast(ToastType.STAMINA, "-" + skill.data.cost());
            skill.useBattle(handler,  p.stats.attack(), target);
        } else {
            if (drainStat) {
                p.lastMana = p.stats.mana;
                p.stats.mana -= skill.data.cost();
                p.stats.mana = Math.max(0, p.stats.mana);
            }
            //p.addToast(ToastType.MANA, "-" + skill.data.cost());
            skill.useBattle(handler,  p.stats.magic(), target);
        }
        return delay;
    }
}
class PlayerItem extends BattleAction {

    private Item item;
    private int delay;
    private MenuHandler handler;
    private Fighter target;

    public PlayerItem(Item item, MenuHandler handler, Fighter target, int delay) {
        this.item = item;
        this.delay = delay;
        this.handler = handler;
        this.target = target;
    }
    @Override
    public int act() {
        item.useBattle(handler, target);
        return delay;
    }
}
class PlayerRun extends BattleAction {
    private boolean successful;
    public PlayerRun(boolean successful) {
        this.successful = successful;
    }
    @Override
    public int act() {
        if (successful) {
            BattleUIManager.logMessage("You got away!");
            BattleManager.runFromBattle();
            return STD_ACTION_DELAY;
        } else {
            BattleUIManager.logMessage("You couldn't get away!");
            return STD_ACTION_DELAY;
        }
    }
}