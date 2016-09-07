package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;

/**
 * Will either be walkable or an empty tile depending on the corresponding
 * global value.
 * @author Peter
 */
public class SolidPlatform extends Entity {
    
    private static Mesh mesh;
    
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

    public SolidPlatform(int x, int y, int h) {
        posX = x+.5f;
        posY = y+.5f;
        tileHeight = h;
    }
    
    @Override
    public void onRoomInit() {}
    
    @Override
    public boolean isWalkable() {
        return true;
    }
    
    @Override
    public void update() {}

    @Override
    public void render(TransformationMatrix model) {
        model.setIdentity();
        model.translate((float)posX-.5f, (float)posY-.5f, tileHeight);
        model.makeCurrent();
        
        Uniform.varFloat("spriteInfo", 1,1,0);
        TextureMap.bindUnfiltered("entity_solidPlatform");
        mesh.render();
    }
    
}