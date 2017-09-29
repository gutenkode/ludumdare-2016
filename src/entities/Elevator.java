package entities;

import map.MapManager;
import mote4.scenegraph.Window;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import main.Input;
import main.Vars;
import org.lwjgl.opengl.GL11;

/**
 * Moves up and down, allowing the player to move between height levels.
 * @author Peter
 */
public class Elevator extends Entity {
    
    private static Mesh mesh;
    
    private boolean state, inputLockActive = false;
    private float height, cycle,
                floatTileHeight;
    private int baseTileHeight, maxHeight,
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
    
    public Elevator(int x, int y, int h, boolean up) {
        posX = x+.5f;
        posY = y+.5f;
        maxHeight = h;
        height = 0;
        onRoomInit();
        state = up;
    }
    
    @Override
    public void onRoomInit() {
        //lightCycle = (int)(posX*4+posY*8);
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
                cycle += (Window.delta()*2.5)/maxHeight;
            else {
                cycle = 1;
                if (inputLockActive)
                    Input.popLock(Input.Lock.ELEVATOR);
                inputLockActive = false;
            }
        } else {
            if (cycle > 0)
                cycle -= (Window.delta()*2.5)/maxHeight;
            else {
                cycle = 0;
                if (inputLockActive)
                    Input.popLock(Input.Lock.ELEVATOR);
                inputLockActive = false;
            }
        }
        
        height = (float)Vars.smoothStep(cycle);
        floatTileHeight = baseTileHeight+Math.max(.075f, maxHeight*height);
        tileHeight = Math.round(floatTileHeight);
    }
    
    @Override
    public void render(TransformationMatrix model) {
        model.translate(posX-.5f, posY-.5f, floatTileHeight);
        model.bind();
        
        Uniform.vec("spriteInfo", 3,1,0);
        Uniform.vec("emissiveMult", (float)(Math.sin(Window.time() * 2.5)+2)/2);
        Uniform.vec("spriteInfoEmissive", 3,1, 2);
        TextureMap.bindUnfiltered("entity_elevator");
        mesh.render();
        
        Uniform.vec("emissiveMult", 0);
    }

    @Override
    public String getName() { return "Elevator"; }
    @Override
    public String getAttributeString() {
        return super.getAttributeString()+"\nbaseHeight:"+baseTileHeight+", maxHeight:base+"+maxHeight+", up:"+state;
    }
    @Override
    public String serialize() {
        return this.getClass().getSimpleName() +","+ (int)(posX-.5) +","+ (int)(posY-.5) +","+ maxHeight +","+ state;
    }
}
