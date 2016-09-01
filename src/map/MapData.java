package map;

import mote4.util.vertex.mesh.Mesh;

/**
 * Container class for an overworld map.
 * Constructed by MapLoader.
 * This class is not meant to be interacted with outside of MapManager.
 * @author Peter
 */
public class MapData {
    
    private boolean meshBuilt = false;
    private Mesh mesh;
    
    protected final int width, height;
    protected final String mapName;
    protected final int[][][] tileData;
    protected final int[][] heightData;
    protected final LinkData[] linkData;
    protected final String[] entities;
    //protected final StaticObject[] staticObjects;
    
    /**
     * Loads all the data required to define a room in the game.
     * @param mapName The String identifying this room.
     * @param tileData Ignore this for now.
     * @param heightData 2D array defining the height of each tile, default is 0.
     * @param linkData Array of LinkData objects for tiles that link to other rooms.
     * @param entities String array of all entities to be constructed.
     */
    protected MapData(String mapName, int[][][] tileData, int[][] heightData, LinkData[] linkData, String[] entities) {
        width = tileData.length;
        height = tileData[0].length;
        this.mapName = mapName;
        this.tileData = tileData;
        this.heightData = heightData;
        this.linkData = linkData;
        this.entities = entities;
    }
    
    /**
     * Find and return the LinkData in this room that links to a specified room.
     * Returns null if there is no matching LinkData.
     * @param linkedRoomName
     * @return 
     */
    public LinkData getLinkPair(String linkedRoomName) {
        for (LinkData ld : linkData)
            if (ld.mapName.equals(linkedRoomName))
                return ld;
        return null;
        
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
    
    public void destroy() {
        mesh.destroy();
    }
}
