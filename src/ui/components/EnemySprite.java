package ui.components;

import mote4.util.matrix.ModelMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;
import rpgbattle.fighter.EnemyFighter;

/**
 *
 * @author Peter
 */
public class EnemySprite {
    
    private static Mesh sprite;
    
    static {
        sprite = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                                   2, new float[] {-96,-96, -96,96, 96,96, 96,-96},
                                   2, new float[] {0,0, 0,1, 1,1, 1,0},
                                   0, null, null);
    }
    
    private int posX, posY;
    private EnemyFighter fighter;
    
    public EnemySprite(int x, int y, EnemyFighter f) {
        posX = x;
        posY = y;
        fighter = f;
    }
    
    /**
     * Simply translates the model matrix to the position of this EnemySprite.
     * Used for rendering toasts.
     * @param model 
     */
    public void renderToast(ModelMatrix model) {
        model.translate(fighter.shakeValue()+posX, posY);
        model.makeCurrent();
        fighter.renderToast(model);
    }
    public void render(ModelMatrix model) {
        TextureMap.bindUnfiltered(fighter.spriteName);
        model.translate(fighter.shakeValue()+posX, posY);
        
        if (fighter.isDead())
            fighter.runDeathAnimation(model);
        
        model.makeCurrent();
        Uniform.varFloat("colorAdd", fighter.updateFlash());
        Uniform.varFloat("spriteInfo", fighter.updateSpriteInfo());
        sprite.render();
        Uniform.varFloat("colorAdd", 0,0,0);
    }
}
