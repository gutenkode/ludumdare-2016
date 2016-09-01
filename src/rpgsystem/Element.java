package rpgsystem;

/**
 * Elements for attacks.
 * @author Peter
 */
public enum Element {
        PHYS(0, new float[] {.2f,.1f,.1f}),
        FIRE(1, new float[] {1,.3f,0}),
        ELEC(2, new float[] {1,1,0}),
        ICE(3,  new float[] {.65f,.65f,1}),
        NONE(4, new float[] {1,1,1});
        
        public final int index;
        public final float[] color;
        Element(int i, float[] c) {
            index = i;
            color = c;
        }
    }
