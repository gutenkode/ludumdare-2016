package ui.components.selectionMenu;

import main.Input;
import main.Vars;
import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.TransformationMatrix;
import ui.selectionmenubehavior.SelectionMenuBehavior;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains multiple behaviors, arranged as tabs in one menu pane.
 * Useful for menus with separate categories, like different types of items.
 * Created by Peter on 6/30/17.
 */
public class TabbedSelectionMenu implements SelectionMenu {

    private List<SingleSelectionMenu> menus;
    private float[] heights;
    private int currentMenu;

    public TabbedSelectionMenu(SelectionMenuBehavior... behaviors) {
        currentMenu = 0;
        menus = new ArrayList<>();
        for (SelectionMenuBehavior b : behaviors)
            menus.add(new SingleSelectionMenu(b));
        menus.forEach(menu -> menu.onFocus());
        heights = new float[menus.size()];
    }

    public void setCursorPos(int i) { menus.get(currentMenu).setCursorPos(i); }
    public int cursorPos()  { return menus.get(currentMenu).cursorPos(); }

    public int width() { return menus.get(currentMenu).width() + currentMenu*Vars.UI_SCALE; }
    public int height() { return menus.get(currentMenu).height(); }

    public void onFocus() { menus.get(currentMenu).onFocus(); }

    public void update() {
        for (int i = 0; i < heights.length; i++) {
            if (i == currentMenu)
                heights[i] += (Vars.UI_SCALE*.5f - heights[i])/10;
            else
                heights[i] -= heights[i]/10;
        }

        if (Input.isKeyNew(Input.Keys.RIGHT)) {
            currentMenu++;
            currentMenu %= menus.size();
            menus.get(currentMenu).onFocus();
            AudioPlayback.playSfx("sfx_menu_hover");
        } else if (Input.isKeyNew(Input.Keys.LEFT)) {
            currentMenu--;
            if (currentMenu < 0)
                currentMenu = menus.size()-1;
            menus.get(currentMenu).onFocus();
            AudioPlayback.playSfx("sfx_menu_hover");
        }
        menus.get(currentMenu).update();
    }
    public void render(TransformationMatrix model) {
        model.push();
        int i = 0;
        for (SingleSelectionMenu m : menus) {
            if (currentMenu != i) {
                model.push();
                model.translate(0, heights[i]);
                m.render(model);
                model.pop();
            }
            model.translate(Vars.UI_SCALE,0);
            i++;
        }
        model.pop();

        model.translate(currentMenu*Vars.UI_SCALE, heights[currentMenu]);
        menus.get(currentMenu).render(model);
    }

    public void destroy() { menus.forEach(menu -> menu.destroy()); }
}
