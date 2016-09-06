package map;

import mote4.scenegraph.Window;
import mote4.util.FileIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Master control for managing maps and levels.  Map groups are split into levels,
 * which are defined in a index.txt and split by directories.  This class will
 * tell MapManger when to load a new set of levels, and which map to load first
 * in a set.
 * @author Peter
 */
public class MapLevelManager {

    private static int numLevels;
    private static String[] levelDirs;
    private static String[] firstMapName;
    private static int[][] playerInitPos;

    private static int currentLevelNum = -1;

    /**
     * Load an index file describing levels to load.
     * This does not load a level, only the metadata.
     * @param fileName
     */
    public static void loadIndexFile(String fileName) {
        BufferedReader bf = FileIO.getBufferedReader("/res/maps/"+fileName);
        ArrayList<String> file = new ArrayList<>();
        try {
            String inString;
            while ((inString = bf.readLine()) != null)
                file.add(inString);
            bf.close();
        } catch (IOException e) {
            System.err.println("Error loading index file '"+fileName+"':");
            e.printStackTrace();
            Window.destroy();
        }

        numLevels = file.size();
        for (String s : file)
            if (s.startsWith("#"))
                numLevels--; // ignore comments
        levelDirs = new String[numLevels];
        firstMapName = new String[numLevels];
        playerInitPos = new int[numLevels][2];
        for (String s : file) {
            if (!s.startsWith("#")) {
                StringTokenizer tok = new StringTokenizer(s, ",");
                int i = Integer.parseInt(tok.nextToken())-1;
                levelDirs[i] = tok.nextToken();
                firstMapName[i] = tok.nextToken();
                playerInitPos[i] = new int[] {Integer.parseInt(tok.nextToken()),Integer.parseInt(tok.nextToken())};
                i++;
            }
        }
    }
    
    public static int getCurrentLevel() { return currentLevelNum; }
    public static void incrementCurrentLevel() { setCurrentLevel(currentLevelNum+1); }
    /**
     * Sets the current level.  Must be called before the game begins running.
     * It will initialize MapManager and MapLoader which create timelines, load
     * the default map file, and construct the player/other entities.
     * @param i 
     */
    public static void setCurrentLevel(int i) {
        if (i > 0 && i <= numLevels) {
            currentLevelNum = i; 
            MapLoader.setLevelPath(levelDirs[currentLevelNum-1]);
            MapManager.createNewTimelines(firstMapName[currentLevelNum-1]);
        }
    }
    
    public static int[] playerStartPos() {
        return playerInitPos[currentLevelNum-1];
    }
}
