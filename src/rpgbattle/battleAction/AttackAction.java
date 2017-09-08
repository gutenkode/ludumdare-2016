package rpgbattle.battleAction;

import mote4.util.audio.AudioPlayback;
import rpgbattle.BattleManager;
import rpgbattle.fighter.Fighter;
import rpgsystem.Element;
import ui.components.BattleAnimation;

/**
 * Standard attack for enemies.
 * Created by Peter on 6/26/17.
 */
public class AttackAction extends BattleAction {

    private int delay;

    private Fighter target;
    private Element e;
    private BattleAnimation an;
    private int attack, power, accuracy;
    private boolean crit;
    private String sfxName;

    public AttackAction(Fighter target, int attack, int power) {
        this(target, attack, power, STD_ACTION_DELAY);
    }
    public AttackAction(Fighter target, int attack, int power, int delay) {
        this(target, Element.PHYS, null, attack, power, 100, false, delay);
        sfxName = "sfx_skill_normalhit";
    }
    public AttackAction(Fighter target, Element e, BattleAnimation an, int attack, int power, int accuracy, boolean crit, int delay) {
        this.e = e;
        this.an = an;
        this.attack = attack;
        this.power = power;
        this.accuracy = accuracy;
        this.crit = crit;
        this.target = target;
        this.delay = delay;
        sfxName = "sfx_skill_"+e.name().toLowerCase();
    }

    @Override
    public int act() {
        target.damage(e, attack, power, accuracy, crit);
        AudioPlayback.playSfx(sfxName);
        if (an != null)
            BattleManager.getPlayer().addAnim(an);
        return delay;
    }
}
