package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;
import rpgsystem.Inventory;
import rpgsystem.Item;

/**
 * A door that will only open if the player has the right keycard, 
 * and only if there is no security alert active.
 * @author Peter
 */
public class KeyDoor extends Entity {
    
    private static Mesh mesh;
    
    private float rot;
    private int keycardLevel;
    private int openVal = 0, delay = 0, alertCycle = 0;
    private boolean playerOn = false;
    
    static {
        mesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN, 
                3, new float[] {0,0,0, 
                                1,0,0, 
                                1,0,1,
                                0,0,1}, 
                2, new float[] {0,1,
                                1,1, 
                                1,0, 
                                0,0}, 
                0, null, new float[] {0,1,0, 0,1,0, 0,1,0, 0,1,0});
    }
    
    public KeyDoor(int x, int y, int l, int dir) {
        // dir defines the wall that the door is against
        // 0 = up, 1 = right, 2 = down, 3 = left
        switch (dir) {
            case 0:
                posX = x+.5f;
                posY = y;
                hitboxW = .5f;
                hitboxH = 0;
                break;
            case 1:
                posX = x+1;
                posY = y+.5f;
                hitboxW = 0;
                hitboxH = .5f;
                break;
            case 2:
                posX = x+.5f;
                posY = y+1;
                hitboxW = .5f;
                hitboxH = 0;
                break;
            case 3:
                posX = x;
                posY = y+.5f;
                hitboxW = 0;
                hitboxH = .5f;
                break;
            default:
                throw new IllegalArgumentException();
        }
        
        rot = dir*(float)(Math.PI/2);
        keycardLevel = l;
    }
    
    @Override
    public void onRoomInit() {
        // doors are complicated, I'll deal with this later...
    }
    
    @Override
    public boolean isSolid() {
        return openVal == 0;
    }

    @Override
    public void update() {
        Player p = MapManager.getPlayer();
        double dist = Math.sqrt( Math.pow(p.posX-posX, 2) + Math.pow(p.posY-posY, 2));
        // if the player is in range of the door
        if (dist < 1.5) {
            // only open if the player has the right keycard
            switch (keycardLevel) {
                case 0:
                    playerOn = true;
                    break;
                case 1:
                    if (Inventory.hasItem(Item.KEYCARD1))
                        playerOn = true;
                case 2:
                    if (Inventory.hasItem(Item.KEYCARD2))
                        playerOn = true;
                case 3:
                    if (Inventory.hasItem(Item.KEYCARD3))
                        playerOn = true;
                case 4:
                    if (Inventory.hasItem(Item.KEYCARD4))
                        playerOn = true;
                    break;
            }
        }
        
        delay--;
        if (delay <= 0) {
            delay = 6;
            
            if (MapManager.getTimelineState().isAlertTriggered()) {
                openVal = 0;
                alertCycle++;
                alertCycle %= 4;
            } else {
                if (playerOn) {
                    if (openVal < 3)
                        openVal++;
                } else {
                    if (openVal > 0)
                        openVal--;
                }
            }
            
            playerOn = false;
        }
    }

    @Override
    public void render(TransformationMatrix model) {
        model.translate((float)posX, (float)posY, tileHeight());
        model.rotate(rot, 0, 0, 1);
        model.translate(-.5f, 0);
        model.scale(1,1,2);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 3,7,keycardLevel+3*openVal);
        TextureMap.bindUnfiltered("entity_keyDoor");
        mesh.render();
        
        if (MapManager.getTimelineState().isAlertTriggered()) {
            Uniform.varFloat("emissiveMult", 1);
            Uniform.varFloat("spriteInfo", 3,7,12+alertCycle);
            Uniform.varFloat("spriteInfoEmissive", 3,7,12+alertCycle);
            model.translate(0,.35f,0);
            model.makeCurrent();
            mesh.render();
            Uniform.varFloat("spriteInfo", 3,7,16+alertCycle/2);
            Uniform.varFloat("spriteInfoEmissive", 3,7,16+alertCycle/2);
            model.translate(0,.15f,0);
            model.makeCurrent();
            mesh.render();
            Uniform.varFloat("emissiveMult", 0);
        }
    }

    @Override
    public String getName() { return "Door"; }
}
