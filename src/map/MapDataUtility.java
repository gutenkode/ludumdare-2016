package map;

import entities.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import mote4.util.vertex.builder.Attribute;
import mote4.util.vertex.builder.ModernMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import nullset.Const;
import org.lwjgl.opengl.GL11;
import rpgbattle.EnemyData;
import rpgsystem.Item;
import rpgsystem.Pickupable;
import rpgsystem.Skill;
import rpgsystem.SkillModifier;

/**
 * Utility class for constructing Entities and Meshes for MapData objects.
 * These methods are separated for readability.
 * @author Peter
 */
public class MapDataUtility {

    public static ArrayList<Entity> constructEntities(ArrayList<String> entityData) {
        ArrayList<Entity> list = new ArrayList<>();
        int x,y,h,width,height;
        for (String s : entityData) {
            if (s.isEmpty())
                continue;
            StringTokenizer tok = new StringTokenizer(s,",");
            switch (tok.nextToken()) {
                case "StaticObject":
                    float fx = Float.valueOf(tok.nextToken());
                    float fy = Float.valueOf(tok.nextToken());
                    String str = tok.nextToken();
                    if (tok.hasMoreTokens())
                        list.add(new StaticObject(fx,fy,str,Float.valueOf(tok.nextToken())));
                    else
                        list.add(new StaticObject(fx,fy,str,0));
                    break;
                case "Terminal":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    list.add(new Terminal(x,y));
                    break;
                case "ItemPickup":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    String itemName = tok.nextToken().trim();
                    Pickupable item = findPickupable(itemName);
                    list.add(new ItemPickup(x,y,item));
                    break;
                case "ScriptTrigger":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    width = Integer.valueOf(tok.nextToken());
                    height = Integer.valueOf(tok.nextToken());
                    String scriptName = tok.nextToken().trim();
                    list.add(new ScriptTrigger(x,y,width,height,scriptName));
                    break;
                case "LaserGrid":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    width = Integer.valueOf(tok.nextToken());
                    list.add(new LaserGrid(x,y,width));
                    break;
                case "KeyDoor":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    int doorLevel = Integer.valueOf(tok.nextToken());
                    int dir = Integer.valueOf(tok.nextToken());
                    list.add(new KeyDoor(x,y,doorLevel,dir));
                    break;
                case "Enemy":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    String name = tok.nextToken().trim();
                    list.add(new Enemy(x,y,name));
                    break;
                case "SolidPlatform":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    h = Integer.valueOf(tok.nextToken());
                    list.add(new SolidPlatform(x,y,h));
                    break;
                case "TogglePlatform":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    h = Integer.valueOf(tok.nextToken());
                    int i = Integer.valueOf(tok.nextToken());
                    int r = Integer.valueOf(tok.nextToken());
                    boolean inv = Boolean.valueOf(tok.nextToken());
                    list.add(new TogglePlatform(x,y,h,i,r,inv));
                    break;
                case "ToggleButton":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    int i2 = Integer.valueOf(tok.nextToken());
                    list.add(new ToggleButton(x,y,i2));
                    break;
                case "Elevator":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    height = Integer.valueOf(tok.nextToken());
                    boolean up = Boolean.parseBoolean(tok.nextToken());
                    list.add(new Elevator(x,y,height,up));
                    break;
                case "Water":
                    h = Integer.valueOf(tok.nextToken());
                    list.add(new Water(h));
                    break;
                case "RoomLink":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    width = Integer.valueOf(tok.nextToken());
                    height = Integer.valueOf(tok.nextToken());
                    int rot = Integer.valueOf(tok.nextToken());
                    String rn = tok.nextToken();
                    list.add(new RoomLink(x,y,width,height,rot,rn));
                    break;
                default:
                    System.err.println("Unrecognized entity name while constructing entities: "+s);
                    break;
            }
        }
        return list;
    }
    private static Pickupable findPickupable(String itemName) {
        Pickupable item = null;
        // try and match an Item
        for (Item i : Item.values()) {
            if (i.toString().equals(itemName))
                item = i;
        }
        // try and match a Skill
        if (item == null) {
            for (Skill i : Skill.values()) {
                if (i.toString().equals(itemName))
                    item = i;
            }
        }
        // try and match a SkillModifier
        if (item == null) {
            for (SkillModifier i : SkillModifier.values()) {
                if (i.toString().equals(itemName))
                    item = i;
            }
        }
        if (item == null)
            throw new IllegalArgumentException("Unable to find item '"+itemName+"' when constructing ItemPickup entity.");
        return item;

    }

    private static MapData mapData; // stored when buildMesh is called
    public static Mesh buildMesh(MapData md) {
        mapData = md;
        ModernMeshBuilder builder = new ModernMeshBuilder();
        
        Attribute vertAttrib = new Attribute(0, 3);
        builder.addAttrib(vertAttrib); // vertices
        
        Attribute texAttrib = new Attribute(1, 2);
        builder.addAttrib(texAttrib); // tex coords
        
        Attribute[] normAttrib = new Attribute[] 
        {
            new Attribute(2, 3),
            new Attribute(3, 3),
            new Attribute(4, 3)
        };
        builder.addAttrib(normAttrib[0]); // tangent space matrix (normals)
        builder.addAttrib(normAttrib[1]);
        builder.addAttrib(normAttrib[2]);
        
        Attribute shadeAttrib = new Attribute(5,2);
        builder.addAttrib(shadeAttrib); // tile shade
        
        for (int i = 0; i < md.width; i++)
            for (int j = 0; j < md.height; j++) {
                addVertex(vertAttrib, texAttrib, normAttrib, shadeAttrib, i,j);
            }
        
        return builder.constructVAO(GL11.GL_TRIANGLES);
    }

    /**
     * Adds all mesh data for a tile.
     * @param vertAttrib
     * @param texAttrib
     * @param normAttrib
     * @param shadeAttrib
     * @param i Tile X value.
     * @param j Tile Y value.
     */
    private static void addVertex(Attribute vertAttrib, Attribute texAttrib, Attribute[] normAttrib, Attribute shadeAttrib, int i, int j) {
        int h = mapData.heightData[i][j];
        
        // ground tile is only drawn for shapes 0,1
        if (testShapeVal(i,j,1)) {
        //if (mapData.tileData[i][j][1] < 2) {
            // ground tile
            vertAttrib.add(
                i,j,h,
                i,j+1,h,
                i+1,j,h,

                i+1,j,h,
                i,j+1,h,
                i+1,j+1,h
            );
            addTex(texAttrib, i,j, 0,0);
            addNormal(normAttrib, 0,0,1);
            addShade(shadeAttrib, i,j, true);
        }
        
        // wall tile only drawn for shapes 1,3
        if (testShapeVal(i,j,0))
        //if (mapData.tileData[i][j][1] == 0 || mapData.tileData[i][j][1] == 2)
            return;
        
        // back wall
        if (j > 0) { // don't try to draw back walls for back row
            int h2 = mapData.heightData[i][j-1];
            if (h2 > h) { // if the back tile is higher than this one
                int diff = h2-h;
                for (int k = 0; k < diff; k++) {
                    vertAttrib.add(
                            i,j,h2-k,
                            i,j,h2-(1+k),
                            i+1,j,h2-k,

                            i+1,j,h2-k,
                            i,j,h2-(1+k),
                            i+1,j,h2-(1+k)
                    );
                    if ((k & 1) == 0)
                        addTex(texAttrib, i,j, 0,2);
                    else
                        addTex(texAttrib, i,j, 1,2);
                    addNormal(normAttrib, 0,1,0);
                    addShade(shadeAttrib, i,j, false);
                }
            }
        }
        
        // right wall
        if (i+1 < mapData.heightData.length) { // don't draw right walls for far right column
            int h2 = mapData.heightData[i+1][j];
            if (h2 > h) { // if the right tile is higher than this one
                int diff = h2-h;
                for (int k = 0; k < diff; k++) {
                    vertAttrib.add(
                            i+1,j,h2-k,
                            i+1,j,h2-(1+k),
                            i+1,j+1,h2-k,

                            i+1,j+1,h2-k,
                            i+1,j,h2-(1+k),
                            i+1,j+1,h2-(1+k)
                    );
                    if ((k & 1) == 0)
                        addTex(texAttrib, i,j, 0,2);
                    else
                        addTex(texAttrib, i,j, 1,2);
                    addNormal(normAttrib, -1,0,0);
                    addShade(shadeAttrib, i,j, false);
                }
            }
        }
        
        // left wall
        if (i > 0) { // don't draw left walls for far left column
            int h2 = mapData.heightData[i-1][j];
            if (h2 > h) { // if the left tile is higher than this one
                int diff = h2-h;
                for (int k = 0; k < diff; k++) {
                    vertAttrib.add(
                            i,j+1,h2-k,
                            i,j+1,h2-(1+k),
                            i,j,h2-k,
                            i,j,h2-k,
                            i,j+1,h2-(1+k),
                            i,j,h2-(1+k)
                    );
                    if ((k & 1) == 0)
                        addTex(texAttrib, i,j, 0,2);
                    else
                        addTex(texAttrib, i,j, 1,2);
                    addNormal(normAttrib, 1,0,0);
                    addShade(shadeAttrib, i,j, false);
                }
            }
        }
    }

    /**
     * Adds texture coordinate data for a tile.
     * @param attrib The Attribute to add to.
     * @param i X index.
     * @param j Y index.
     * @param offset Vertical tiles to offset by, useful for rendering multi-tile textures.
     * @param texType Which "set" of texture coordinates to use, 0 for default, 2 for special.
     *                0 is currently used for floor textures and 2 for wall textures.
     */
    private static void addTex(Attribute attrib, int i, int j, int offset, int texType) {
        float indX = (int)(mapData.tileData[i][j][texType]%Const.TILESHEET_X)*Const.TILE_SIZE_X;
        float indY = (int)(mapData.tileData[i][j][texType]/Const.TILESHEET_X)*Const.TILE_SIZE_Y;
        indY += offset*Const.TILE_SIZE_Y;

        attrib.add(
            indX, indY,
            indX, indY+Const.TILE_SIZE_Y,
            indX+Const.TILE_SIZE_X, indY,

            indX+Const.TILE_SIZE_X, indY,
            indX, indY+Const.TILE_SIZE_Y,
            indX+Const.TILE_SIZE_X, indY+Const.TILE_SIZE_Y
        );
    }

    /**
     * Similar to addTex, but only a tile index is required.
     * Adds the "shade" texture, making the edges of platforms a darker color.
     * Any edge near a ledge or the edge of the map will have shade added to it.
     * @param attrib
     */
    private static void addShade(Attribute attrib, int x, int y, boolean floor) {
        int ind = 0;
        
        if (floor)
        {
            int h = mapData.heightData[x][y];

            boolean l = false;
            if (x > 0)
                l = mapData.heightData[x-1][y] == h;
            boolean r = false;
            if (x < mapData.heightData.length-1)
                r = mapData.heightData[x+1][y] == h;
            boolean u = false;
            if (y > 0)
                u = mapData.heightData[x][y-1] == h;
            boolean d = false;
            if (y < mapData.heightData[0].length-1)
                d = mapData.heightData[x][y+1] == h;
        
            // all sides
            if (!l && !r && !u && !d)
                ind = 11;
            // single side
            else if (!l && r && u && d)
                ind = 3;
            else if (l && !r && u && d)
                ind = 2;
            else if (l && r && !u && d)
                ind = 1;
            else if (l && r && u && !d)
                ind = 4;
            // L sides
            else if (!l && r && !u && d)
                ind = 7;
            else if (!l && r && u && !d)
                ind = 6;
            else if (l && !r && !u && d)
                ind = 8;
            else if (l && !r && u && !d)
                ind = 5;
            // parallel sides
            else if (!l && !r && u && d)
                ind = 9;
            else if (l && r && !u && !d)
                ind = 10;
            // three sides
            else if (!l && !r && !u && d)
                ind = 14;
            else if (!l && !r && u && !d)
                ind = 15;
            else if (!l && r && !u && !d)
                ind = 12;
            else if (l && !r && !u && !d)
                ind = 13;
            else
            {
                ind = 0;
                /*
                boolean ul = mapData.heightData[x-1][y-1] == h;
                boolean ur = mapData.heightData[x+1][y-1] == h;
                boolean dl = mapData.heightData[x-1][y+1] == h;
                boolean dr = mapData.heightData[x+1][y+1] == h;
                
                // single corners
                if (!ul && ur && dl && dr)
                    ind = 11;
                */
            }
        }
        
        float indX = (int)(ind%Const.TILESHEET_X )*Const.TILE_SIZE_X;
        float indY = (int)(ind/Const.TILESHEET_X )*Const.TILE_SIZE_Y;

        attrib.add(
            indX, indY,
            indX, indY+Const.TILE_SIZE_Y,
            indX+Const.TILE_SIZE_X, indY,

            indX+Const.TILE_SIZE_X, indY,
            indX, indY+Const.TILE_SIZE_Y,
            indX+Const.TILE_SIZE_X, indY+Const.TILE_SIZE_Y
        );
    }

    /**
     * Adds the normal matrix used for lighting and bumpmapping.
     * @param attrib The array of Attributes for normals.
     * @param x Normal X value.
     * @param y Normal Y value.
     * @param z Normal Z value.
     */
    private static void addNormal(Attribute[] attrib, float x, float y, float z) {
        for (int i = 0; i < 6; i++) {
            if (x == 0 && y == 1 && z == 0) {
                attrib[0].add(1,0,0);
                attrib[1].add(0,0,1);
                attrib[2].add(0,1,0);
            } else if (x == 0 && y == 0 && z == 1) {
                attrib[0].add(1,0,0);
                attrib[1].add(0,-1,0);
                attrib[2].add(0,0,1);
            } else if (x == 1 && y == 0 && z == 0) {
                attrib[0].add(0,-1,0);
                attrib[1].add(0,0,1);
                attrib[2].add(1,0,0);
            } else if (x == -1 && y == 0 && z == 0) {
                attrib[0].add(0,1,0);
                attrib[1].add(0,0,1);
                attrib[2].add(-1,0,0);
            } else {
                attrib[0].add(y,z,-x);
                attrib[1].add(x,z,-y);
                attrib[2].add(x,y,z);
            }
        }
    }

    /**
     * Test the shape value for a tile.
     * Tile values are stored in a single int as separate bits; index is the offset of the bit to test.
     * @param x Tile X.
     * @param y Tile Y.
     * @param index Index of tile value to test.
     * @return
     */
    private static boolean testShapeVal(int x, int y, int index) {
        return 0 == (mapData.tileData[x][y][1] & (1 << index));
    }
}