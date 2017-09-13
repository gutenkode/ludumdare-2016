package rpgsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores the player's inventory.
 * @author Peter
 */
public class Inventory {
    
    private static Map<Item, Integer> inventory;
    
    static {
        inventory = new TreeMap<>();
        inventory.put(Item.KEYCARD4, 1);
        inventory.put(Item.POTION, 2);
        inventory.put(Item.ENERGY_DRINK, 3);
        inventory.put(Item.MANA_HYPO, 1);
        inventory.put(Item.GRENADE, 1);
    }
    
    public static Map<Item, Integer> get() { return inventory; }
    public static void addItem(Item i) {
        switch (i) {
            // you can only have one keycard at a time
            // you can only collect one of every level,
            // and higher-level keycards will remove lower-level keycards
            case KEYCARD4:
                if (!inventory.containsKey(i)) {
                    inventory.put(i, 1);
                    inventory.remove(Item.KEYCARD3);
                    inventory.remove(Item.KEYCARD2);
                    inventory.remove(Item.KEYCARD1);
                }
                break;
            case KEYCARD3:
                if (!inventory.containsKey(i) &&
                    !inventory.containsKey(Item.KEYCARD4)) {
                    inventory.put(i, 1);
                    inventory.remove(Item.KEYCARD2);
                    inventory.remove(Item.KEYCARD1);
                }
                break;
            case KEYCARD2:
                if (!inventory.containsKey(i)&&
                    !inventory.containsKey(Item.KEYCARD3) &&
                    !inventory.containsKey(Item.KEYCARD4)) {
                    inventory.put(i, 1);
                    inventory.remove(Item.KEYCARD1);
                }
                break;
            case KEYCARD1:
                if (!inventory.containsKey(i) &&
                    !inventory.containsKey(Item.KEYCARD2) &&
                    !inventory.containsKey(Item.KEYCARD3) &&
                    !inventory.containsKey(Item.KEYCARD4))
                    inventory.put(i, 1);
                break;
            default:
                // add 1 to the number of this item, or add it and set amount to 1
                inventory.put(i, inventory.getOrDefault(i, 0)+1);
                break;
        }
        // TreeMap sorts automatically
        //Collections.sort(inventory);
    }
    public static void removeItem(Item i) {
        if (inventory.containsKey(i)) {
            int amount = inventory.get(i);
            amount--;
            if (amount > 0)
                inventory.put(i, amount);
            else
                inventory.remove(i);
        }
    }
    public static void removeItem(String s) {
        Item removeItem = null;
        for (Item i : inventory.keySet()) {
            if (i.name.toLowerCase().equals(s.toLowerCase())) {
                removeItem = i;
                break;
            }
        }
        if (removeItem != null)
            removeItem(removeItem);
    }
    public static boolean hasItem(Item i) { return inventory.containsKey(i); }
    public static boolean hasItem(String s) {
        for (Item i : inventory.keySet()) {
            if (i.name.toLowerCase().equals(s.toLowerCase()))
                return true;
        }
        return false;
    }
}