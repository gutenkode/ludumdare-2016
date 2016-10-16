package ui.selectionmenubehavior;

/**
 * Defines the content and behavior of a SelectionMenu.
 * @author Peter
 */
public interface SelectionMenuBehavior {
    /**
     * Tile of this menu.
     * @return 
     */
    public String getTitle();
    /**
     * Number of entries in this menu.
     * @return 
     */
    public int getNumElements();
    /**
     * The name of the specified element.
     * @param index
     * @return 
     */
    public String getElementName(int index);
    
    /**
     * Perform the action for the specified element.
     * @param index 
     */
    public void onAction(int index);
    /**
     * To be called when the cursor is moved to this element.
     * @param index 
     */
    public void onHighlight(int index);
    /**
     * To be called when this menu is focused, including on creation.
     */
    public void onFocus();
    /**
     * To be called when this menu is given the signal to close.
     * If this menu should close, it must explicitly call closeMenu() from a manager.
     * State cleanup code should NOT be placed here as it will not always be called,
     * if this menu is closed from elsewhere.
     */
    public void onClose();
    /**
     * Called when this menu is being closed.  Any cleanup or state reset code should go here,
     * as it will always reliably be called when the menu is closing.
     */
    public void onCloseCleanup();
}