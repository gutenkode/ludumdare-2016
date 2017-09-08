package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;

/**
 * Toggles the state of a global value when the player walks over it.
 * @author Peter
 */
public class ToggleButton extends Entity {
    
    private static Mesh mesh;
    
    private int index;
    private int cooldown = 0;
    private float brightness = 0;
    
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
    
    public ToggleButton(int x, int y, int i) {
        posX = x+.5f;
        posY = y+.5f;
        index = i;
    }

    @Override
    public void onRoomInit() {
        tileHeight = MapManager.getTileHeight((int)posX, (int)posY);
    }

    @Override
    public void playerPointIn() {
        if (cooldown <= 0)
            MapManager.getTimelineState().toggleMapState(index);
        cooldown = 2;
    }
    
    @Override
    public void update() {
        if (cooldown > 0) {
            cooldown--;
            if (brightness < 1)
                brightness += .125;
        } else if (brightness > 0)
            brightness -= .125;
    }

    @Override
    public void render(TransformationMatrix model) {
        model.setIdentity();
        model.translate((float)posX-.5f, (float)posY-.5f, tileHeight+.075f);
        model.makeCurrent();
        
        Uniform.varFloat("spriteInfo", 2,4,2*index);
        if (brightness > 0) {
            Uniform.varFloat("emissiveMult", brightness);
            Uniform.varFloat("spriteInfoEmissive", 2,4,1+2*index);
        }
        TextureMap.bindUnfiltered("entity_toggleButton");
        mesh.render();
        
        Uniform.varFloat("emissiveMult", 0);
    }

    @Override
    public String getName() { return "Toggle Button"; }
    @Override
    public String getAttributeString() {
        return super.getAttributeString()+"\nindex:"+index;
    }
    @Override
    public boolean hasLight() { return true; }
    @Override
    public float[] lightPos() { return new float[] {posX,posY,tileHeight+.3f}; }
    @Override
    public float[] lightColor() {
        switch (index) {
            case 0:
                return new float[] {1,0,0};
            case 1:
                return new float[] {0,0,1};
            case 2:
                return new float[] {1,1,0};
            case 3:
                return new float[] {0,1,0};
            default:
                return new float[] {0,0,0};
        }
    }
    @Override
    public String serialize() {
        return this.getClass().getSimpleName() +","+ (int)(posX-.5) +","+ (int)(posY-.5) +","+ index;
    }
}