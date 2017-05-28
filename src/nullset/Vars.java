package nullset;

import mote4.scenegraph.Window;
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

    private static boolean filterScreen = true;
    public static void setFiltering(boolean enable) {
        filterScreen = enable;
        Postprocess.initFinalPassShader();
    }
    public static boolean useFiltering() { return filterScreen; }
}
