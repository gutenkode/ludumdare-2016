package ui.selectionmenubehavior;

import mote4.scenegraph.Window;
import ui.MenuHandler;

/**
 * Created by Peter on 9/19/16.
 */
public class OptionsMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "OPTIONS";
    private String[] options = {"Toggle Fullscreen","<vsync>","Exit"};

    public OptionsMenu(MenuHandler h) {
        handler = h;
        refreshOptions();
    }
    private void refreshOptions() {
        if (Window.isVsyncEnabled())
            options[1] = "Disable Vsync";
        else
            options[1] = "Enable Vsync";
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
        switch(index) {
            case 0:
                if (Window.isFullscreen())
                    Window.setWindowedPercent(.75, 16/9.0);
                else
                    Window.setFullscreen();
                break;
            case 1:
                Window.setVsync(!Window.isVsyncEnabled());
                refreshOptions();
                handler.forceMenuRefocus();
                break;
            default:
                handler.closeMenu();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {}

    @Override
    public void onFocus() {}

    @Override
    public void onClose() {
        handler.closeMenu();
    }
    @Override
    public void onCloseCleanup() {}
}
