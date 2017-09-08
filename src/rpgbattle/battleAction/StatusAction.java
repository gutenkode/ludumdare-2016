package rpgbattle.battleAction;

import rpgbattle.fighter.Fighter;
import rpgsystem.StatusEffect;

/**
 * Inflicts a status condition.
 * Created by Peter on 6/27/17.
 */
public class StatusAction extends BattleAction {

    private StatusEffect effect;
    private int accuracy, delay;
    private Fighter fighter;

    public StatusAction(Fighter fighter, StatusEffect effect, int accuracy) {
        this(fighter, effect, accuracy, STD_ACTION_DELAY);
    }
    public StatusAction(Fighter fighter, StatusEffect effect, int accuracy, int delay) {
        this.effect = effect;
        this.accuracy = accuracy;
        this.fighter = fighter;
        this.delay = delay;
    }

    @Override
    public int act() {
        // if the infliction is successful, the animation is played automatically
        fighter.inflictStatus(effect, accuracy);
        return delay;
    }
}
