package rpgbattle.fighter;

import java.util.ArrayList;

import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.ModelMatrix;
import mote4.util.shader.Uniform;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import main.Vars;
import main.RootLayer;
import rpgbattle.fighter.Fighter.Toast.ToastType;
import rpgsystem.Element;
import rpgsystem.StatusEffect;
import ui.BattleUIManager;
import ui.components.BattleAnimation;

/**
 * Represents a single fighter on the field.  This includes enemies and the player.
 * @author Peter
 */
public abstract class Fighter {
    
    public ArrayList<StatusEffect> statusEffects = new ArrayList<>();
    private ArrayList<BattleAnimation> battleAnimations = new ArrayList<>();
    public FighterStats stats;

    protected int lastHealth, lastStamina, lastMana;

    double shake, shakeVel;
    float[] targetFlash, flash = new float[] {0,0,0};
    private int flashState = 0; // indicates the state of the flash effect. Even = increase, Odd = decrease
    private float flashSpeed, // the rate at which a flash color will change
                  flashIndex; // current state of the flash effect
    
    /**
     * Called once at the beginning of a Fighter's turn.
     * @return If >= 0, indicates that this Fighter is done performing any on-start actions.
     */
    public abstract int initAct();
    /**
     * When this Fighter is active, taking its turn, act() will be called
     * until this Fighter indicates that its turn is over.
     * Any actions created by this Fighter will be ran next.
     * @return If >= 0, indicates that this Fighter is done taking its turn.
     */
    public abstract int act();
    
    public abstract void damage(Element e, int stat, int atkPower, int accuracy, boolean crit);
    public abstract void darkDamage(Element e, double percent, int accuracy);
    public abstract void lightDamage(Element e, int accuracy);
    /**
     * Runs an accuracy check for an attack against this Fighter.
     * Returns true the % of time the attack should hit.
     * @param accuracy Accuracy as an int from 0-100.
     * @return
     */
    final boolean calculateHit(int accuracy) {
        double rand = Math.random();
        return (rand < accuracy*.01);
        // if random value is less than attack's accuracy
    }
    /**
     * Standardized method for performing damage calculation.
     * @param element Element of the attack.
     * @param skillPower Power of the skill used.
     * @param atkStat Attack stat of the attacker.
     * @param crit Whether this is a critical hit.
     */
    final int calculateDamage(Element element, int atkStat, int skillPower, boolean crit) {
        /*
            Use this formula instead, from Persona...
            DMG = 5 x sqrt(ST/EN x ATK) x MOD x HITS x RND

            DMG = Damage
            ST  = Character's Strength stat
            EN  = Enemy's Endurance stat
            ATK = Atk value of equipped weapon OR Pwr value of used skill
            MOD = Modifier based on the difference between character level and enemy level
            HITS= Number of hits (for physical skills)
            RND = Randomness factor (according to DragoonKain33, may be roughly between 0.95 and 1.05)
         */

        // elemental strength/weakness
        Element.Resistance resistance = stats.elementResistance(element);

        double StEnRatio;
        if (crit) {
            // critical hits have 3/2 power, 2/3 the defense stat, and ignore defense buffs
            int critDef = Math.min(stats.defense(), stats.baseDefense());
            StEnRatio = (1.5*atkStat) / (critDef/1.5);
            addToast("CRITICAL");
        } else
            StEnRatio = atkStat/(double)stats.defense();

        double dmg =  5 * Math.sqrt(StEnRatio * skillPower);

        // multiplier for attack elemental type
        switch (resistance) {
            case WEAK:
                addToast("WEAK");
                dmg *= 1.5;
                break;
            case RES:
                addToast("RESIST...");
                dmg *= 0.5;
                break;
            case NULL:
                addToast("NULL");
                dmg = 0;
                break;
        }

        // slight randomness, add/subtract up to a 20th of total damage
        dmg += (Math.random()*dmg*.1)-(dmg*.05);

        // damage cap
        dmg = Math.min(9999, dmg);

        dmg = Math.round(dmg);

        if (dmg == 0 && resistance != Element.Resistance.NULL)
            addToast("NO DAMAGE");

        // flash the sprite the color of the elemental attack
        flash(element.color);

        return (int)dmg;
    }
    /**
     * Inflicts damage from poison.
     */
    public void poisonDamage() {
        if (stats.health > 1) {
            lastHealth = stats.health;
            int dmg = stats.maxHealth / 12;
            stats.health -= dmg;
            stats.health = Math.max(1, stats.health);
            AudioPlayback.playSfx("sfx_skill_status");
            addToast(ToastType.POISON, "POISON -" + (lastHealth - stats.health)); // if health was capped at 1hp, actual damage might be different from dmg
        }
    }

    public boolean isDead() { return stats.health <= 0; }
    
    public void inflictStatus(StatusEffect e, int accuracy) {
        if (calculateHit(accuracy)) {
            AudioPlayback.playSfx("sfx_skill_status");
            BattleUIManager.logMessage(getStatusEffectString(e));
            switch (e) {
                case POISON:
                    addToast(ToastType.POISON, e.name.toUpperCase());
                    break;
                case FATIGUE:
                    addToast(ToastType.STAMINA, e.name.toUpperCase());
                    break;
                default:
                    addToast(e.name.toUpperCase());
                    break;
            }
            this.addAnim(new BattleAnimation(BattleAnimation.Type.STATUS));
            if (!statusEffects.contains(e)) {
                statusEffects.add(e);
            }
        } else {
            addToast("MISS");
            AudioPlayback.playSfx("sfx_skill_miss");
        }
    }
    protected abstract String getStatusEffectString(StatusEffect e);
    public boolean hasStatus(StatusEffect e) {
        return statusEffects.contains(e);
    }
    public void cureStatus(StatusEffect e) {
        addToast("CURED "+e.name.toUpperCase());
        statusEffects.remove(e);
    }
    
    /**
     * Flashes this Fighter a color, mainly for added effect when taking damage
     * or afflicted by a status effect.
     * @param rgb 
     */
    void flash(float... rgb) {
        if (rgb.length != 3)
            throw new IllegalArgumentException("Flash color array must have three components.");
        flash = new float[] {0,0,0};
        targetFlash = rgb.clone();
        flashIndex = 0;
        flashState = 2;
        flashSpeed = .06f;
    }
    /**
     * A special double flash indicating this Fighter is about to use its turn.
     */
    void actionStartFlash() {
        flash = new float[] {0,0,0};
        targetFlash = new float[] {1,1,1};
        flashIndex = 0;
        flashState = 4; // flash twice
        flashSpeed = .135f;
    }

    /**
     * Update and return the flash effect values.
     * @return
     */
    public float[] updateFlash() {
        if (flashState > 0) {
            if (flashState % 2 == 0) {
                // even, increase
                flashIndex += flashSpeed;
                if (flashIndex >= 1)
                    flashState--;
            } else {
                // odd, decrease
                flashIndex -= flashSpeed;
                if (flashIndex <= 0) {
                    flashState--;
                    flash[0] = flash[1] = flash[2] = 0;
                }
            }
            for (int i = 0; i < 3; i++)
                flash[i] = targetFlash[i]*flashIndex;
        }
        return flash;
    }
    
    /**
     * Restore this fighter's health.
     * @param amount
     * @return Whether health was restored.  Already full health is considered
     * a failure state.
     */
    public abstract boolean restoreHealth(int amount);
    public abstract boolean restoreStamina(int amount);
    public abstract boolean restoreMana(int amount);
    
    public int lastHealth() { return lastHealth; }
    public int lastStamina() { return lastStamina; }
    public int lastMana() { return lastMana; }

    /**
     * Updates the shake effect value.
     * @return
     */
    public void updateShake() {
        shake += shakeVel;
        shakeVel -= shake/3;
        shake *= .85;
        shakeVel *= .8;
    }
    public int shakeValue() { return (int)shake; }
    
    ////// text "toasts" for displaying damage/restore amounts
    
    private ArrayList<Toast> toast = new ArrayList<>();
    private static final int TOAST_DELAY = 30; // frames to wait for every toast already in the queue
    void addToast(int val) {
        addToast(String.valueOf(val));
    }
    void addToast(String text) {
        // new toasts are delayed by the number of toasts in the queue ahead of them
        toast.add(new Toast(text, TOAST_DELAY*toast.size()));
    }
    void addToast(ToastType type, int val) {
        addToast(type, String.valueOf(val));
    }
    void addToast(ToastType type, String text) {
        if (RootLayer.getState() == RootLayer.State.BATTLE)
            // new toasts are delayed by the number of toasts in the queue ahead of them
            toast.add(new Toast(type.color+text, TOAST_DELAY*toast.size()));
    }
    public void renderToast(ModelMatrix m) {
        for (int i = 0; i < toast.size(); i++) {
            if (toast.get(i).render(m)) {
                toast.remove(i);
                i--;
            }
        }
        Uniform.varFloat("colorMult", 1,1,1,1);
    }
    public boolean hasToastsLeft() { return !toast.isEmpty(); }
    public static class Toast {
        public enum ToastType {
            DAMAGE("@{1,1,1,1}"),
            HEAL("@{0,.8,0,1}"),
            STAMINA("@{1,.9,0,1}"),
            MANA("@{.6,.6,1,1}"),
            POISON("@{.8,0,.7,1}");
            public final String color;
            ToastType(String c) { color = c; }
        }
        private final Mesh mesh;
        private int delay, timeout, randX, randY;
        private float centerOffset;
        Toast(String text, int d) {
            delay = d;
            timeout = 0;
            randX = 0;//(int)(Math.random()*10)-5;
            randY = 0;//(int)(Math.random()*10)-5;
            FontUtils.useMetric("font_1");
            mesh = FontUtils.createStringColor(text, 0, 0, Vars.UI_SCALE, Vars.UI_SCALE);
            
            centerOffset = FontUtils.getStringWidth(text)/2* Vars.UI_SCALE;
        }
        public boolean render(ModelMatrix m) {
            if (delay > 0) {
                delay--;
                return false;
            }
            double val = 1.0-1.0/(timeout/20.0+1.0);
            m.translate(randX-centerOffset, randY-(float)val*40);
            m.makeCurrent();
            Uniform.varFloat("colorMult", 0,0,0,1);
            mesh.render();
            m.translate(-1, -1);
            m.makeCurrent();
            Uniform.varFloat("colorMult", 1,1,1,1);
            mesh.render();
            m.translate(1-randX+centerOffset, 1-randY+(float)val*40);
            timeout++;
            if (timeout > 50) {
                mesh.destroy();
                return true;
            }
            return false;
        }
    }

    ////// battle animation handling

    public void addAnim(BattleAnimation ba) {
        battleAnimations.add(ba);
    }
    public boolean hasAnimationsLeft() { return !battleAnimations.isEmpty(); }
    public void renderAnim() {
        for (int i = 0; i < battleAnimations.size(); i++) {
            if (battleAnimations.get(i).render()) {
                battleAnimations.remove(i);
                i--;
            }
        }
    }
}