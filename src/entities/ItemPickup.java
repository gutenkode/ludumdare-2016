package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import static org.lwjgl.opengl.GL11.*;
import rpgsystem.Inventory;
import rpgsystem.Item;
import ui.IngameUIManager;

/**
 * Gives the player an item when triggered.
 * @author Peter
 */
public class ItemPickup extends Entity {
    
    private static Mesh mesh;
    private boolean collected = false;
    private float cycle = 0, addSpeed = 0;
    private Item item;
    
    static {
        mesh = StaticMeshBuilder.constructVAO(GL_TRIANGLES, 
                3, new float[] {-.5f,0, 0, 
                                 .5f,0, 0, 
                                -.5f,0, 1,
                                -.5f,0, 1,
                                 .5f,0, 0,
                                 .5f,0, 1,
                                  
                                 .5f,0, 0, 
                                -.5f,0, 0, 
                                 .5f,0, 1,
                                 .5f,0, 1,
                                -.5f,0, 0,
                                -.5f,0, 1}, 
                2, new float[] {0,1,
                                1,1, 
                                0,0,
                                0,0,
                                1,1,
                                1,0,
                                
                                0,1,
                                1,1,
                                0,0,
                                0,0,
                                1,1,
                                1,0}, 
                0, null, new float[] {0,1,0, 0,1,0, 0,1,0, 0,1,0, 0,1,0, 0,1,0,
                                      0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0});
    }
    
    public ItemPickup(int x, int y, Item i) {
        posX = x+.5f;
        posY = y+.5f;
        hitboxW = .35f;
        hitboxH = .35f;
        item = i;
        
        tileHeight = MapManager.getTileHeight(x, y);
    }
    
    @Override
    public void update() {
        if (collected) {
            addSpeed += .03;
            if (addSpeed > 1)
                MapManager.removeEntity(this);
        }
        cycle += .05f+addSpeed*.35;
    }
    
    @Override
    public void render(TransformationMatrix model) {
        float floatHeight = (float)(Math.sin(cycle)/2+.5)/3;
        
        model.translate(posX, posY, floatHeight+tileHeight);
        model.scale(1-addSpeed,1,1);
        model.rotate(cycle, 0, 0, 1);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 1,1,0);
        TextureMap.bindUnfiltered(item.spriteName);
        glEnable(GL_CULL_FACE);
        mesh.render();
        glDisable(GL_CULL_FACE);
    }
    
    @Override
    public void playerPointIn() {
        if (!collected) {
            collected = true;
            Inventory.addItem(item);
            IngameUIManager.logMessage("Obtained a " + item.name + ".");
        }
    }
}
