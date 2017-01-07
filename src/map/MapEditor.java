package map;

import java.util.ArrayList;

/**
 * Container class for the map editor, a sort of API for modifying a map file.
 * @author Peter
 */
public class MapEditor {

    private MapData mapData;

    public MapEditor(MapData md) {
        mapData = md;
    }

    public void editTileInd1(int x, int y, int chg) {
        if (mapData.tileData[x][y][0]+chg >= 0) {
            mapData.tileData[x][y][0] += chg;
            mapData.rebuildMesh();
        }
    }
    public void editTileInd2(int x, int y, int chg) {
        if (mapData.tileData[x][y][2]+chg >= 0) {
            mapData.tileData[x][y][2] += chg;
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
    }

    public void addLink(int x, int y, String roomname) {
        ArrayList<Character> ids = new ArrayList<>();
        for (LinkData ld : mapData.linkData) {
            if ((ld.x == x && ld.y == y) || roomname.equals(ld.mapName))
                return; // there is already a LinkData here or it links to the same room
            ids.add(ld.id);
        }
        // no LinkData was found
        for (int i = 'a'; i <= 'z'; i++) {
            if (!ids.contains(i)) {
                mapData.linkData.add(new LinkData((char) i, roomname, x, y, 0));
                return;
            }
        }
        // this room links to too many other rooms, somehow...
    }

    /**
     * If there is a room link on this tile, edits its orientation.
     * @param x
     * @param y
     * @param chg
     */
    public void editLinkDirection(int x, int y, int chg) {
        for (LinkData ld : mapData.linkData) {
            if (ld.x == x && ld.y == y) {
                ld.direction += chg;
                ld.direction %= 4;
                return;
            }
        }
        // no LinkData was found
    }

    /**
     * Adds the specified entity to the map.
     * The entity descriptor string will be added to the map but if it is not
     * recognized it will not be constructed.
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
        //mapData.entities.add(sb.toString());
    }
}