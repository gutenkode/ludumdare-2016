package main;

import scenes.Postprocess;

/**
 *
 * @author Peter
 */
public class Vars {
    // tilesheet size, number of tiles on each axis
    public static final float TILESHEET_X = 8, 
                              TILESHEET_Y = 8;
    // width of one tile out of a scale of 1 on the tilesheet
    public static final float TILE_SIZE_X = 1f/TILESHEET_X,
                              TILE_SIZE_Y = 1f/TILESHEET_Y;
    public static final int UI_SCALE = 16,
                            WINDOW_HEIGHT = 1080/3;

    private static boolean ssaa = true;
    public static void setSSAA(boolean enable) {
        ssaa = enable;
        Nullset_Ludumdare.rootLayer().refreshFramebuffer();
    }
    public static boolean useSSAA() { return ssaa; }

    public enum Filter {
        NEAREST("None"),LINEAR("Linear"),QUILEZ("Smooth"),CRT("CRT");
        public final String NAME;
        Filter(String n) {NAME = n;}
    }
    private static Filter currentFilter = Filter.QUILEZ;
    public static void setFilter(Filter filter) {
        currentFilter = filter;
        Postprocess.initFinalPassShader();
    }
    public static void cycleFilters() {
        switch (currentFilter) {
            case NEAREST: setFilter(Filter.LINEAR); break;
            case LINEAR: setFilter(Filter.QUILEZ); break;
            case QUILEZ: setFilter(Filter.CRT); break;
            case CRT: setFilter(Filter.NEAREST); break;
            default: throw new IllegalStateException();
        }
    }
    public static Filter currentFilter() { return currentFilter; }

    /**
     * Linearly interpolate between two values.
     * @param a
     * @param b
     * @param i
     * @return
     */
    public static double lerp(double a, double b, double i) {
        double step = clamp(0,1,i);
        return a*(1-step)+b*step;
    }
    /**
     * Calculates a smooth step value based on timestamps.
     * @param startTime
     * @param endTime
     * @param currentTime
     * @return
     */
    public static double smoothStep(double startTime, double endTime, double currentTime) {
        currentTime -= startTime;
        currentTime = currentTime/(endTime-startTime);
        return smoothStep(currentTime);
    }
    /**
     * Returns a smoothly interpolated value based on a step value of range 0-1.
     * @param i
     * @return
     */
    public static double smoothStep(double i) {
        double step = clamp(0,1,i);
        return clamp(0,1,(Math.sin(step*Math.PI -Math.PI/2)+1)/2);
    }
    /**
     * Clamps a value between a min and max.
     * @param min
     * @param max
     * @param i
     * @return
     */
    public static double clamp(double min, double max, double i) {
        return Math.min(max,Math.max(min,i));
    }
}
