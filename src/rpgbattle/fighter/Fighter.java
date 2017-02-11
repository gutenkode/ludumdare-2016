package rpgbattle.fighter;

import java.util.ArrayList;
import mote4.util.matrix.ModelMatrix;
import mote4.util.shader.Uniform;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import nullset.Const;
import nullset.RootLayer;
import rpgbattle.fighter.Fighter.Toast.ToastType;
import rpgsystem.Element;
import rpgsystem.StatEffect;
import ui.BattleUIManager;
import ui.components.BattleAnimation;

/**
 * Represents a single fighter on the field.  This includes enemies and the player.
 * @author Peter
 */
public abstract class Fighter {
    
    public ArrayList<StatEffect> statEffects = new ArrayList<>();
    private ArrayList<BattleAnimation> battleAnimations = new ArrayList<>();
    public FighterStats stats;
    
    float[] flash = new float[] {0,0,0};
    double shake, shakeVel;
    protected int lastHealth, lastStamina, lastMana;
    
    private int doubleFlashDelay = -1; // used to trigger the second flash from actionStartFlash()
    private float flashDecay, // the rate at which a flash color will fade
                  poisonDelay; // delay for taking poison damage
    
    /**
     * Called once at the beginning of a Fighter's turn.
     */
    public abstract void initAct();
    /**
     * When this Fighter is active, taking its turn, act() will be called
     * until this Fighter indicates that its turn is over.
     * @return Indicates that this Fighter is done taking its turn.
     */
    public abstract boolean act();
    
    public abstract void damage(Element e, int stat, int atkPower, int accuracy, boolean crit);
    public abstract void cutHealth(Element e, double percent, int accuracy);
    /**
     * Runs an accuracy check for an attack against this Fighter.
     * Returns true the % of time the attack should hit.
     * @param accuracy Accuracy as an int from 0-100.
     * @return 
     */
    final boolean calculateHit(int accuracy) {
        double rand = Math.random();
        return (rand < accuracy*.01-stats.evasion());
        // if random value is less than attack's accuracy minus evasion
    }
    /**
     * Standardized method for performing damage calculation.
     * @param element
     * @param strength
     * @param crit 
     */
    final int calculateDamage(Element element, int strength, boolean crit) {
        // use this formula instead, from Persona...
        /*
        DMG = 5 x sqrt(ST/EN x ATK) x MOD x HITS X RND

        DMG = Damage
        ST = Character's Strength stat
        EN = Enemy's Endurance stat
        ATK = Atk value of equipped weapon OR Pwr value of used skill
        MOD = Modifier based on the difference between character level and enemy level
        HITS= Number of hits (for physical skills)
        RND = Randomness factor (according to DragoonKain33, may be roughly between 0.95 and 1.05)
         */

        // elemental strength/weakness
        double elementMultVal = stats.elementMultiplier(element.index);

        int dmg;
        if (crit) {// criticals have 1.5 power and use /1.5 the defense stat
            dmg = (int)(1.5*strength/(stats.defense()/1.5));
            addToast("CRITICAL");
        } else
            dmg = strength/stats.defense();

        // multiplier for attack elemental type
        dmg *= elementMultVal;
        if (elementMultVal > 1)
            addToast("WEAK");
        else if (elementMultVal < 1)
            addToast("RESIST...");
        
        // slight randomness, add/subtract up to a 10th of total damage
        dmg += (int)((Math.random()*dmg*.2)-(dmg*.1));

        // damage cap
        dmg = Math.min(9999, dmg);

        if (dmg == 0)
            addToast("NO DAMAGE");

        // flash the sprite the color of the elemental attack
        flash(element.color);
        
        return dmg;
    }
    /**
     * Inflicts damage from poison.
     */
    public void poisonDamage() {
        if (stats.health > 0)
            if (poisonDelay <= 0) {
                poisonDelay = 60*4;
                lastHealth = stats.health;
                int dmg = stats.maxHealth/20;
                stats.health -= dmg;
                stats.health = Math.max(1, stats.health);
                addToast(ToastType.POISON, "-"+dmg);
            } else
                poisonDelay--;
    }
    
    public boolean isDead() { return stats.health <= 0; }
    
    public void inflictStatus(StatEffect e, int accuracy) {
        if (calculateHit(accuracy)) {
            BattleUIManager.logMessage(getStatusEffectString(e));
            switch (e) {
                case POISON:
                    addToast(ToastType.POISON, e.name.toUpperCase());
                    break;
                case FATIGUE:
                    addToast(ToastType.STAMINA, e.name.toUpperCase());
                    break;
                case DEF_UP:
                    addToast("+DEFENSE");
                    break;
                default:
                    addToast(e.name.toUpperCase());
                    break;
            }
            this.addAnim(new BattleAnimation(BattleAnimation.Type.STATUS));
            if (!statEffects.contains(e)) {
                statEffects.add(e);
            }
        } else {
            addToast("MISS");
        }
    }
    protected abstract String getStatusEffectString(StatEffect e);
    public boolean hasStatus(StatEffect e) {
        return statEffects.contains(e);
    }
    public void cureStatus(StatEffect e) {
        addToast("CURED "+e.name.toUpperCase());
        statEffects.remove(e);
    }
    
    /**
     * Flashes this Fighter a color, mainly for added effect when taking damage
     * or afflicted by a status effect.
     * @param rgb 
     */
    void flash(float... rgb) {
        if (rgb.length == 3)
            flash = rgb.clone();
        flashDecay = .9f;
    }
    /**
     * A special double flash indicating this Fighter is about to use its turn.
     */
    void actionStartFlash() {
        doubleFlashDelay = 10;
        flash[0] = flash[1] = flash[2] = .1f;
        flashDecay = 1.2f;
    }
    public float[] updateFlash() {
        doubleFlashDelay--;
        if (doubleFlashDelay == 0) {
            flashDecay = .8f;
        }
        flash[0] *= flashDecay;
        flash[1] *= flashDecay;
        flash[2] *= flashDecay;
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

    public int shakeValue() {
        shake += shakeVel;
        shakeVel -= shake/6;
        shake *= .9;
        shakeVel *= .9;

        return (int)shake;
    }
    
    // text "toasts" for displaying damage/restore amounts
    
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
            mesh = FontUtils.createStringColor(text, 0, 0, Const.UI_SCALE, Const.UI_SCALE);
            
            centerOffset = FontUtils.getStringWidth(text)/2*Const.UI_SCALE;
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

    // battle animation handling

    public void addAnim(BattleAnimation ba) {
        battleAnimations.add(ba);
    }
    public void updateAnim() {
        for (int i = 0; i < battleAnimations.size(); i++) {
            if (battleAnimations.get(i).render()) {
                battleAnimations.remove(i);
                i--;
            }
        }
    }
}