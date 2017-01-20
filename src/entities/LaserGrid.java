package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;
import ui.IngameUIManager;

/**
 * Triggers a security alert when the player enters its hitbox.
 * @author Peter
 */
public class LaserGrid extends Entity {
    
    private static Mesh mesh;
    
    private float cycle;
    
    static {
        float width = 1;
        mesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                3, new float[] {0,0,0, width,0,0, width,0,2, 0,0,2},
                2, new float[] {0,1, 1,1, 1,0, 0,0},
                0, null, new float[] {0,1,0, 0,1,0, 0,1,0, 0,1,0});
    }
    
    public LaserGrid(int x, int y, int l) {
        hitboxW = l*.5f;
        hitboxH = .2f;
        posX = x+hitboxW;
        posY = y+.5f;
        tileHeight = MapManager.getTileHeight(x, y);
    }
    
    @Override
    public void update() {
        cycle += 1;
        cycle %= Math.PI*2;
    }
    
    @Override
    public void render(TransformationMatrix model) {
        float bob = (float)(Math.sin(cycle)+1)*.1f;
        model.translate(posX-hitboxW, posY, tileHeight-bob);
        model.scale(hitboxW*2, 1, 1);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 6,1,(int)(cycle%2));
        Uniform.varFloat("spriteInfoEmissive", 6,1,(int)(cycle%2));
        Uniform.varFloat("emissiveMult", 1);
        TextureMap.bindUnfiltered("entity_laserGrid");
        mesh.render();
        Uniform.varFloat("emissiveMult", 0);
    }
    
    @Override
    public void playerPointIn() {
        if (!MapManager.getTimelineState().isAlertTriggered()) {
            MapManager.getTimelineState().triggerAlert(true);
            IngameUIManager.logMessage("Security alert triggered.");
        }
    }

    @Override
    public String getName() { return "Laser Wall"; }
}
