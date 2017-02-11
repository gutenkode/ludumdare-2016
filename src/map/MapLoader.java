package map;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import mote4.scenegraph.Window;
import mote4.util.FileIO;

/**
 * Utility class for loading map files into the game.
 * @author Peter
 */
public class MapLoader {
    
    public static final String FILE_EXTENSION = ".rf2";
    private static String levelPath = ""; // the folder (or folders) the set of levels is in
    private static HashMap<String, MapData> loadedMaps = new HashMap<>();
    
    // intermediate variables used during map loading
    private static int mapWidth, mapHeight;
    private static String fileName;
    
    /**
     * Load the specified map file into the game.
     * @param fileName Name of the map file.
     */
    public static void loadMapFile(String fileName) {
        if (loadedMaps.containsKey(fileName)) {
            System.err.println("Attempted to load already loaded map file '"+fileName+"'.");
            return; // don't load the same room more than once
        }
        System.out.println("Loading map '"+fileName+"'...");
        
        MapLoader.fileName = fileName;
        mapWidth = mapHeight = -1;
        
        BufferedReader bf = FileIO.getBufferedReader("/res/maps/"+levelPath+"/"+fileName+FILE_EXTENSION);
        ArrayList<String> file = new ArrayList<>();
        try {
            String inString;
            while ((inString = bf.readLine()) != null)
                file.add(inString);
            bf.close();
        } catch (IOException e) {
            System.err.println("Error loading map '"+fileName+"':");
            e.printStackTrace();
            Window.destroy();
        }
        
        // load metadata
        int start = file.indexOf("<meta>");
        int end = file.indexOf("</meta>");
        loadMeta(start, end, file);

        // load texture data
        start = file.indexOf("<tiledata>");
        end = file.indexOf("</tiledata>");
        int[][][] tileData = loadTile(start, end, file);

        // load height data
        start = file.indexOf("<walkdata>");
        end = file.indexOf("</walkdata>");
        int[][] heightData = loadHeight(start, end, file);
        
        // load entity data
        start = file.indexOf("<entitydata>");
        end = file.indexOf("</entitydata>");
        ArrayList<String> entities = loadEntities(start, end, file);
        
        loadedMaps.put(fileName, new MapData(fileName, tileData, heightData, entities));
    }
    
    /**
     * Changes the filepath for maps to load.  Should be updated when the level number changes.
     * IMPORTANT: calling this method with a different path will clear all currently loaded map files.
     * @param s 
     */
    public static void setLevelPath(String s) { 
        if (!levelPath.equals(s)) {
            levelPath = s;
            for (MapData md : loadedMaps.values())
                md.destroy();
            loadedMaps.clear();
        }
    }
    
    /**
     * Return a previously loaded MapData object.
     * If the requested map is not loaded, an attempt will 
     * be made to load it and then return the result.
     * @param name The name of the map to return.
     * @return 
     */
    public static MapData getMap(String name) {
        MapData md = loadedMaps.get(name);
        if (md == null) {
            //System.err.println("The map '"+name+"' is not loaded. Attempting to load...");
            loadMapFile(name);
            return loadedMaps.get(name);
        }
        return md;
    }
    
    /**
     * Reads values such as height and width of the map being loaded.
     * @param s Start index.
     * @param e End index.
     * @param str File to read. 
     */
    private static void loadMeta(int s, int e, ArrayList<String> str) {
        for (int i = s+1; i < e; i++) {
            StringTokenizer tok = new StringTokenizer(str.get(i),"=");
            switch (tok.nextToken()) {
                case "width":
                    mapWidth = Integer.parseInt(tok.nextToken());
                    break;
                case "height":
                    mapHeight = Integer.parseInt(tok.nextToken());
                    break;
                default:
                    System.err.println("Unrecognized metadata tag when parsing map file '"+fileName+"'.");
                    break;
            }
        }
    }
    /**
     * Reads tile shape and texture info.
     * The returned array contains the following:
     *      texture coordinates,
     *      tile "shape",
     *      secondary texture coordinates.
     * @param s Start index.
     * @param e End index.
     * @param str File to read.
     * @return 
     */
    private static int[][][] loadTile(int s, int e, ArrayList<String> str) {
        int[][][] data = new int[mapWidth][mapHeight][3];
        for (int i = s+1; i < e; i++) {
            StringTokenizer tok = new StringTokenizer(str.get(i),", ");
            for (int j = 0; j < mapWidth; j++) {
                data[j][i-s-1][0] = Integer.parseInt(tok.nextToken());
                data[j][i-s-1][1] = Integer.parseInt(tok.nextToken());
                data[j][i-s-1][2] = Integer.parseInt(tok.nextToken());
            }
        }
        return data;
    }
    /**
     * Reads height data for tiles.
     * @param s Start index.
     * @param e End index.
     * @param str File to read.
     * @return 
     */
    private static int[][] loadHeight(int s, int e, ArrayList<String> str) {
        int[][] data = new int[mapWidth][mapHeight];
        for (int i = s+1; i < e; i++) {
            StringTokenizer tok = new StringTokenizer(str.get(i)," ");
            for (int j = 0; j < mapWidth; j++) {
                data[j][i-s-1] = Integer.parseInt(tok.nextToken());
            }
        }
        return data;
    }
    /**
     * Creates a list of Entity "blueprints" for each MapData object.
     * This is simply the entity constructor represented by a String.
     * Actual Entity construction is handled in MapData, as multiple sets
     * of entities may need to be created, not just once during map loading.
     * @param s Start index.
     * @param e End index.
     * @param str File to read.
     * @return 
     */
    private static ArrayList<String> loadEntities(int s, int e, ArrayList<String> str) {
        // pretty simple
        ArrayList<String> list = new ArrayList<>();//new String[e-s-1];
        int ind = 0;
        for (int i = s+1; i < e; i++) {
            list.add(str.get(i).trim());
            //list[ind] = str.get(i).trim();
            //ind++;
        }
        return list;
    }

    /**
     * Write a map out to a file, used with the editor.
     * @param md
     */
    public static boolean saveMapFile(MapData md) {
        try {
            File file = new File("./src/res/maps/"+levelPath+"/"+md.mapName+"_NEW"+FILE_EXTENSION);
            if (!file.exists())
                file.createNewFile();
            System.out.println(file.getPath());
            FileWriter fWriter = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fWriter);

            String outString = "";

            // write meta
            outString += "<meta>\nwidth="+md.width+"\nheight="+md.height+"\n</meta>";
            out.write(outString);
            out.newLine();

            // write tile data
            out.write("<tiledata>");
            out.newLine();
            for (int i = 0; i < md.height; i++) {
                outString = "";
                for (int j = 0; j < md.width; j++) {
                    outString += md.tileData[j][i][0] + "," + md.tileData[j][i][1] + "," + md.tileData[j][i][2] + " ";
                }
                out.write(outString);
                out.newLine();
            }
            out.write("</tiledata>");
            out.newLine();

            // write tile height
            out.write("<walkdata>");
            out.newLine();
            for (int i = 0; i < md.height; i++) {
                outString = "";
                for (int j = 0; j < md.width; j++) {
                    outString += md.heightData[j][i]+" ";
                }
                out.write(outString);
                out.newLine();
            }
            out.write("</walkdata>");
            out.newLine();

            out.write("<entitydata>");
            out.newLine();
            for (String s : md.entities) {
                out.write(s);
                out.newLine();
            }
            out.write("</entitydata>");
            out.newLine();

            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void makeEmptyMap(String fileName, int x, int y) {
        int[][][] tileData = new int[x][y][3]; //3,1,0
        for (int i = 0; i < tileData.length; i++)
            for (int j = 0; j < tileData[0].length; j++) {
                tileData[i][j][0] = 1;
                tileData[i][j][1] = 1;
                tileData[i][j][2] = 0;
            }
        int[][] heightData = new int[x][y];
        ArrayList<String> entities = new ArrayList<>();
        loadedMaps.put(fileName, new MapData(fileName, tileData, heightData, entities));
    }

    public static boolean deleteMapFile(String mapName) {
        File file = new File("./src/res/maps/"+levelPath+"/"+mapName+FILE_EXTENSION);
        if (file.exists())
            return file.delete();
        return false;
    }
}
