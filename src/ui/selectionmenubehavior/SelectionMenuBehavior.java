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
     * To be called when this menu is closed.
     */
    public void onClose();
}