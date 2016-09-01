package rpgsystem;

/**
 * Status effects.
 * @author Peter
 */
public enum StatEffect {
    POISON("Poison","Does HP damage after every turn.", "status_poison"),
    FATIGUE("Fatigue","SP is lost instead of gained every turn.", "status_fatigue"),
    DEBILITATE("Debilitate","All stats are lowered.", "status_debilitate");

    public final String name, desc, spriteName;
    StatEffect(String n, String d, String s) {
        name = n;
        desc = d;
        spriteName = s;
        // TODO add color variable
    }
}