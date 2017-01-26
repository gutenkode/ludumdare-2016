package rpgsystem;

import java.util.ArrayList;

/**
 * Stores the player's inventory.
 * @author Peter
 */
public class Inventory {
    
    private static ArrayList<Item> inventory;
    
    static {
        inventory = new ArrayList<>();
        //inventory.add(Item.KEYCARD1);
        inventory.add(Item.POTION);
        inventory.add(Item.ENERGY_DRINK);
        inventory.add(Item.MANA_HYPO);
        inventory.add(Item.GRENADE);
    }
    
    public static ArrayList<Item> get() { return inventory; }
    public static void addItem(Item i) {
        switch (i) {
            // you can only have one keycard at a time
            // you can only collect one of every level,
            // and higher-level keycards will remove lower-level keycards
            case KEYCARD4:
                if (!inventory.contains(i)) {
                    inventory.add(i);
                    inventory.remove(Item.KEYCARD3);
                    inventory.remove(Item.KEYCARD2);
                    inventory.remove(Item.KEYCARD1);
                }
                break;
            case KEYCARD3:
                if (!inventory.contains(i) &&
                    !inventory.contains(Item.KEYCARD4)) {
                    inventory.add(i);
                    inventory.remove(Item.KEYCARD2);
                    inventory.remove(Item.KEYCARD1);
                }
                break;
            case KEYCARD2:
                if (!inventory.contains(i)&&
                    !inventory.contains(Item.KEYCARD3) &&
                    !inventory.contains(Item.KEYCARD4)) {
                    inventory.add(i);
                    inventory.remove(Item.KEYCARD1);
                }
                break;
            case KEYCARD1:
                if (!inventory.contains(i) &&
                    !inventory.contains(Item.KEYCARD2) &&
                    !inventory.contains(Item.KEYCARD3) &&
                    !inventory.contains(Item.KEYCARD4))
                    inventory.add(i);
                break;
            default:
                inventory.add(i); 
                break;
        }
    }
    public static void removeItem(Item i){ inventory.remove(i); }
    public static void removeItem(String s){
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).name.toLowerCase().equals(s.toLowerCase())){
                inventory.remove(i);
                break;
            }
        }
    }
    public static boolean hasItem(Item i) { return inventory.contains(i); }
    public static boolean hasItem(String s){
        boolean ret = false;
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).name.toLowerCase().equals(s.toLowerCase())) ret = true;
        }
        return ret;
    }
}