package rpgsystem;

/**
 * Elements for attacks.
 * The float array is the rgb color associated with that element.
 * @author Peter
 */
public enum Element {
    PHYS(new float[] {.2f,.1f,.1f}),
    BOMB(new float[] {1,.6f,.1f}),

    FIRE(new float[] {1,.3f,0}),
    ELEC(new float[] {1,1,0}),
    ICE(new float[] {.65f,.65f,1}),

    ACID(new float[] {1,0,.9f}),
    DARK(new float[] {.6f,.5f,.5f}),
    LIGHT(new float[] {.9f,.9f,1});

    public final float[] color;
    Element(float[] c) {
        color = c;
    }

    /**
     * Resistances to elemental types.
     */
    public enum Resistance {
        N,      // normal damage
        RES,    // resist - less damage
        WEAK,   // weak - more damage
        NULL    // null - no damage
    }
}


