package rpgbattle.battleAction;

import ui.BattleUIManager;

/**
 * Play a script during a battle.
 * Created by Peter on 6/27/17.
 */
public class ScriptAction extends BattleAction {
    private String script;
    private boolean init = true;
    private int delay = 25;
    public ScriptAction(String script) {
        this.script = script;
    }
    @Override
    public int act() {
        if (delay > 0) {
            delay--;
            return -1;
        }
        if (init) {
            BattleUIManager.playScript(script);
            init = false;
        }
        if (BattleUIManager.isScriptPlaying())
            return -1;
        return 30;
    }
}
