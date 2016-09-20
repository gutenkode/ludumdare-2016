package ui;

import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * A MenuHandler can receive requests from a SelectionMenuBehavior and
 * open/close menus and other UI elements.
 * @author Peter
 */
public interface MenuHandler 
{
    public void openMenu(SelectionMenuBehavior sm);
    public void setMenuCursorPos(int i);
    /**
     * Calls onFocus() again for the current menu, potentially changing the displayed text.
     */
    public void forceMenuRefocus();
    public void closeMenu();
    
    public void showDialogue(String s);
    public void showDialogue(String s, String sprite);
    public void closeDialogue();
    
    public void displayScriptChoice(String[] s);
    public void loadScriptLine(String s);
    /**
     * End playing a script.
     * @param b Whether the script is finished.  If false, it will reset.
     */
    public void endScript(boolean b);
    
    public void showFlavorText(boolean lock, String s);
    public void showFlavorText(boolean lock, String s, String sprite);
    public void closeFlavorText();
}