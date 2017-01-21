package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import ui.script.ScriptLoader;
import ui.IngameUIManager;

/**
 * Triggers a scripted scene when the player enters its hitbox.
 * @author Peter
 */
public class ScriptTrigger extends Entity {
    
    private boolean triggered = false;
    private int retriggerCooldown = 0;
    private String scriptName;
    private final int x, y;
    
    public ScriptTrigger(int x, int y, int width, int height, String scriptName) {
        hitboxW = width/2f -.01f; // -.01f is to get rid of any edge cases
        hitboxH = height/2f -.01f;
        posX = x+hitboxW;
        posY = y+hitboxH;
        this.x = x; this.y = y;
        this.scriptName = scriptName;
    }
    
    @Override
    public void onRoomInit() {
        tileHeight = MapManager.getTileHeight(x, y);
        retriggerCooldown = 0;
    }

    @Override
    public void update() {
    }

    @Override
    public void render(TransformationMatrix model) {
    }
    
    @Override
    public void playerPointIn() {
        if (!triggered && retriggerCooldown <= 0) {
            retriggerCooldown = 20;
            triggered = true;
            IngameUIManager.playScript(this, scriptName);
        } else
            retriggerCooldown--;
    }
    
    /**
     * Will reset this script's trigger, so the script can play again.
     */
    public void reset() { triggered = false; }

    @Override
    public String getName() { return "Script"; }
}