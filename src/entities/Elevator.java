package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import nullset.Input;
import org.lwjgl.opengl.GL11;

/**
 * Moves up and down, allowing the player to move between height levels.
 * @author Peter
 */
public class Elevator extends Entity {
    
    private static Mesh mesh;
    
    private boolean state = true, inputLockActive = false;
    private float height, cycle,
                  floatTileHeight;
    private int baseTileHeight, maxHeight,
                lightCycle = 0,
                triggerCooldown = 0; // to only trigger when the player first gets on
    
    static {
        mesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN, 
                3, new float[] {0,0, 0, 
                                0,1, 0, 
                                1,1, 0,
                                1,0, 0}, 
                2, new float[] {0,1,
                                1,1, 
                                1,0, 
                                0,0}, 
                0, null, new float[] {0,0,1, 0,0,1, 0,0,1, 0,0,1});
    }
    
    public Elevator(int x, int y, int h) {
        posX = x+.5f;
        posY = y+.5f;
        maxHeight = h;
        height = 0;
        onRoomInit();
        state = true;
    }
    
    @Override
    public void onRoomInit() {
        baseTileHeight = MapManager.getTileHeight((int)posX, (int)posY);
        if (state) {
            cycle = 1;
        } else {
            cycle = 0;
        }
    }
    
    @Override
    public boolean isWalkable() {
        return true;
    }
    
    @Override
    public void playerBoxIn() {
        if (triggerCooldown <= 0 && !inputLockActive) {
            state = !state;
            inputLockActive = true;
            Input.pushLock(Input.Lock.ELEVATOR);
        }
        triggerCooldown = 3;
        MapManager.getPlayer().setElevatorHeight(floatTileHeight);
    }
    
    @Override
    public void update() {
        triggerCooldown--;
        if (state) {
            if (cycle < 1)
                cycle += .04/maxHeight;
            else {
                cycle = 1;
                if (inputLockActive)
                    Input.popLock();
                inputLockActive = false;
            }
        } else {
            if (cycle > 0)
                cycle -= .04/maxHeight;
            else {
                cycle = 0;
                if (inputLockActive)
                    Input.popLock();
                inputLockActive = false;
            }
        }
        
        height = (float)Math.sin(cycle*Math.PI -Math.PI/2);
        height = (height+1)/2;
        floatTileHeight = baseTileHeight+maxHeight*height;
        tileHeight = Math.round(floatTileHeight);
    }
    
    @Override
    public void render(TransformationMatrix model) {
        lightCycle++;
        lightCycle %= 100;
        
        model.translate((float)posX-.5f, (float)posY-.5f, floatTileHeight+.01f);
        model.makeCurrent();
        
        Uniform.varFloat("spriteInfo", 3,1,0);
        Uniform.varFloat("emissiveMult", 1);
        if (lightCycle > 70)
            Uniform.varFloat("spriteInfoEmissive", 3,1, 2);
        else
            Uniform.varFloat("spriteInfoEmissive", 3,1, 1);
        TextureMap.bindUnfiltered("entity_elevator");
        mesh.render();
        
        Uniform.varFloat("emissiveMult", 0);
    }
}
