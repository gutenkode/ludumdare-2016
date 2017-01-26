package ui.components;

import mote4.util.matrix.ModelMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.StatEffect;

/**
 *
 * @author Peter
 */
public class EnemySprite {
    
    public static final Mesh sprite, statusIconMesh, barMesh;
    
    static {
        sprite = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                                   2, new float[] {-96,-96, -96,96, 96,96, 96,-96},
                                   2, new float[] {0,0, 0,1, 1,1, 1,0},
                                   0, null, null);
        statusIconMesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                2, new float[] {0,0, 0,9, 9,9, 9,0},
                2, new float[] {0,0, 0,1, 1,1, 1,0},
                0, null, null);
        float barLength = 96;
        barMesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                2, new float[] {0,0, 0,6, barLength,6, barLength,0},
                2, new float[] {0,0, 0,1, 1,1, 1,0},
                0, null, null);
    }
    
    private int posX, posY;
    private EnemyFighter fighter;
    private float lastHealth, renderHealth;
    
    public EnemySprite(int x, int y, EnemyFighter f) {
        posX = x;
        posY = y;
        fighter = f;
        lastHealth = renderHealth = 1;
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

        model.setIdentity();
        model.translate(fighter.shakeValue()+posX, posY);
        if (fighter.isDead())
            fighter.runDeathAnimation(model);
        model.makeCurrent();

        // the sprite
        Uniform.varFloat("colorAdd", fighter.updateFlash());
        Uniform.varFloat("spriteInfo", fighter.updateSpriteInfo());
        sprite.render(); // 96*2 by 96*2
        Uniform.varFloat("colorAdd", 0,0,0);

        // health bar
        model.translate(-48, 96);
        model.makeCurrent();
        TextureMap.bindUnfiltered("ui_statbars");
        renderBar(3, 0, 1, model);
        float healthPercent = (float)fighter.stats.health/fighter.stats.maxHealth;
        renderHealth -= (renderHealth-healthPercent)/10;
        renderBar(6, 0, lastHealth, model);
        renderBar(0, 0, renderHealth, model);
        if (Math.abs(renderHealth-healthPercent) < .01)
            lastHealth = healthPercent;

        // status icons
        model.translate(6, 4);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 1,1,0);
        for (StatEffect s : fighter.statEffects) {
            TextureMap.bindUnfiltered(s.spriteName);
            statusIconMesh.render();

            model.translate(10, 0);
            model.makeCurrent();
        }
    }
    private static void renderBar(int index, int yOffset, float percent, ModelMatrix model) {
        model.push();
        model.translate(0, yOffset);
        model.scale(percent, 1, 1);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 8,1,index);
        barMesh.render();
        model.pop();
        model.makeCurrent();
    }

    public void renderAnimations(ModelMatrix model) {
        model.setIdentity();
        model.translate(posX, posY);
        model.makeCurrent();

        fighter.updateAnim();
    }
}
