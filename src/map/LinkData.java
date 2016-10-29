package map;

/**
 * Encapsulates a link to another map, held by MapData.
 * @author Peter
 */
public class LinkData {
    
    /**
     * The char ID of this room link.
     */
    protected final char id;
    /**
     * The name of the room to link to.
     */
    protected final String mapName;
    /**
     * X and Y tile coordinate of this room link.
     */
    protected final int x,y;
    /**
     * The direction this room link is facing.
     * Modified by MapEditor.
     */
    protected int direction;
    
    public LinkData(char id, String s, int x, int y, int dir) {
        this.id = id;
        mapName = s;
        this.x = x;
        this.y = y;
        direction = dir;
    }
    
    /**
     * Returns the tile "in front" of this link.
     * Used for where to place the player after linking.
     * @return 
     */
    public int[] getFrontTile() {
        switch (direction) {
            case 0:
                return new int[] {x-1,y};
            case 1:
                return new int[] {x,y+1};
            case 2:
                return new int[] {x+1,y};
            case 3:
                return new int[] {x,y-1};
            default:
                throw new IllegalStateException("Direction value of "+direction);
        }
    }
}