package entities;

import mote4.util.matrix.TransformationMatrix;
import mote4.util.vertex.mesh.MeshMap;

/**
 * 
 * @author Peter
 */
public abstract class Entity {
    
    protected float posX = 0, posY = 0, // X and Y coordinates 
                     hitboxW = .49f, hitboxH = .49f; // half the total width and height of the hitbox, less than .5f to avoid edge cases
    protected int tileHeight = 0; // Y coordinate, height
    
    public void onRoomInit() {}
    public void moveTo(float x, float y) { posX = x; posY = y; }
    public void moveTo(int... coord) { posX = coord[0]; posY = coord[1]; }
    public void moveTo(float... coord) { posX = coord[0]; posY = coord[1]; }
    
    public float posX() { return posX; }
    public float posY() { return posY; }
    public float hitboxW() { return hitboxW; }
    public float hitboxH() { return hitboxH; }
    public final int tileHeight() { return tileHeight; }
    public abstract String getName();
    public String getAttributeString() {
        return "x:"+posX()+", y:"+posY()+", height:"+tileHeight()+
               "\nhitboxH:"+hitboxW()+", hitboxH:"+hitboxH();
    }
    /**
     * Get a string representing this entity,
     * to be used for storing it in a level file.
     * @return
     */
    public abstract String serialize();
    
    /**
     * Whether this entity should be treated as a solid object.
     * @return 
     */
    public boolean isSolid() { return false; }
    /**
     * Whether this entity should be able to be walked over.
     * Useful for objects like toggleable platforms.
     * @return 
     */
    public boolean isWalkable() { return false; }
    
    /**
     * Called when the player presses the use key on this object.
     */
    public void playerUse() {}
    /**
     * Called when the player's center point is inside this entity's hitbox.
     * Only applicable to non-solid entities.
     */
    public void playerPointIn() {}
    /**
     * Called when the player hitbox is entirely inside this entity's hitbox.
     * Only applicable to non-solid entities.
     */
    public void playerBoxIn() {}
    
    public abstract void update();
    public abstract void render(TransformationMatrix model);
    public final void renderHitbox(TransformationMatrix model) {
        model.translate(posX, posY, tileHeight()+.5f);
        model.scale(hitboxW, hitboxH, -.5f);
        model.bind();
        MeshMap.render("cube");
    }
    public void renderShadow(TransformationMatrix model) { render(model); }
    
    /**
    * Whether this entity collides with another entity.
    * Entities must have intersecting hitboxes and be on the same tile height.
    * This method does not check whether either entity is solid.
    * @param e
    * @return 
    */
    public final boolean collides(Entity e) {
        return (tileHeight == e.tileHeight &&
                e.posX-e.hitboxW <= posX+hitboxW && e.posX+e.hitboxW >= posX-hitboxW &&
                e.posY-e.hitboxH <= posY+hitboxH && e.posY+e.hitboxH >= posY-hitboxH);
    }
    /**
    * Whether this entity collides with another entity.
    * Entities must have intersecting hitboxes and be on the same tile height.
    * This method does not check whether either entity is solid.
    * To make this method useful for collision detection, the position of this
    * entity can be offset to test collisions relative to the actual location.
    * @param e
     * @param chgX X position offset.
     * @param chgY Y position offset.
    * @return 
    */
    public final boolean collides(Entity e, float chgX, float chgY) {
        return (tileHeight == e.tileHeight &&
                e.posX-e.hitboxW <= posX+hitboxW+chgX && e.posX+e.hitboxW >= posX-hitboxW+chgX &&
                e.posY-e.hitboxH <= posY+hitboxH+chgY && e.posY+e.hitboxH >= posY-hitboxH+chgY);
    }
    /**
     * Whether this entity's hitbox is entirely enclosed by the hitbox
     * of the specified entity.
     * @param e
     * @return 
     */
    public final boolean isInside(Entity e) {
        return (tileHeight == e.tileHeight &&
                e.posX-e.hitboxW <= posX-hitboxW && e.posX+e.hitboxW >= posX+hitboxW &&
                e.posY-e.hitboxH <= posY-hitboxH && e.posY+e.hitboxH >= posY+hitboxH);
    }
    /**
     * Whether the specified point is inside the hitbox of this entity.
     * @param x
     * @param y
     * @return 
     */
    public final boolean isInside(float x, float y) {
        return (x > posX-hitboxW && x < posX+hitboxW &&
                y > posY-hitboxH && y < posY+hitboxH);
    }

    /**
     * Whether this entity projects a light.
     */
    public boolean hasLight() { return false; }
    public float[] lightPos() { return new float[] {posX,posY,tileHeight+.5f}; }
    public float[] lightColor() { return new float[] {1,1,1}; }
}
