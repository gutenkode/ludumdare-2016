package ui.components.selectionMenu;

import mote4.util.matrix.TransformationMatrix;

/**
 * A SelectionMenu is the core component of the player's UI; it displays a list
 * of options the player can choose from.  This can be used for pause menus,
 * item selection menus, etc.
 * @author Peter
 */
public interface SelectionMenu {

    void setCursorPos(int i);
    int cursorPos();

    int width();
    int height();

    void onFocus();

    void update();
    void render(TransformationMatrix model);

    void destroy();
}