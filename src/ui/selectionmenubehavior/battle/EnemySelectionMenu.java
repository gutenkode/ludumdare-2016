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
    private ArrayList<String> options;
    private ArrayList<Fighter> fighters;
    private boolean multiTarget;

    public EnemySelectionMenu(MenuHandler h, Consumer<Fighter[]> cb, boolean mt, boolean inclEnemies, boolean inclPlayer) {
        handler = h;
        callback = cb;
        multiTarget = mt;

        if (multiTarget) {
            options = new ArrayList<String>();
            options.add("All");
        } else {
            fighters = new ArrayList<>();
            options = new ArrayList<>();
            if (inclEnemies) {
                ArrayList<EnemyFighter> enemies = BattleManager.getEnemies();
                for (EnemyFighter f : enemies) {
                    fighters.add(f);
                    options.add(f.displayName);
                }
            }
            if (inclPlayer) {
                fighters.add(BattleManager.getPlayer());
                options.add("Player");
            }
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getNumElements() {
        return options.size();
    }

    @Override
    public String getElementName(int index) { return options.get(index); }

    @Override
    public void onAction(int index) {
        Fighter[] f;
        if (multiTarget)
            f = BattleManager.getEnemies().toArray(new Fighter[0]);
        else {
            f = new Fighter[1];
            f[0] = fighters.get(index);
        }
        callback.accept(f);
    }

    @Override
    public void onHighlight(int index) {}

    @Override
    public void onFocus() {
        handler.setFlavorTextLock(true);
    }

    @Override
    public void onClose() { handler.closeMenu(); }
    @Override
    public void onCloseCleanup() { handler.setFlavorTextLock(false); }
}
