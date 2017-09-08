package rpgbattle.fighter;

import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.EnemyData;
import rpgbattle.enemyBehavior.EnemyBehavior;
import rpgbattle.fighter.Fighter.Toast.ToastType;
import rpgsystem.Element;
import rpgsystem.StatusEffect;
import ui.BattleUIManager;
import ui.components.EnemySprite;

/**
 * Encapsulates the logic of an enemy in a battle.
 * @author Peter
 */
public class EnemyFighter extends Fighter {

    public final int[] frameDelay;
    public final String enemyName,
                        displayName,
                        spriteName,
                        encounterString,
                        deathString;
    private EnemyBehavior behavior;
    private EnemySprite sprite;
    private float deathScale = 1;
    
    public EnemyFighter(String enemyName) {
        this.enemyName = enemyName;
        displayName = EnemyData.getDisplayName(enemyName);
        spriteName = EnemyData.getBattleSprite(enemyName);
        encounterString =  EnemyData.getEncounterString(enemyName);
        deathString = EnemyData.getDeathString(enemyName);
        stats = EnemyData.populateStats(enemyName,this);
        frameDelay = EnemyData.getFrameDelay(enemyName);
        
        behavior = EnemyData.getBehavior(this);
        sprite = new EnemySprite(this);
    }
    
    @Override
    public int initAct() {
        actionStartFlash();
        return behavior.initAct();
    }
    
    @Override
    public int act() {
        return behavior.act();
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
            
            if (stats.health <= 0)
                BattleManager.enemyDied(this);
        } else {
            addToast("MISS");
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

            if (stats.health <= 0)
                BattleManager.enemyDied(this);
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
            BattleManager.enemyDied(this);
        } else {
            addToast("MISS");
            AudioPlayback.playSfx("sfx_skill_miss");
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
    public boolean restoreStamina(int amount) {return false; }
    @Override
    public boolean restoreMana(int amount) { return false; }

    public void runDeathAnimation(ModelMatrix model) {
        model.scale(deathScale,1,1);
        if (deathScale > 0) {
            deathScale -= .035;
            if (deathScale < 0)
                deathScale = 0;
        }
    }
    public boolean isDeathAnimationFinished() { return deathScale <= 0; }
    public EnemySprite getSprite() { return sprite; }
    
    //public void runDeathAnimation(ModelMatrix model) {
    //    behavior.runDeathAnimation(model);
    //}

    @Override
    protected String getStatusEffectString(StatusEffect e) {
        switch (e) {
            case POISON:
                return "The "+displayName+" is poisoned!";
            case FATIGUE:
                return "The "+displayName+" is fatigued!";
            default:
                return "The "+displayName+" is now [" + e.name() + "]!";
        }
    }
}