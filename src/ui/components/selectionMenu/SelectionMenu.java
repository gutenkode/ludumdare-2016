package ui.components.selectionMenu;

import mote4.util.matrix.ModelMatrix;

/**
 * A SelectionMenu is the core component of the player's UI.  It displays a list
 * of options the player can choose from.  This can be used for pause menus and
 * item selection menus.
 * @author Peter
 */
public interface SelectionMenu {

    void setCursorPos(int i);
    int cursorPos();

    int width();
    int height();

    void onFocus();

    void update();
    void render(ModelMatrix model);

    void destroy();
}