package ui.selectionmenubehavior;

import ui.MenuHandler;

import java.util.function.Consumer;

/**
 * Created by Peter on 1/20/17.
 */
public class ConfirmMenu implements SelectionMenuBehavior {

    private MenuHandler handler;
    private Consumer callback;

    private String title;
    private String[] options = {"Yes","No"};

    public ConfirmMenu(MenuHandler h, String t, Consumer<Boolean> cb) {
        title = t;
        callback = cb;
        handler = h;
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
        if (index == 0)
            callback.accept(true);
        else
            callback.accept(false);
        handler.closeMenu();
    }

    @Override
    public void onHighlight(int index) {}

    @Override
    public void onFocus() {
        handler.setMenuCursorPos(1);
    }

    @Override
    public void onClose() {
        handler.closeMenu();
    }
    @Override
    public void onCloseCleanup() {}
}
