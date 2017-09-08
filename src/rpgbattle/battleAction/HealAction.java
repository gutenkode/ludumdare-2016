package rpgbattle.battleAction;

import mote4.util.audio.AudioPlayback;
import rpgbattle.fighter.Fighter;

/**
 * Created by Peter on 6/27/17.
 */
public class HealAction extends BattleAction {
    private int amount, delay;
    private Fighter fighter;
    public HealAction(Fighter fighter, int amount) {
        this(fighter, amount, STD_ACTION_DELAY);
    }
    public HealAction(Fighter fighter, int amount, int delay) {
        this.amount = amount;
        this.fighter = fighter;
        this.delay = delay;
    }
    @Override
    public int act() {
        AudioPlayback.playSfx("sfx_skill_heal");
        fighter.restoreHealth(amount);
        return delay;
    }
}
