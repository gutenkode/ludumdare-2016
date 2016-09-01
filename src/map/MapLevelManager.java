package map;

/**
 * Master control for managing maps and levels.  Map groups are split into levels,
 * which are defined in a index.txt and split by directories.  This class will
 * tell MapManger when to load a new set of levels, and which map to load first
 * in a set.
 * @author Peter
 */
public class MapLevelManager {
    /*
    public static final int MAX_LEVELS = 2;
    private static final String[] DIRS = new String[] {"_mylevel1","old"};
    private static final String[] FIRSTROOM = new String[] {"1a_start","newStart"};
    private static final int[][] PLAYERPOS = new int[][] {{2,5},{2,5}};
    */
    
    public static final int MAX_LEVELS = 4;
    private static final String[] DIRS = new String[] {"Level1","Level2","Level3","Level4"};
    private static final String[] FIRSTROOM = new String[] {"Room1","Room1","Room1","Room1"};
    private static final int[][] PLAYERPOS = new int[][] {{4,4},{4,4},{4,4},{4,4}};
    
    private static int currentLevelNum = -1;
    
    public static int getCurrentLevel() { return currentLevelNum; }
    public static void incrementCurrentLevel() { setCurrentLevel(currentLevelNum+1); }
    /**
     * Sets the current level.  Must be called before the game begins running.
     * It will initialize MapManager and MapLoader, creating timelines, loading
     * the default map file, and constructing the player/other entities.
     * @param i 
     */
    public static void setCurrentLevel(int i) {
        if (i > 0 && i <= MAX_LEVELS) {
            currentLevelNum = i; 
            MapLoader.setLevelPath(DIRS[currentLevelNum-1]);
            MapManager.createNewTimelines(FIRSTROOM[currentLevelNum-1]);
        }
    }
    
    public static int[] playerStartPos() {
        return PLAYERPOS[currentLevelNum-1];
    }
}
