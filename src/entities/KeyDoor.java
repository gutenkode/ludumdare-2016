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
    
    private float rot, openVal = 0;
    private int keycardLevel, tileX, tileY;
    private int delay = 0, alertCycle = 0;
    private boolean playerOn = false, flicker;
    
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
        tileX = x;
        tileY = y;
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
        tileHeight = MapManager.getTileHeight((int)posX, (int)posY);
    }
    
    @Override
    public boolean isSolid() { return openVal <= 0; }

    @Override
    public void update() {
        Player p = MapManager.getPlayer();
        double dist = Math.sqrt( Math.pow(p.posX-posX, 2) + Math.pow(p.posY-posY, 2));
        // if the player is in range of the door
        if (dist < 1.5) {
            // only open if the player has the right keycard
            // higher-level keycards can open lower-level doors
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

        if (MapManager.getTimelineState().isAlertTriggered()) {
            openVal = 0;
            delay--;
            if (delay <= 0) {
                delay = 6;
                alertCycle++;
                alertCycle %= 4;
            }
        } else {
            if (playerOn) {
                if (openVal < 1)
                    openVal += .05;
            } else {
                if (openVal > 0)
                    openVal -= .05;
            }
        }

        playerOn = false;
        flicker = !flicker;
    }

    @Override
    public void render(TransformationMatrix model) {
        model.translate(posX, posY, tileHeight());
        model.rotate(rot, 0, 0, 1);
        model.translate(-.5f, 0);
        model.scale(1,1,2);
        model.makeCurrent();

        TextureMap.bindUnfiltered("entity_keyDoor");
        Uniform.varFloat("spriteInfo", 3,5,0);
        mesh.render();

        model.push();
        model.translate(-.5f*openVal, -.05f, 0);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 3,5,1);
        mesh.render();
        model.translate(2*.5f*openVal, 0, 0);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 3,5,2);
        mesh.render();
        model.pop();

        model.push();
        if (keycardLevel > 0 && openVal < 1) {
            int offset = 5;
            float slide = openVal*.6f;
            if (flicker) {
                offset += 6;
                slide *= -1;
            }
            Uniform.varFloat("emissiveMult", 1);
            Uniform.varFloat("spriteInfo", 3,10,keycardLevel+offset);
            Uniform.varFloat("spriteInfoEmissive", 3,10,keycardLevel+offset);
            model.translate(slide, .1f, .25f);
            model.scale(1,1,.5f);
            model.makeCurrent();
            mesh.render();
            Uniform.varFloat("emissiveMult", 0);
        }
        model.pop();

        if (MapManager.getTimelineState().isAlertTriggered()) {
            int offset = 10;
            if (flicker)
                offset += 6;
            Uniform.varFloat("emissiveMult", 1);

            // moving red lines
            Uniform.varFloat("spriteInfo", 3,10,alertCycle+offset+8);
            Uniform.varFloat("spriteInfoEmissive", 3,10,alertCycle+offset+8);
            model.scale(1,1,.5f);
            model.translate(0, .15f, .5f);
            model.makeCurrent();
            mesh.render();

            // flashing yellow warning sign
            Uniform.varFloat("spriteInfo", 3,10,alertCycle/2+offset);
            Uniform.varFloat("spriteInfoEmissive", 3,10,alertCycle/2+offset);
            model.translate(0, .05f, 0);
            model.makeCurrent();
            mesh.render();

            Uniform.varFloat("emissiveMult", 0);
        }

        /*
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
        */
    }

    @Override
    public String getName() { return "Door"; }

    @Override
    public boolean hasLight() { return keycardLevel > 0; }
    @Override
    public float[] lightPos() { return new float[] {tileX,tileY,tileHeight+1f}; }
    @Override
    public float[] lightColor() { return new float[] {1,0,0}; }
}
