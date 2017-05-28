package map;

import entities.Entity;
import nullset.Vars;

import java.util.ArrayList;

/**
 * Container class for the map editor, a sort of API for modifying a map file.
 * @author Peter
 */
public class MapEditor {

    private MapData mapData;
    private ArrayList<Entity> entities;
    private boolean entitiesRefreshed;

    public MapEditor(MapData md) {
        mapData = md;
        entitiesRefreshed = false;
    }
    public void refreshEntities() {
        entities = MapDataUtility.constructEntities(mapData.entities);
        for (Entity e : entities)
            e.onRoomInit();
        entitiesRefreshed = true;
    }

    /**
     * Edits the floor texture coordinates of the tile.
     * @param x
     * @param y
     * @param chg
     */
    public void editTileInd1(int x, int y, int chg) {
        if (mapData.tileData[x][y][0]+chg >= 0) {
            mapData.tileData[x][y][0] += chg;
            mapData.tileData[x][y][0] %= Vars.TILESHEET_X* Vars.TILESHEET_Y;
            mapData.rebuildMesh();
        }
    }
    /**
     * Edits the wall texture coordinates of the tile.
     * @param x
     * @param y
     * @param chg
     */
    public void editTileInd2(int x, int y, int chg) {
        if (mapData.tileData[x][y][2]+chg >= 0) {
            mapData.tileData[x][y][2] += chg;
            mapData.tileData[x][y][0] %= Vars.TILESHEET_X* Vars.TILESHEET_Y;
            mapData.rebuildMesh();
        }
    }

    public void setTileShapeBit(int x, int y, int bit, boolean value) {
        if (value)
            mapData.tileData[x][y][1] |= 1 << bit;
        else
            mapData.tileData[x][y][1] &= ~(1 << bit);
        mapData.rebuildMesh();
    }
    public void toggleTileShapeBit(int x, int y, int bit) {
        int val = mapData.tileData[x][y][1] & (1 << bit);
        setTileShapeBit(x,y,bit, val == 0);
    }

    public void editHeight(int x, int y, int chg) {
        mapData.heightData[x][y] += chg;
        mapData.rebuildMesh();
        for (Entity e : entities)
            e.onRoomInit();
    }

    /**
     * Adds the specified entity to the map.
     * The entity descriptor string will be added to the map, but if it is not
     * recognized at load time it will not be constructed.
     * @param vars
     */
    public void addEntity(String... vars) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s : vars) {
            sb.append(s);
            i++;
            if (i < vars.length)
                sb.append(",");
        }
        System.out.println("Adding entity to map: "+sb.toString());
        mapData.addEntity(sb.toString());

        entitiesRefreshed = false;
    }
    public ArrayList<Entity> getEntities() {
        if (!entitiesRefreshed)
            refreshEntities();
        return entities;
    }
    public MapData getMapData() { return mapData; }
}