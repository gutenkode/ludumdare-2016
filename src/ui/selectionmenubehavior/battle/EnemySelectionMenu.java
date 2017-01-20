package ui.selectionmenubehavior.battle;

import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import rpgbattle.fighter.Fighter;
import scenes.Battle;
import ui.MenuHandler;
import ui.selectionmenubehavior.SelectionMenuBehavior;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Created by Peter on 10/16/16.
 */
public class EnemySelectionMenu implements SelectionMenuBehavior {

    private MenuHandler handler;
    private Consumer callback;

    private String title = "TARGET";
    private String[] options;
    private Fighter[] fighters;
    private boolean multiTarget;

    public EnemySelectionMenu(MenuHandler h, Consumer<Fighter[]> cb, boolean mt) {
        handler = h;
        callback = cb;
        multiTarget = mt;

        if (multiTarget) {
            options = new String[] {"All"};
        } else {
            ArrayList<EnemyFighter> enemies = BattleManager.getEnemies();
            fighters = new Fighter[enemies.size() + 1];
            options = new String[enemies.size() + 1];
            int i = 0;
            for (EnemyFighter f : enemies) {
                fighters[i] = f;
                options[i] = f.displayName;
                i++;
            }
            fighters[i] = BattleManager.getPlayer();
            options[i] = "Self";
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getNumElements() {
        return options.length;
    }

    @Override
    public String getElementName(int index) {
        return options[index];
    }

    @Override
    public void onAction(int index) {
        Fighter[] f;
        if (multiTarget)
            f = BattleManager.getEnemies().toArray(new Fighter[0]);
        else {
            f = new Fighter[1];
            f[0] = fighters[index];
        }
        callback.accept(f);
    }

    @Override
    public void onHighlight(int index) {

    }

    @Override
    public void onFocus() {
        handler.setFlavorTextLock(true);
    }

    @Override
    public void onClose() { handler.closeMenu(); }
    @Override
    public void onCloseCleanup() { handler.setFlavorTextLock(false); }
}
