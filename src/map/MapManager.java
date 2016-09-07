package map;

import entities.Entity;
import entities.Player;
import java.util.ArrayList;

import entities.Water;
import mote4.util.matrix.GenericMatrix;
import mote4.util.matrix.Transform;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import nullset.Input;
import static org.lwjgl.opengl.GL11.*;
import scenes.Postprocess;

/**
 * Manages rendering and updating maps and entities.
 * @author Peter
 */
public class MapManager {
    private static Player player;
    private static Timeline currentTimeline;
    private static ArrayList<Timeline> timelines;
    private static int currentTimelineInd;
    // temporary list of entities that should be deleted, to avoid ConcurrentModificationExceptions
    private static ArrayList<Entity> deleteList = new ArrayList<>(); 
    private static LinkData loadLinkData; // stored link data while a fadeout is performed
    
    public static Player getPlayer() { return player; }
    public static TimelineState getTimelineState() { return currentTimeline.getState(); }
    /**
     * Creates new timelines.
     * @param firstRoom
     */
    public static void createNewTimelines(String firstRoom) {
        if (timelines == null)
            timelines = new ArrayList<>();
        else
            timelines.clear();
        for (int i = 0; i < 3; i++) {
            Timeline t = new Timeline();
            currentTimeline = t; // entity creation references the current timeline
            // this will create all entities in the start room
            t.setMapData(MapLoader.getMap(firstRoom));
            timelines.add(t);
        }
        jumpToTimeline(0);
    }
    /**
     * Jumps to the specified timeline.
     * @param i 
     */
    public static void jumpToTimeline(int i) {
        currentTimelineInd = i;
        currentTimeline = timelines.get(currentTimelineInd);
        
        for (Entity e : currentTimeline.getEntities())
            e.onRoomInit();
        player = currentTimeline.getPlayer();
        player.onRoomInit();
    }
    
    /**
     * Attempts to remove the specified entity from the current room.
     * If the entity is not present in the current room,
     * no entity will be deleted.
     * @param e 
     */
    public static void removeEntity(Entity e) {
        for (Entity e1 : currentTimeline.getEntities())
            if (e1 == e) {
                deleteList.add(e1);
                return;
            }
    }
    
    public static void update() {
        if (Input.currentLock() == Input.Lock.FADE)
            return;
        if (Input.currentLock() == Input.Lock.PLAYER) {
            if (Input.isKeyNew(Input.Keys.TIMELINE_1))
                jumpToTimeline(0);
            else if (Input.isKeyNew(Input.Keys.TIMELINE_2))
                jumpToTimeline(1);
            else if (Input.isKeyNew(Input.Keys.TIMELINE_3))
                jumpToTimeline(2);
        }
        
        player.update();
        testRoomLinks();
        
        while (!deleteList.isEmpty()) {
            currentTimeline.getEntities().remove(deleteList.remove(0));
        }
        
        for (Entity e : currentTimeline.getEntities()) {
            if (e.isInside(player.posX(), player.posY())) {
                e.playerPointIn();
                if (Input.currentLock() == Input.Lock.PLAYER)
                    if (Input.isKeyNew(Input.Keys.YES))
                        e.playerUse();
            }
            if (player.isInside(e))
                e.playerBoxIn();
            e.update();
        }
    }
    /**
     * Test if the player is on a room link tile, and load the new room if needed.
     */
    private static void testRoomLinks() {
        for (LinkData ld : currentTimeline.getMapData().linkData)
            if ((int)player.posX() == ld.x &&
                (int)player.posY() == ld.y) {
                Input.pushLock(Input.Lock.FADE);
                loadLinkData = ld; // save the link data for after the room transition
                Postprocess.fadeOut(MapManager::fadeCallback);
                return;
            }
                
    }
    public static void fadeCallback() {
        loadRoom(loadLinkData);
        Input.popLock();
    }
    /**
     * Load the specified room.
     * @param roomLink 
     */
    private static void loadRoom(LinkData roomLink) {
        MapData newMapData = MapLoader.getMap(roomLink.mapName); // get the MapData for the new room
        // newMapData will not be null; MapLoader will throw an error if no map is found
        LinkData newLink = newMapData.getLinkPair(currentTimeline.getMapData().mapName); // get the LinkData the player has moved to
        if (newLink == null)
            throw new IllegalStateException("Linked room does not have reciprocating link. From: "+currentTimeline.getMapData().mapName +" To: "+roomLink.mapName);
        int[] loc = newLink.getFrontTile();
        player.moveTo(loc[0]+.5f, loc[1]+.5f); // move the player to the new location
        currentTimeline.setMapData(newMapData);
        // now called by setMapData()
        //currentTimeline.createEntities(md); // create new entities if necessary
        for (Entity e : currentTimeline.getEntities())
            e.onRoomInit();
        player.onRoomInit();
    }
    
    static double cycle;
    /**
     * Renders static map mesh, entity sprites, and static object meshes using
     * lighting shaders and textures.
     * @param trans
     * @param shadowProj
     */
    public static void render(Transform trans, GenericMatrix shadowProj) {
        float dir = player.facingDirection();
        dir /= 8;
        dir *= Math.PI*2;
        
    // render static map mesh
        ShaderMap.use("ingame_map");
        shadowProj.makeCurrent();
        Uniform.varFloat("ambient", 0,0,0);//Uniform.varFloat("ambient", .2f,.2f,.2f);
        Uniform.varFloat("flashlightAngle", -(float)Math.sin(dir),-(float)Math.cos(dir),0);
        Uniform.varFloat("lightPos", player.posX(), player.posY()+player.hitboxH(), player.elevatorHeight()+1f);
        Uniform.samplerAndTextureFiltered("shadowMap", 1, "fbo_depth");
        
        TextureMap.bindUnfiltered("tileset_1");
        Uniform.samplerAndTextureUnfiltered("tex_shade", 2, "tileset_shade");
        Uniform.samplerAndTextureUnfiltered("tex_bump", 3, "tileset_1_NRM");
        trans.model.setIdentity();
        trans.makeCurrent();
        currentTimeline.getMapData().render();
        
    // render entity tilesheets
        ShaderMap.use("spritesheet_light");
        shadowProj.makeCurrent();
        Uniform.varFloat("ambient", 0,0,0);//Uniform.varFloat("ambient", .2f,.2f,.2f);
        Uniform.varFloat("flashlightAngle", -(float)Math.sin(dir),-(float)Math.cos(dir),0);
        Uniform.varFloat("lightPos", player.posX(), player.posY()+player.hitboxH(), player.elevatorHeight()+1f);
        Uniform.samplerAndTextureFiltered("shadowMap", 1, "fbo_depth");
        
        trans.makeCurrent();
        player.render(trans.model);
        for (Entity e : currentTimeline.getEntities()) {
            trans.model.setIdentity();
            e.render(trans.model);
        }
        
    // render hitboxes
        /*
        ShaderMap.use("color");
        //Uniform.varFloat("colorMult", 1,0,0,1);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        trans.makeCurrent();
        trans.model.setIdentity();
        player.renderHitbox(trans.model);
        for (Entity e : currentTimeline.getEntities()) {
            trans.model.setIdentity();
            trans.model.makeCurrent();
            e.renderHitbox(trans.model);
        }
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        */
    }
    /**
     * Renders meshes with only data needed for constructing the depth texture
     * for shadow mapping.
     * @param shadowModel
     * @param shadowProj
     */
    public static void renderForShadow(TransformationMatrix shadowModel, GenericMatrix shadowProj) {
        ShaderMap.use("shadowMap");
        shadowProj.makeCurrent();
        
        // render static map mesh
        shadowModel.setIdentity();
        shadowModel.makeCurrent();
        currentTimeline.getMapData().render();
        
        ShaderMap.use("shadowMap_tex");
        shadowProj.makeCurrent();
        
        // render entity tilesheets
        for (Entity e : currentTimeline.getEntities()) {
            if (e instanceof Water)
                continue; // water does not cast shadows
            shadowModel.setIdentity();
            shadowModel.makeCurrent();
            e.render(shadowModel);
        }
        
    }
    
    /**
     * The size of the current map.
     * @return 
     */
    public static int[] currentMapSize() { return new int[] {currentTimeline.getMapData().width, currentTimeline.getMapData().height}; }
    /**
     * The height of a tile in the current map.
     * @param x
     * @param y
     * @return 
     */
    public static int getTileHeight(int x, int y) { return currentTimeline.getMapData().heightData[x][y]; }
    
    /**
     * Tests whether an entity is colliding with the heightmap of any loaded maps.
     * Out of bounds is invalid.
     * @param e
     * @param chgX
     * @param chgY
     * @return 
     */
    public static boolean entityCollidesWithMap(Entity e, float chgX, float chgY) {
        // out of bounds check
        /*
        if ((e.posX()-e.hitboxW()+chgX) < 0 ||
            (e.posX()+e.hitboxW()+chgX) >= md.width ||
            (e.posY()-e.hitboxH()+chgY) < 0 ||
            (e.posX()+e.hitboxH()+chgY) >= md.height)
            return true;
        */
        MapData md = currentTimeline.getMapData();
        if (md.heightData[(int)(e.posX()+e.hitboxW()+chgX)][(int)(e.posY()+e.hitboxH()+chgY)] != e.tileHeight() ||
            md.heightData[(int)(e.posX()-e.hitboxW()+chgX)][(int)(e.posY()+e.hitboxH()+chgY)] != e.tileHeight() ||
            md.heightData[(int)(e.posX()-e.hitboxW()+chgX)][(int)(e.posY()-e.hitboxH()+chgY)] != e.tileHeight() ||
            md.heightData[(int)(e.posX()+e.hitboxW()+chgX)][(int)(e.posY()-e.hitboxH()+chgY)] != e.tileHeight())
            return true;
        return false;
    }
    /**
     * Tests whether an entity is colliding with any solid entities on the map.
     * @param e
     * @param chgX
     * @param chgY
     * @return 
     */
    public static boolean entityCollidesWithSolidEntities(Entity e, float chgX, float chgY) {
        for (Entity e2 : currentTimeline.getEntities()) {
            if (e2.isSolid())
                if (e2 != e) // don't collide an entity with itself
                    if (e.collides(e2, chgX, chgY))
                        return true;
        }
        return false;
    }
    /**
     * Tests whether an entity is colliding with the heightmap of any loaded
     * maps, as well as walkable entities.  Walkable entities on the same height
     * will override map collision.
     * Out of bounds is invalid.
     * @param e
     * @param chgX
     * @param chgY
     * @return 
     */
    public static boolean entityCollidesWithMapAndWalkableEntities(Entity e, float chgX, float chgY) {
        // out of bounds check
        /*
        if ((e.posX()-e.hitboxW()+chgX) < 0 ||
            (e.posX()+e.hitboxW()+chgX) >= md.width ||
            (e.posY()-e.hitboxH()+chgY) < 0 ||
            (e.posX()+e.hitboxH()+chgY) >= md.height)
            return true;
        */
        if (testWalkableCollision(e, (int)(e.posX()+e.hitboxW()+chgX), (int)(e.posY()+e.hitboxH()+chgY) ) ||
            testWalkableCollision(e, (int)(e.posX()-e.hitboxW()+chgX), (int)(e.posY()+e.hitboxH()+chgY) ) ||
            testWalkableCollision(e, (int)(e.posX()-e.hitboxW()+chgX), (int)(e.posY()-e.hitboxH()+chgY) ) ||
            testWalkableCollision(e, (int)(e.posX()+e.hitboxW()+chgX), (int)(e.posY()-e.hitboxH()+chgY) ))
            return true;
        return false;
    }
    private static boolean testWalkableCollision(Entity e, int testX, int testY) {
        int[][] heightData = currentTimeline.getMapData().heightData;
        if (testX >= heightData.length || testY >= heightData[0].length)
            return true;
        int testHeight = currentTimeline.getMapData().heightData[testX][testY];
        Entity testE = getWalkableEntityOnTile(testX, testY);
        if (testE != null && testE.tileHeight() == e.tileHeight()) // if there is a walkable entity at the same height
            testHeight = Math.max(testHeight, testE.tileHeight());
        return testHeight != e.tileHeight();
    }
    /**
     * If there is a walkable entity on this space, return it.
     * A walkable entity will make the entire tile space its origin is in walkable.
     * This is for simplicity's sake and to make colliding with the map work smoothly.
     * @param x
     * @param y 
     * @return Returns null if there is no entity.
     */
    public static Entity getWalkableEntityOnTile(int x, int y) {
        for (Entity e : currentTimeline.getEntities()) {
            if (e.isWalkable())
                if ((int)e.posX() == x && (int)e.posY() == y)
                    return e;
        }
        return null;
    }
}