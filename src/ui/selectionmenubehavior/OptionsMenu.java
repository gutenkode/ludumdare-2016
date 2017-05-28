package ui.selectionmenubehavior;

import mote4.scenegraph.Window;
import nullset.Vars;
import ui.MenuHandler;

/**
 * Created by Peter on 9/19/16.
 */
public class OptionsMenu implements SelectionMenuBehavior {

    private MenuHandler handler;

    private String title = "OPTIONS";
    private String[] options = {"Toggle Fullscreen","<vsync>","<supersample>","<filter>","Exit"};

    public OptionsMenu(MenuHandler h) {
        handler = h;
        refreshOptions();
    }
    private void refreshOptions() {
        if (Window.isVsyncEnabled())
            options[1] = "Vsync: On";
        else
            options[1] = "Vsync: Off";
        if (Vars.useSSAA())
            options[2] = "SSAA: On";
        else
            options[2] = "SSAA: Off";
        if (Vars.useFiltering())
            options[3] = "Filtering: On";
        else
            options[3] = "Filtering: Off";
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
            case 0: // fullscreen
                if (Window.isFullscreen())
                    Window.setWindowedPercent(.75, 16/9.0);
                else
                    Window.setFullscreen();
                break;
            case 1: // vsync
                Window.setVsync(!Window.isVsyncEnabled());
                refreshOptions();
                handler.forceMenuRefocus();
                break;
            case 2: // supersample
                Vars.setSSAA(!Vars.useSSAA());
                refreshOptions();
                handler.forceMenuRefocus();
                break;
            case 3: // filter
                Vars.setFiltering(!Vars.useFiltering());
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
