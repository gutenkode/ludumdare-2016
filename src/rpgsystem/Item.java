package rpgsystem;

import rpgbattle.BattleManager;
import rpgbattle.fighter.Fighter;
import static rpgsystem.DefaultTarget.*;
import ui.BattleUIManager;
import ui.MenuHandler;

/**
 *
 * @author Peter
 */
public enum Item {
    KEYCARD1("Lv1 Keycard",
        "A keycard with the text \"Level 1\" written\non it.",
        "item_keycard1", NO_TARGET, false),
    KEYCARD2("Lv2 Keycard",
        "A keycard with the text \"Level 2\" written\non it.",
        "item_keycard2", NO_TARGET, false),
    KEYCARD3("Lv3 Keycard",
        "A keycard with the text \"Level 3\" written\non it.",
        "item_keycard3", NO_TARGET, false),
    KEYCARD4("Lv4 Keycard",
        "A keycard with the text \"Level 4\" written\non it.",
        "item_keycard4", NO_TARGET, false),

    POTION("Potion",
        "A small purple flask.\nRestores a fair amount of health.",
        "item_potion", PLAYER, true),
    ENERGY_DRINK("Energy Drink",
        "Linked to several heart conditions.\nRestores a fair amount of stamina.",
        "item_drink", PLAYER, true),
    MANA_HYPO("Mana Hypo",
        "A syringe filled with a glowing blue liquid.\nRestores a fair amount of mana.",
        "item_hypo", PLAYER, true),
    GRENADE("Grenade",
        "An explosive weapon.\nDeals damage to enemies.",
        "item_grenade", ENEMY, true);

    public final String name, desc, spriteName;
    public final boolean canDiscard;
    public final DefaultTarget defaultTarget;
    Item(String n, String d, String s, DefaultTarget t, boolean dis) {
        name = n;
        desc = d;
        spriteName = s;
        canDiscard = dis;
        defaultTarget = t;
    }
    
    /**
     * Use an item in the overworld.
     * @param handler
     * @return Whether the item was used.
     */
    public boolean useIngame(MenuHandler handler) {
        switch (this) {
            case KEYCARD1:
            case KEYCARD2:
                handler.showDialogue("The card will open nearby\ndoors automatically.", this.spriteName);
                return false;
                
            case POTION:
                if (BattleManager.getPlayer().restoreHealth(25)) {
                    discard();
                    handler.closeMenu();
                    handler.showDialogue("You drink the potion.\nDelicious!");
                    return true;
                }
                handler.showDialogue("Your health is full!", this.spriteName);
                return false;
            case ENERGY_DRINK:
                if (BattleManager.getPlayer().restoreStamina(60)) {
                    BattleUIManager.logMessage("You down the whole can.");
                    discard();
                    handler.closeMenu();
                    handler.showDialogue("You down the whole can.");
                    return true;
                }
                handler.showDialogue("Your stamina is full!", spriteName);
                return false;
            case MANA_HYPO:
                if (BattleManager.getPlayer().restoreMana(40)) {
                    BattleUIManager.logMessage("You inject the contents of the hypo.");
                    discard();
                    handler.closeMenu();
                    handler.showDialogue("You inject the contents of the hypo.");
                    return true;
                }
                handler.showDialogue("Your mana is full!", spriteName);
                return false;
                
            default:
                handler.showDialogue("You can't use this here.", this.spriteName);
                return false;
        }
    }
    /**
     * Use an item during a battle.
     * @param handler
     * @param fighter
     * @return Whether the item was used.
     */
    public boolean useBattle(MenuHandler handler, Fighter fighter) {
        switch (this) {
            case POTION:
                if (fighter.restoreHealth(25)) {
                    BattleUIManager.logMessage("You drink the potion. Delicious!");
                    discard();
                    return true;
                }
                handler.showDialogue("Your health is full!", spriteName);
                return false;
            case GRENADE:
                BattleUIManager.logMessage("You throw the grenade! Boom!");
                fighter.damage(Element.EXPLOSIVE, 40, 10, 100, false);
                discard();
                return true;
            case ENERGY_DRINK:
                if (fighter.restoreStamina(60)) {
                    BattleUIManager.logMessage("You down the whole can.");
                    discard();
                    return true;
                }
                handler.showDialogue("Your stamina is full!", spriteName);
                return false;
            case MANA_HYPO:
                if (fighter.restoreMana(40)) {
                    BattleUIManager.logMessage("You inject the contents of the hypo.");
                    discard();
                    return true;
                }
                handler.showDialogue("Your mana is full!", spriteName);
                return false;
            default:
                handler.showDialogue("You can't use this here.", spriteName);
                return false;
        }
    }
    public void discard() {
        if (canDiscard)
            Inventory.get().remove(this);
    }
}

