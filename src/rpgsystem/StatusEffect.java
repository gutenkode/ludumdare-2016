package rpgsystem;

/**
 * Status effects.
 * @author Peter
 */
public enum StatusEffect {
    POISON("Poison","Does HP damage after every turn.", "status_poison"),
    FATIGUE("Fatigue","SP is lost instead of gained every turn.", "status_fatigue");

    public final String name, desc, spriteName;
    StatusEffect(String n, String d, String s) {
        name = n;
        desc = d;
        spriteName = s;
        // TODO add color variable
    }
}