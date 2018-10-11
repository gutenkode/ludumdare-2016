package ui.components;

import mote4.scenegraph.Window;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import main.RootLayer;
import main.Vars;
import org.lwjgl.opengl.GL11;
import rpgbattle.fighter.EnemyFighter;
import rpgsystem.StatusEffect;

/**
 *
 * @author Peter
 */
public class EnemySprite {
    
    public static final Mesh sprite, statusIconMesh, barMesh;
    private static final float barLength = 64;
    private Mesh statText;
    private int atk, def, mag;
    private boolean glow, showStats;
    private double glowCycle, showStatHealthChangeDelay;
    
    static {
        sprite = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                                   2, new float[] {-1,-1, -1,1, 1,1, 1,-1},
                                   //2, new float[] {-96,-96, -96,96, 96,96, 96,-96},
                                   2, new float[] {0,0, 0,1, 1,1, 1,0},
                                   0, null, null);
        statusIconMesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                2, new float[] {0,0, 0,9, 9,9, 9,0},
                2, new float[] {0,0, 0,1, 1,1, 1,0},
                0, null, null);
        barMesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                2, new float[] {0,0, 0,6, barLength,6, barLength,0},
                2, new float[] {0,0, 0,1, 1,1, 1,0},
                0, null, null);
    }
    
    private float posX, posY;
    private int posX2d, posY2d;
    private EnemyFighter fighter;
    private float lastHealth, renderHealth;

    private int currentFrame;
    private double currentFrameDelay;
    private float[] spriteInfo;

    public EnemySprite(EnemyFighter f) {
        this(0,0,f);
    }
    public EnemySprite(int x, int y, EnemyFighter f) {
        posX = x;
        posY = y;
        fighter = f;
        lastHealth = renderHealth = 0;

        currentFrame = (int)(Math.random()*f.frameDelay.length);
        currentFrameDelay = (int)(Math.random()*f.frameDelay[currentFrame]);
        spriteInfo = new float[] {f.frameDelay.length, 1, 0};

        statText = FontUtils.createStringColor("",0,0,0,0);
    }

    public void setPos(float x, float y) {
        posX = x;
        posY = y;
    }
    public void setPos2d(float... coords) {
        posX2d = (int)((coords[0]+1)/2*RootLayer.width());
        posY2d = (int)((coords[1]+1)/2*RootLayer.height());
    }
    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public float getRenderhealth() { return renderHealth; }

    /**
     * A constant pulsing glow, to indicate that this enemy is currently selected.
     * @param e
     */
    public void glow(boolean e) {
        glow(e,0);
    }
    public void glow(boolean e, double init) {
        glow = e;
        glowCycle = init;
        if (e)
            showStats(true);
    }
    public void showStats(boolean e) { showStats = e; }

    public void render3d(TransformationMatrix model) {
        TextureMap.bindUnfiltered(fighter.spriteName);

        model.setIdentity();
        fighter.updateShake(Window.delta());
        model.translate(fighter.shakeValue()*.01f+posX, 0, posY);
        if (fighter.isDead())
            fighter.runDeathAnimation(model);
        model.bind();

        // the sprite
        if (glow) {
            float c = .75f-(float)((Math.sin(glowCycle)+1)/4.0);
            Uniform.vec("colorAdd", c*.9f,c*.8f,c*.2f);
        } else
            Uniform.vec("colorAdd", fighter.updateFlash(Window.delta()));
        updateSpriteInfo();
        Uniform.vec("spriteInfo", spriteInfo);
        sprite.render(); // currently locked to 96*2 by 96*2
        Uniform.vec("colorAdd", 0,0,0);

        renderAnimations(model);
    }
    /**
     * Updates the battle animations for this fighter.
     * @param model
     */
    private void renderAnimations(TransformationMatrix model) {
        model.setIdentity();
        model.translate(posX, 0, posY+.1f); // z offset to prevent z-fighting
        model.bind();

        fighter.renderAnim();
    }

    public void render2d(TransformationMatrix model) {
        float healthPercent = (float)fighter.stats.health/fighter.stats.maxHealth;
        boolean healthIsCurrent = Math.abs(renderHealth-healthPercent) < .01;
        if (healthIsCurrent)
            lastHealth = healthPercent;
        else
            showStatHealthChangeDelay = .5; // in seconds

        if (showStats || !healthIsCurrent || showStatHealthChangeDelay > 0)
        {
            showStatHealthChangeDelay -= Window.delta();

            // health bar
            model.setIdentity();
            model.translate(posX2d - barLength / 2, posY2d);
            model.bind();
            TextureMap.bindUnfiltered("ui_statbars");
            renderBar(3, 0, 1, model);
            renderHealth -= (renderHealth - healthPercent) / 10 * (Window.delta()*60);
            renderBar(6, 0, lastHealth, model);
            renderBar(0, 0, renderHealth, model);

            // status icons
            model.translate(6, 4);
            model.bind();
            Uniform.vec("spriteInfo", 1,1,0);
            for (StatusEffect s : fighter.statusEffects) {
                TextureMap.bindUnfiltered(s.spriteName);
                statusIconMesh.render();

                model.translate(10, 0);
                model.bind();
            }
        }
    }
    /**
     * Simply translates the model matrix to the position of this EnemySprite.
     * Used for rendering toasts.
     * @param model
     */
    public void renderToast(TransformationMatrix model) {
        model.translate(posX2d, posY2d);
        model.bind();
        fighter.renderToast(model);
    }
    /**
     * Render the text showing stat buffs/debuffs.
     * @param model
     */
    public void renderStatText(TransformationMatrix model) {
        if (!showStats)
            return;
        int aBuff = fighter.stats.getAtkBuff();
        int dBuff = fighter.stats.getDefBuff();
        int mBuff = fighter.stats.getMagBuff();
        if (atk != aBuff || def != dBuff || mag != mBuff) {
            StringBuilder sb = new StringBuilder();
            if (aBuff > 0)
                sb.append("ATK +"+aBuff+" ");
            else if (aBuff < 0)
                sb.append("ATK "+aBuff+" ");
            if (dBuff > 0)
                sb.append("DEF +"+dBuff+" ");
            else if (aBuff < 0)
                sb.append("DEF "+dBuff+" ");
            if (mBuff > 0)
                sb.append("MAG +"+mBuff);
            else if (aBuff < 0)
                sb.append("MAG "+mBuff);
            FontUtils.useMetric("6px");
            statText.destroy();
            statText = FontUtils.createStringColor(sb.toString(), 0,0, Vars.UI_SCALE, Vars.UI_SCALE);
        }
        atk = aBuff;
        def = dBuff;
        mag = mBuff;

        model.translate(posX2d+barLength/2+1, posY2d);
        model.bind();
        statText.render();
    }

    /**
     * Utility to render3d the health bar.
     * @param index
     * @param yOffset
     * @param percent
     * @param model
     */
    private void renderBar(int index, int yOffset, float percent, TransformationMatrix model) {
        model.push();
        model.translate(0, yOffset);
        model.scale(percent, 1, 1);
        model.bind();
        Uniform.vec("spriteInfo", 8,1,index);
        barMesh.render();
        model.pop();
        model.bind();
    }
    /**
     * Updates animation data for this enemy and returns the array needed
     * for rendering the correct tilesheet frame.
     * @return
     */
    private void updateSpriteInfo() {
        currentFrameDelay -= Window.delta()*60;
        if (currentFrameDelay <= 0) {
            currentFrame++;
            currentFrame %= fighter.frameDelay.length;
            currentFrameDelay = fighter.frameDelay[currentFrame];
            spriteInfo[2] = currentFrame;
        }
        glowCycle += Window.delta()*9;
    }
}
