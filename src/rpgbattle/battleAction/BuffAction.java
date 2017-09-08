package rpgbattle.battleAction;

import mote4.util.audio.AudioPlayback;
import rpgbattle.fighter.Fighter;

/**
 * Buffs/debuffs a stat.
 * Created by Peter on 6/27/17.
 */
public class BuffAction extends BattleAction {
    private Fighter fighter;
    private Stat stat;
    private int amount, delay;
    public BuffAction(Fighter fighter, Stat stat, int amount) {
        this(fighter, stat, amount, STD_ACTION_DELAY);
    }
    public BuffAction(Fighter fighter, Stat stat, int amount, int delay) {
        this.fighter = fighter;
        this.stat = stat;
        this.amount = amount;
        this.delay = delay;
    }
    @Override
    public int act() {
        switch (stat) {
            case ATK:
                fighter.stats.buffAtk(amount); break;
            case DEF:
                fighter.stats.buffDef(amount); break;
            case MAG:
                fighter.stats.buffMag(amount); break;
        }
        if (amount >= 0)
            AudioPlayback.playSfx("sfx_skill_buff");
        else
            AudioPlayback.playSfx("sfx_skill_debuff");
        return delay;
    }

    public enum Stat {
        ATK, DEF, MAG,
        ATK_DEF, ATK_MAG, DEF_MAG,
        ALL;
    }
}
