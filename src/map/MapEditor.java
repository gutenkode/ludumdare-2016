package map;

import entities.Entity;
import nullset.Const;

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
            mapData.tileData[x][y][0] %= Const.TILESHEET_X*Const.TILESHEET_Y;
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
            mapData.tileData[x][y][0] %= Const.TILESHEET_X*Const.TILESHEET_Y;
            mapData.rebuildMesh();
        }
    }
    /**
     * Edits the tile shape value to toggle drawing of ground tiles.
     * @param x
     * @param y
     */
    public void toggleGroundTile(int x, int y) {
        int val = mapData.tileData[x][y][1]+2;
        mapData.tileData[x][y][1] = val % 4;
        mapData.rebuildMesh();

    }
    /**
     * Edits the tile shape value to toggle drawing of wall tiles.
     * @param x
     * @param y
     */
    public void toggleWallTile(int x, int y) {
        switch (mapData.tileData[x][y][1]) {
            case 0:
                mapData.tileData[x][y][1] = 1;
                break;
            case 1:
                mapData.tileData[x][y][1] = 0;
                break;
            case 2:
                mapData.tileData[x][y][1] = 3;
                break;
            case 3:
                mapData.tileData[x][y][1] = 2;
                break;
        }
        mapData.rebuildMesh();
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