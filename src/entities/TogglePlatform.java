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
public class TogglePlatform extends Entity {
    
    private static Mesh mesh;
    
    private int index, rotation;
    private float position;
    private boolean inverted;
    
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

    public TogglePlatform(int x, int y, int h, int i, int r, boolean inv) {
        posX = x+.5f;
        posY = y+.5f;
        tileHeight = h;
        index = i;
        rotation = r;
        inverted = inv;
    }
    
    @Override
    public void onRoomInit() {
        if (!MapManager.getTimelineState().getMapState(index) ^ inverted) {
            position = .9f;
        } else {
            position = 0;
        }
    }
    
    @Override
    public boolean isWalkable() {
        return MapManager.getTimelineState().getMapState(index) ^ inverted;
    }
    
    @Override
    public void update() {
        if (!MapManager.getTimelineState().getMapState(index) ^ inverted) {
            if (position < .9375)
                position += .0625;
        } else {
            if (position > 0)
                position -= .0625;
        }
    }

    @Override
    public void render(TransformationMatrix model) {
        model.setIdentity();
        model.translate((float)posX-.5f, (float)posY-.5f+.075f*position, tileHeight);
        // rotate to flip towards a certain wall
        //model.rotate((float)Math.PI/2*rotation, 0, 0, 1);
        // flip down if deactivated
        model.rotate(-(float)Math.PI/2*position, 1, 0, 0);
        
        model.makeCurrent();
        
        Uniform.varFloat("emissiveMult", 1);
        Uniform.varFloat("spriteInfo", 3,1,0);
        Uniform.varFloat("spriteInfoEmissive", 3,1,index+1);
        TextureMap.bindUnfiltered("entity_togglePlatform");
        mesh.render();
        Uniform.varFloat("emissiveMult", 0);
    }
    
}