package map;

import entities.Entity;
import entities.Player;
import entities.RoomLink;
import main.Input;
import main.RootLayer;
import mote4.util.matrix.CubeMapMatrix;
import mote4.util.matrix.Transform;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import scenes.Editor;
import scenes.Postprocess;

import java.util.ArrayList;

/**
 * Manages rendering and updating maps and entities.
 * TODO this is currently very monolithic.
 * @author Peter
 */
public class MapManager {
    private static Player player;
    private static Timeline currentTimeline;
    private static ArrayList<Timeline> timelines;
    private static int currentTimelineInd;
    // temporary list of entities that should be deleted, to avoid ConcurrentModificationExceptions
    private static ArrayList<Entity> deleteList = new ArrayList<>(); 
    private static String newMapName; // stored while a fadeout is performed
    // data for entity lights in shaders
    private static float[] eLightPos, eLightColor;
    private static TransformationMatrix shadowModel = new TransformationMatrix("modelMatrix"); // used when rendering shadows

    // initialization and timelines

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
        
        //for (Entity e : currentTimeline.getEntities())
        //    e.onRoomInit();
        player = currentTimeline.getPlayer();
        //player.onRoomInit();
        runOnRoomInit();
    }
    public static void initShaders() {
        // initialize uniform values once
        ShaderMap.use("ingame_light");
        Uniform.vec("ambient", 0,0,0);
        Uniform.sampler("shadowCubeMap", 1, "fbo_depth", true);
        Uniform.sampler("tex_shade", 2, "tileset_shade", false);
        Uniform.sampler("tex_bump", 3, "tileset_1_NRM", false);

        ShaderMap.use("spritesheet_light");
        Uniform.vec("ambient", 0,0,0);
        Uniform.sampler("shadowCubeMap", 1, "fbo_depth", true);
    }

    // update and room load process methods

    public static void update() {
        if (Input.currentLock() == Input.Lock.FADE)
            return; // don't update ANYTHING if the scene is fading
        // jump between timelines
        if (Input.currentLock() == Input.Lock.PLAYER) {
            if (Input.isKeyNew(Input.Keys.TIMELINE_1))
                jumpToTimeline(0);
            else if (Input.isKeyNew(Input.Keys.TIMELINE_2))
                jumpToTimeline(1);
            else if (Input.isKeyNew(Input.Keys.TIMELINE_3))
                jumpToTimeline(2);
        }
        
        player.update();
        
        while (!deleteList.isEmpty()) {
            currentTimeline.getEntities().remove(deleteList.remove(0));
        }
        // update all entities, test if they player is inside their hitboxes or is using them
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
     * Will load the specified room.  Currently somewhat broken;
     * if the room does not have a room link to the current room,
     * the game won't know where to put the player and will crash.
     * @param roomName
     */
    public static void loadRoom(String roomName) {
        newMapName = roomName;
        Postprocess.fadeOut(MapManager::fadeCallback);
        Input.pushLock(Input.Lock.FADE);
    }
    /**
     * Called when a room fade is done, performs the actual load action.
     */
    private static void fadeCallback() {
        String currentMapName = currentTimeline.getMapData().mapName; // the map we are about to unload
        MapData newMapData = MapLoader.getMap(newMapName); // get the MapData for the new room
        // newMapData will not be null; MapLoader will throw an error if no map is found
        currentTimeline.setMapData(newMapData);
        RoomLink newLink = currentTimeline.getRoomLink(currentMapName);

        float[] loc;
        if (newLink == null) {
            System.err.println("Linked room does not have reciprocating link. From: "
                    + currentMapName + " To: " + newMapName);
            loc = new float[] {0.5f,0.5f};
        } else
            loc = newLink.getFrontTile();

        player.moveTo(loc[0], loc[1]); // move the player to the new location
        runOnRoomInit();
        newMapName = null;
        Input.popLock(Input.Lock.FADE);

    }
    /**
     * Called whenever a room is loaded, initializes entities and lights.
     */
    private static void runOnRoomInit() {
        for (Entity e : currentTimeline.getEntities())
            e.onRoomInit();
        player.onRoomInit();
        refreshLighting();
    }

    /**
     * Update the light data sent to the shader, useful if
     * an entity toggles its lighting state.
     */
    public static void refreshLighting() {
        int numLights = 0;
        eLightPos = new float[16*3];
        eLightColor = new float[16*3];
        for (Entity e : currentTimeline.getEntities()) {
            if (e.hasLight()) {
                if (numLights >= eLightPos.length/3)
                    throw new IllegalStateException("Room has more entity lights than maximum.");
                float[] pos = e.lightPos();
                eLightPos[numLights*3] = pos[0];
                eLightPos[numLights*3+1] = pos[1];
                eLightPos[numLights*3+2] = pos[2];
                float[] color = e.lightColor();
                eLightColor[numLights*3] = color[0];
                eLightColor[numLights*3+1] = color[1];
                eLightColor[numLights*3+2] = color[2];
                numLights++;
            }
        }
        // send the data to shaders
        ShaderMap.use("ingame_light");
        Uniform.array("eLightPos",3, eLightPos);
        Uniform.array("eLightColor",3, eLightColor);
        ShaderMap.use("spritesheet_light");
        Uniform.array("eLightPos",3, eLightPos);
        Uniform.array("eLightColor",3, eLightColor);

        // set room size uniforms
        int[] m = currentMapSize();
        float[] mapSize = new float[] {m[0],m[1]};
        ShaderMap.use("ingame_light");
        Uniform.vec("mapSize", mapSize);
        ShaderMap.use("ingame_nolight");
        Uniform.vec("mapSize", mapSize);
    }

    // rendering

    /**
     * Renders the static map mesh and entity sprites.
     * @param trans Transform for the scene, includes camera information.
     *              It is passed in since it must be bound to multiple shaders.
     * @param flashlightDir Flashlight angle, in spherical coordinates.
     */
    public static void render(Transform trans, float[] flashlightDir) {
        float[] lightVector = new float[3];
        lightVector[0] = -(float)Math.sin(flashlightDir[0]);
        lightVector[1] = -(float)Math.cos(flashlightDir[0]);
        lightVector[2] = 0;
        
        // render static map mesh
        ShaderMap.use("ingame_light");
        TextureMap.bindUnfiltered("tileset_1");
        Uniform.vec("flashlightAngle", lightVector);
        Uniform.vec("lightPos", player.posX(), player.posY() +player.hitboxH(), player.elevatorHeight() +1f);
        trans.model.setIdentity();
        trans.bind();
        currentTimeline.getMapData().render();
        
        // render entity tilesheets
        ShaderMap.use("spritesheet_light");
        Uniform.vec("flashlightAngle", lightVector);
        Uniform.vec("lightPos", player.posX(), player.posY() +player.hitboxH(), player.elevatorHeight() +1f);
        trans.bind();
        player.render(trans.model);
        for (Entity e : currentTimeline.getEntities()) {
            trans.model.setIdentity();
            e.render(trans.model);
        }

        // render hitboxes
        /*ShaderMap.use("color");
        //Uniform.vec("colorMult", 1,0,0,1);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        trans.bind();
        trans.model.setIdentity();
        player.renderHitbox(trans.model);
        for (Entity e : currentTimeline.getEntities()) {
            trans.model.setIdentity();
            trans.model.bind();
            e.renderHitbox(trans.model);
        }
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);*/
    }

    /**
     * Renders meshes with only elements needed for constructing
     * the depth texture for shadow mapping.
     * @param shadowProj Projection for the shadow camera.
     */
    public static void renderForShadow(CubeMapMatrix shadowProj, TransformationMatrix shadowView) {
        ShaderMap.use("shadowCubeMap");
        shadowProj.bind();
        shadowView.bind();

        // render static map mesh
        shadowModel.setIdentity();
        shadowModel.bind();
        currentTimeline.getMapData().render();

        // render entity tilesheets, the player is not rendered
        for (Entity e : currentTimeline.getEntities()) {
            shadowModel.setIdentity();
            e.renderShadow(shadowModel);
        }
    }

    // state management methods

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
    public static int getTileHeight(int x, int y) {
        // somewhat hackish solution to making entities load in the editor
        // the engine wasn't designed to be used withouth the MapManagerz
        if (RootLayer.getState() == RootLayer.State.EDITOR)
            return Editor.getMapEditor().getMapData().heightData[x][y];
        return currentTimeline.getMapData().heightData[x][y];
    }
    public static Player getPlayer() { return player; }
    public static TimelineState getTimelineState() { return currentTimeline.getState(); }
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

    // collision detection methods

    /**
     * Tests whether an entity is colliding with the heightmap of any loaded maps.
     * Out of bounds is invalid.
     * @param e
     * @param chgX
     * @param chgY
     * @return 
     */
    public static boolean entityCollidesWithMap(Entity e, float chgX, float chgY) {
        MapData md = currentTimeline.getMapData();
        // out of bounds check
        if ((int)(e.posX()-e.hitboxW()+chgX) < 0 ||
            (int)(e.posX()+e.hitboxW()+chgX) >= md.width ||
            (int)(e.posY()-e.hitboxH()+chgY) < 0 ||
            (int)(e.posY()+e.hitboxH()+chgY) >= md.height)
            return true;
        // check if any of the corners of the entity are in a map square with a different height
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