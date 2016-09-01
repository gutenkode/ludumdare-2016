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

/**
 * Utility class for constructing Entities and Meshes for MapData objects.
 * These methods are separated for readability.
 * @author Peter
 */
public class MapDataUtility {
    public static ArrayList<Entity> constructEntities(String[] entityData) {
        ArrayList<Entity> list = new ArrayList<>();
        int x,y,h,width,height;
        for (String s : entityData) {
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
                    Item item = null;
                    for (Item i2 : Item.values()) {
                        if (i2.toString().equals(itemName))
                            item = i2;
                    }
                    if (item == null)
                        throw new IllegalArgumentException("Unable to find item '"+itemName+"' when constructing ItemPickup entity.");
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
                    list.add(new Elevator(x,y,height));
                    break;
                case "Water":
                    x = Integer.valueOf(tok.nextToken());
                    y = Integer.valueOf(tok.nextToken());
                    list.add(new Water(x,y));
                    break;
                default:
                    System.err.println("Unrecognized entity name while constructing entities: "+s);
                    break;
            }
        }
        return list;
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
    private static void addVertex(Attribute vertAttrib, Attribute texAttrib, Attribute[] normAttrib, Attribute shadeAttrib, int i, int j) {
        int h = mapData.heightData[i][j];
        
        // ground tile is only drawn for first 2 types
        if (mapData.tileData[i][j][1] < 2) {
            // ground tile
            vertAttrib.add(i,j,h,
                             i,j+1,h,
                             i+1,j,h,
                             
                             i+1,j,h,
                             i,j+1,h,
                             i+1,j+1,h);
            addTex(texAttrib, i,j, 1,0);
            addNormal(normAttrib, 0,0,1);
            
            addShade(shadeAttrib, i,j, true);
        }
        
        // shape 0 and 2 only have the ground tile
        if (mapData.tileData[i][j][1] == 0 || mapData.tileData[i][j][1] == 2)
            return;
        
        // back wall
        if (j > 0) {
            int h2 = mapData.heightData[i][j-1];
            if (h2 > h) { // if the back tile is higher than this one
                int diff = h2-h;
                vertAttrib.add(i,j,h2,
                                 i,j,h2-diff,
                                 i+1,j,h2,
                                 i+1,j,h2,
                                 i,j,h2-diff,
                                 i+1,j,h2-diff);
                // shapes 1 and 4 use the current tile's texture
                if (mapData.tileData[i][j][1] == 1 || mapData.tileData[i][j][1] == 4)
                    addTex(texAttrib, i,j, diff,2);
                else
                    addTex(texAttrib, i,j-1, diff,2);
                addNormal(normAttrib, 0,1,0);
                //addNormal(builder, 0,-1,1);
                addShade(shadeAttrib, i,j, false);
            }
        }
        
        // right wall
        if (i+1 < mapData.heightData.length) {
            int h2 = mapData.heightData[i+1][j];
            if (h2 > h) { // if the right tile is higher than this one
                int diff = h2-h;
                vertAttrib.add(i+1,j,h2,
                                 i+1,j,h2-diff,
                                 i+1,j+1,h2,
                                 i+1,j+1,h2,
                                 i+1,j,h2-diff,
                                 i+1,j+1,h2-diff);
                // shapes 1 and 4 use the current tile's texture
                if (mapData.tileData[i][j][1] == 1 || mapData.tileData[i][j][1] == 4)
                    addTex(texAttrib, i,j, diff,2);
                else
                    addTex(texAttrib, i+1,j, diff,2);
                addNormal(normAttrib, -1,0,0);
                addShade(shadeAttrib, i,j, false);
            }
        }
        
        // left wall
        if (i > 0) {
            int h2 = mapData.heightData[i-1][j];
            if (h2 > h) { // if the left tile is higher than this one
                int diff = h2-h;
                vertAttrib.add(i,j+1,h2,
                                 i,j+1,h2-diff,
                                 i,j,h2,
                                 i,j,h2,
                                 i,j+1,h2-diff,
                                 i,j,h2-diff);
                // shapes 1 and 4 use the current tile's texture
                if (mapData.tileData[i][j][1] == 1 || mapData.tileData[i][j][1] == 4)
                    addTex(texAttrib, i,j, diff,2);
                else
                    addTex(texAttrib, i-1,j, diff,2);
                addNormal(normAttrib, 1,0,0);
                addShade(shadeAttrib, i,j, false);
            }
        }
    }
    /**
     * Adds texture coordinate data for a tile.
     * @param attrib The Attribute to add to.
     * @param i X index.
     * @param j Y index.
     * @param scale Vertical tile scale, e.g. how many tiles to include vertically in this quad.
     * @param texType Which "set" of texture coordinates to use, 0 for default, 2 for special.
     */
    private static void addTex(Attribute attrib, int i, int j, float scale, int texType) {
        float indX = (int)(mapData.tileData[i][j][texType]%Const.TILESHEET_X )*Const.TILE_SIZE_X;
        float indY = (int)(mapData.tileData[i][j][texType]/Const.TILESHEET_X )*Const.TILE_SIZE_Y;

        attrib.add(indX, indY,
                   indX, indY+Const.TILE_SIZE_Y*scale,
                   indX+Const.TILE_SIZE_X, indY,
                   indX+Const.TILE_SIZE_X, indY,
                   indX, indY+Const.TILE_SIZE_Y*scale,
                   indX+Const.TILE_SIZE_X, indY+Const.TILE_SIZE_Y*scale);
    }
    /**
     * Similar to addTex, but only a tile index is required.
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

        attrib.add(indX, indY,
                   indX, indY+Const.TILE_SIZE_Y,
                   indX+Const.TILE_SIZE_X, indY,
                   indX+Const.TILE_SIZE_X, indY,
                   indX, indY+Const.TILE_SIZE_Y,
                   indX+Const.TILE_SIZE_X, indY+Const.TILE_SIZE_Y);
    }
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
}