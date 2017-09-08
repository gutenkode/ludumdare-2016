package rpgbattle.battleAction;

/**
 * An individual action to perform during battle, such as an attack or dialogue.
 * Fighters (typically) create BattleActions and add them to the queue in the
 * BattleManager, to keep modularity high.
 * Created by Peter on 6/26/17.
 */
public abstract class BattleAction {
    public static final int STD_INIT_DELAY = 30,    // default delay time for turn init text
                            STD_ACTION_DELAY = 50;  // default delay time for actions
    public abstract int act();
}
