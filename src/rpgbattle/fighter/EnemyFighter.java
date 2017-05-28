package rpgbattle.fighter;

import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.EnemyData;
import rpgbattle.enemyBehavior.EnemyBehavior;
import rpgbattle.fighter.Fighter.Toast.ToastType;
import rpgsystem.Element;
import rpgsystem.StatEffect;

/**
 * Encapsulates the logic of an enemy in a battle.
 * @author Peter
 */
public class EnemyFighter extends Fighter {
    
    private int currentFrame, currentFrameDelay;
    private int[] frameDelay;
    private float[] spriteInfo;
    public final String enemyName,
                        displayName,
                        spriteName,
                        encounterString,
                        deathString;
    private EnemyBehavior behavior;
    
    public EnemyFighter(String enemyName) {
        this.enemyName = enemyName;
        displayName = EnemyData.getDisplayName(enemyName);
        spriteName = EnemyData.getBattleSprite(enemyName);
        encounterString =  EnemyData.getEncounterString(enemyName);
        deathString = EnemyData.getDeathString(enemyName);
        frameDelay = EnemyData.getFrameDelay(enemyName);
        stats = EnemyData.populateStats(enemyName,this);
        
        currentFrame = (int)(Math.random()*frameDelay.length);
        currentFrameDelay = (int)(Math.random()*frameDelay[currentFrame]);
        spriteInfo = new float[] {frameDelay.length, 1, 0};
        
        behavior = EnemyData.getBehavior(this);
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
            int dmg = calculateDamage(e,stat*atkPower,crit);

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
    public void cutHealth(Element e, double percent, int accuracy) {
        if (calculateHit(accuracy)) {
            double elementMultVal = stats.elementMultiplier(e.index);
            if (elementMultVal > 1)
                addToast("WEAK");
            else if (elementMultVal < 1)
                addToast("RESIST...");

            int dmg = (int)(stats.health*percent*elementMultVal);

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
    
    /**
     * Updates animation data for this enemy and returns the array needed
     * for rendering the correct tilesheet frame.
     * @return 
     */
    public float[] updateSpriteInfo() { 
        currentFrameDelay--;
        if (currentFrameDelay <= 0) {
            currentFrame++;
            currentFrame %= frameDelay.length;
            currentFrameDelay = frameDelay[currentFrame];
            spriteInfo[2] = currentFrame;
        }
        return spriteInfo; 
    }
    
    public void runDeathAnimation(ModelMatrix model) {
        behavior.runDeathAnimation(model);
    }

    @Override
    protected String getStatusEffectString(StatEffect e) {
        switch (e) {
            case POISON:
                return "The "+displayName+" is poisoned!";
            case FATIGUE:
                return "The "+displayName+" is fatigued!";
            case DEF_UP:
                return "The "+displayName+"'s defense increased!";
            default:
                return "The "+displayName+" is now [" + e.name() + "]!";
        }
    }
}