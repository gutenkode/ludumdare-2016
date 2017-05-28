package rpgsystem;

/**
 * Elements for attacks.
 * The float array is the rgb color associated with that element.
 * @author Peter
 */
public enum Element {
    PHYS(0, new float[] {.2f,.1f,.1f}),
    FIRE(1, new float[] {1,.3f,0}),
    ELEC(2, new float[] {1,1,0}),
    ICE(3,  new float[] {.65f,.65f,1}),
    FORCE(4, new float[] {0,.9f,.1f}),
    ACID(5, new float[] {1,0,.9f}),
    RUIN(6, new float[] {.6f,.5f,.5f}),
    RADIANT(7, new float[] {.9f,.9f,1}),
    EXPLOSIVE(8, new float[] {1,.6f,.1f});

    public final int index;
    public final float[] color;
    Element(int i, float[] c) {
        index = i;
        color = c;
    }
}
