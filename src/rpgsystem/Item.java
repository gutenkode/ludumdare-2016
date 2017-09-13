package rpgsystem;

import mote4.util.audio.AudioPlayback;
import rpgbattle.BattleManager;
import rpgbattle.fighter.Fighter;
import ui.MenuHandler;
import ui.components.BattleAnimation;

import static rpgsystem.BattleEffect.*;
import static rpgsystem.Item.ItemType.*;

/**
 * Items and their properties.
 * This is currently more monolithic than I'd like...
 * @author Peter
 */
public enum Item implements Pickupable {

    KEYCARD1("Lv1 Keycard",
        "A keycard with the text\n\"Level 1\" written on it.",
        "item_keycard1", null, null, NONE, KEY),
    KEYCARD2("Lv2 Keycard",
        "A keycard with the text\n\"Level 2\" written on it.",
        "item_keycard2", null, null, NONE, KEY),
    KEYCARD3("Lv3 Keycard",
        "A keycard with the text\n\"Level 3\" written on it.",
        "item_keycard3", null, null, NONE, KEY),
    KEYCARD4("Lv4 Keycard",
        "A keycard with the text\n\"Level 4\" written on it.",
        "item_keycard4", null, null, NONE, KEY),

    POTION("Potion",
            "A small purple flask.\nRestores "+Const.HEALTH_HEAL+" health.",
            "item_potion", "You drink the potion.", "Your health is full!",
            HEAL, CONSUMABLE),
    ENERGY_DRINK("Energy Drink",
            "Linked to several heart conditions.\nRestores "+Const.STAMINA_HEAL+" stamina.",
            "item_drink", "You down the whole can.", "Your stamina is full!",
            HEAL, CONSUMABLE),
    MANA_HYPO("Mana Hypo",
            "A syringe filled with a glowing blue liquid.\nRestores "+Const.MANA_HEAL+" mana.",
            "item_hypo", "You inject the contents of the hypo.", "Your mana is full!",
            HEAL, CONSUMABLE),
    GRENADE("Grenade",
            "An explosive weapon.\nDeals high damage to all enemies.",
            "item_grenade", "You throw the grenade!", "You can't use this right now.",
            ATTACK_ALL, CONSUMABLE);

    public final String name, desc, spriteName, useString, noUseString;
    public final ItemType itemType;
    public final BattleEffect battleEffect;
    Item(String n, String d, String s, String use, String noUse, BattleEffect t, ItemType i) {
        name = n;
        desc = d;
        spriteName = s;
        useString = use;
        noUseString = noUse;
        itemType = i;
        battleEffect = t;
    }

    private static class Const {
        public static final int
                HEALTH_HEAL = 75,
                STAMINA_HEAL = 100,
                MANA_HEAL = 50,
                BOMB_POWER = 100;
    }
    public enum ItemType {
        CONSUMABLE,
        KEY;
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
            case KEYCARD3:
            case KEYCARD4:
                handler.showDialogue("The card will open nearby\ndoors automatically.", this.spriteName);
                return false;
                
            case POTION:
                if (BattleManager.getPlayer().restoreHealth(Const.HEALTH_HEAL)) {
                    discard();
                    handler.closeMenu();
                    handler.showDialogue(this.useString);
                    AudioPlayback.playSfx("sfx_skill_heal");
                    return true;
                }
                handler.showDialogue(this.noUseString, this.spriteName);
                AudioPlayback.playSfx("sfx_menu_invalid");
                return false;

            case ENERGY_DRINK:
                if (BattleManager.getPlayer().restoreStamina(Const.STAMINA_HEAL)) {
                    discard();
                    handler.closeMenu();
                    handler.showDialogue(this.useString);
                    AudioPlayback.playSfx("sfx_skill_heal");
                    return true;
                }
                handler.showDialogue(this.noUseString, spriteName);
                AudioPlayback.playSfx("sfx_menu_invalid");
                return false;

            case MANA_HYPO:
                if (BattleManager.getPlayer().restoreMana(Const.MANA_HEAL)) {
                    discard();
                    handler.closeMenu();
                    handler.showDialogue(this.useString);
                    AudioPlayback.playSfx("sfx_skill_heal");
                    return true;
                }
                handler.showDialogue(this.noUseString, spriteName);
                AudioPlayback.playSfx("sfx_menu_invalid");
                return false;
                
            default:
                handler.showDialogue("You can't use this here.", this.spriteName);
                AudioPlayback.playSfx("sfx_menu_invalid");
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
                if (fighter.restoreHealth(Const.HEALTH_HEAL)) {
                    AudioPlayback.playSfx("sfx_skill_heal");
                    discard();
                    return true;
                }
                throw new IllegalStateException("Attempted to use item in battle that cannot be used.");

            case GRENADE:
                fighter.damage(Element.BOMB, Const.BOMB_POWER, 10, 100, false);
                fighter.addAnim(new BattleAnimation(BattleAnimation.Type.FIRE));
                AudioPlayback.playSfx("sfx_skill_bomb");
                discard();
                return true;

            case ENERGY_DRINK:
                if (fighter.restoreStamina(Const.STAMINA_HEAL)) {
                    AudioPlayback.playSfx("sfx_skill_heal");
                    discard();
                    return true;
                }
                throw new IllegalStateException("Attempted to use item in battle that cannot be used.");

            case MANA_HYPO:
                if (fighter.restoreMana(Const.MANA_HEAL)) {
                    AudioPlayback.playSfx("sfx_skill_heal");
                    discard();
                    return true;
                }
                throw new IllegalStateException("Attempted to use item in battle that cannot be used.");

            default:
                throw new IllegalStateException("Attempted to use item in battle that cannot be used.");
        }
    }
    public boolean checkCanUseInBattle(MenuHandler handler, Fighter f) {
        switch (this) {
            case POTION:
                if (f.stats.health == f.stats.maxHealth) {
                    handler.showDialogue(this.noUseString);
                    AudioPlayback.playSfx("sfx_menu_invalid");
                    return false;
                }
                return true;
            case ENERGY_DRINK:
                if (f.stats.stamina == f.stats.maxStamina) {
                    handler.showDialogue(this.noUseString);
                    AudioPlayback.playSfx("sfx_menu_invalid");
                    return false;
                }
                return true;
            case GRENADE:
                return true;
            case MANA_HYPO:
                if (f.stats.mana == f.stats.maxMana) {
                    handler.showDialogue(this.noUseString);
                    AudioPlayback.playSfx("sfx_menu_invalid");
                    return false;
                }
                return true;
            default:
                handler.showDialogue("Can't use this right now.");
                AudioPlayback.playSfx("sfx_menu_invalid");
                return false;
        }
    }
    public void discard() {
        if (itemType == CONSUMABLE)
            Inventory.removeItem(this);
    }

    @Override
    public String pickupName() { return name; };
    @Override
    public String overworldSprite() { return spriteName; };
    @Override
    public void pickup() { Inventory.addItem(this); }
}

