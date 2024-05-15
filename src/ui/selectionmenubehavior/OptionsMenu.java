package ui.selectionmenubehavior;

import mote4.scenegraph.Window;
import mote4.util.audio.AudioPlayback;
import main.Vars;
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
        options[3] = "Filtering: "+Vars.currentFilter().NAME;
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
                if (System.getProperty("os.name").toLowerCase().contains("mac"))
                    AudioPlayback.playSfx("sfx_menu_invalid");
                else {
                    if (Window.isFullscreen())
                        Window.setWindowedPercent(.75, 16 / 9.0);
                    else
                        Window.setFullscreen();
                    AudioPlayback.playSfx("sfx_menu_select");
                }
                break;
            case 1: // vsync
                Window.setVsync(!Window.isVsyncEnabled());
                refreshOptions();
                handler.forceMenuRefocus();
                AudioPlayback.playSfx("sfx_menu_select");
                break;
            case 2: // supersample
                Vars.setSSAA(!Vars.useSSAA());
                refreshOptions();
                handler.forceMenuRefocus();
                AudioPlayback.playSfx("sfx_menu_select");
                break;
            case 3: // filter
                Vars.cycleFilters();
                refreshOptions();
                handler.forceMenuRefocus();
                AudioPlayback.playSfx("sfx_menu_select");
                break;
            default:
                handler.closeMenu();
                break;
        }
    }

    @Override
    public void onHighlight(int index) {
        switch(index) {
            case 0: // fullscreen
                handler.showFlavorText(false, "Toggle fullscreen.\nOn macOS, use the maximize button.");
                break;
            case 1: // vsync
                handler.showFlavorText(false, "Toggle between vsync\nor locked 60fps.");
                break;
            case 2: // supersample
                handler.showFlavorText(false, "Supersample Antialiasing\nHigh-quality antialiasing, smooths\nedges and increases detail.");
                break;
            case 3: // filter
                handler.showFlavorText(false, "Change filtering modes.");
                break;
            default:
                handler.closeFlavorText();
                break;
        }
    }

    @Override
    public void onFocus() {}

    @Override
    public void onClose() {
        handler.closeMenu();
    }
    @Override
    public void onCloseCleanup() {
        handler.closeFlavorText();
    }
}
