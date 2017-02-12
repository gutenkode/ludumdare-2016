package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;

/**
 * Created by Peter on 1/30/17.
 */
public class RoomLink extends Entity {

    private int direction;
    private String roomName;

    public RoomLink(int x, int y, int w, int h, int dir, String room) {
        posX = x+w/2f;
        hitboxW = w/2f+.01f;
        posY = y+h/2f;
        hitboxH = h/2f+.01f;
        direction = dir;
        roomName = room;
    }

    @Override
    public void onRoomInit() {
        tileHeight = MapManager.getTileHeight((int)posX,(int)posY);
    }

    @Override
    public void playerBoxIn() {
        MapManager.loadRoom(roomName);
    }

    /**
     * Returns the space "in front" of this link.
     * Used for determining where to place the player after linking.
     * @return
     */
    public float[] getFrontTile() {
        switch (direction) {
            case 0:
                return new float[] {posX-hitboxW/2-.3f, posY};
            case 1:
                return new float[] {posX, posY+hitboxH/2+.3f};
            case 2:
                return new float[] {posX+hitboxW/2+.3f, posY};
            case 3:
                return new float[] {posX, posY-hitboxH/2-.3f};
            default:
                throw new IllegalStateException("Direction value of "+direction);
        }
    }

    public String roomName() { return roomName; }

    @Override
    public void update() {}

    @Override
    public void render(TransformationMatrix model) {}

    @Override
    public String getName() { return "RoomLink"; }
}
