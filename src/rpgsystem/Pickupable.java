package rpgsystem;

/**
 * Something that is "pickupable" can be obtained from
 * an ItemPickup entity in the overworld,
 * like items, skills, and skill modifiers.
 * Created by Peter on 1/25/17.
 */
public interface Pickupable {
    public String pickupName();
    public String overworldSprite();
    public void pickup();
}
