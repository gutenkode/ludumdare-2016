package map;

import entities.Entity;
import entities.Player;
import entities.RoomLink;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Encapsulates the state of a single timeline.
 * @author Peter
 */
public class Timeline {
    
    private HashMap<MapData, ArrayList<Entity>> entityStates = new HashMap<>(); // stores the state of entities for each room
    private TimelineState state;
    private Player player;
    private MapData mapData;
    
    protected Timeline() {
        state = new TimelineState();
        int[] pos = MapLevelManager.playerStartPos();
        player = new Player(pos[0], pos[1]);
    }


    /**
     * Get the entities for the current MapData of this Timeline.
     * @return
     */
    public ArrayList<Entity> getEntities() { return getEntities(mapData); }
    /**
     * Get the entities of the specified MapData.  Null if room has not been loaded.
     * @param md
     * @return 
     */
    public ArrayList<Entity> getEntities(MapData md) { return entityStates.get(md); }

    /**
     * Returns the RoomLink Entity with the corresponding room name,
     * for the current room only.  Null if there is no such RoomLink.
     * @param roomName
     * @return
     */
    public RoomLink getRoomLink(String roomName) {
        for (Entity e : getEntities())
            if (e instanceof RoomLink) {
                RoomLink r = (RoomLink)e;
                if (r.roomName().equals(roomName))
                    return r;
            }
        return null;
    }
    
    public TimelineState getState() { return state; }
    public Player getPlayer() { return player; }
    public void setMapData(MapData md) { 
        mapData = md; 
        createEntities(mapData);
    }
    private void createEntities(MapData md) {
        // only create a new set of entities if they
        // have not already been created in this timeline
        if (!entityStates.containsKey(md))
            entityStates.put(md, MapDataUtility.constructEntities(md.entities));
    }
    public MapData getMapData() { return mapData; }
}
