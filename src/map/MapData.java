package map;

import mote4.util.vertex.mesh.Mesh;

import java.util.ArrayList;

/**
 * Container class for an overworld map.
 * Constructed by MapLoader.
 * This class is not meant to be interacted with outside of MapManager.
 * @author Peter
 */
public class MapData {
    
    private boolean meshBuilt = false;
    private Mesh mesh;
    
    public final int width, height;
    public final String mapName;
    public final int[][][] tileData;
    public final int[][] heightData;
    public final ArrayList<String> entities;
    
    /**
     * Loads all the data required to define a room in the game.
     * @param mapName The String identifying this room.
     * @param tileData Stores data for texture coordinates and other tile data, in the following layers:
     *                  texture coordinates,
     *                  tile "shape",
     *                  secondary texture coordinates.
     *                 Tile shape is a value (0,1,2,3): 0,1=draw ground, 2,3=no ground, 0,2=no wall, 1,3=draw wall
     * @param heightData 2D array defining the height of each tile, default is 0.
     * @param entities String array of all entities to be constructed.
     */
    protected MapData(String mapName, int[][][] tileData, int[][] heightData, ArrayList<String> entities) {
        width = tileData.length;
        height = tileData[0].length;
        this.mapName = mapName;
        this.tileData = tileData;
        this.heightData = heightData;
        this.entities = entities;
    }
    
    public void render() {
        if (!meshBuilt) {
            meshBuilt = true;
            if (mesh != null) // this should never be triggered, but good to be safe
                mesh.destroy();
            mesh = MapDataUtility.buildMesh(this);
        }
        
        mesh.render();
    }

    /**
     * Signals that the current mesh should be rebuilt.
     * The mesh will not be rebuilt until render3d() is called again.
     */
    public void rebuildMesh() { meshBuilt = false; }

    /**
     * Used by MapEditor.
     * @param s
     */
    public void addEntity(String s) { entities.add(s); }
    
    public void destroy() {
        if (mesh != null)
            mesh.destroy();
    }
}
