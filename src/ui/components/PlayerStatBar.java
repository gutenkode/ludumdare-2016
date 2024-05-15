package ui.components;

import mote4.scenegraph.Window;
import mote4.util.matrix.Transform;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import main.Vars;
import org.lwjgl.opengl.GL11;
import rpgbattle.BattleManager;
import rpgbattle.fighter.FighterStats;
import rpgsystem.StatusEffect;
import ui.MenuMeshCreator;

/**
 * Renders the player status bar in a battle.
 * @author Peter
 */
public class PlayerStatBar {
    
    public static final int BORDER_W = 65, 
                            BORDER_H = 10;
    
    private static Mesh borderMesh, statusIconMesh, barMesh;
    private static Mesh[] textMesh;
    
    private static double statPreviewCycle;
    private static boolean manaPreview, staminaPreview, statPreviewTooHigh;
    private static float healthRender, staminaRender, manaRender;
    private static int statCostPreview,
                       lastHealthText, lastStaminaText, lastManaText;

    static {
        lastHealthText = lastStaminaText = lastManaText = -1;
        healthRender = staminaRender = manaRender = 0;
        
        borderMesh = MenuMeshCreator.create(50, 0, BORDER_W, BORDER_H, Vars.UI_SCALE);

        statusIconMesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                2, new float[] {0,0, 0,9, 9,9, 9,0},
                2, new float[] {0,0, 0,1, 1,1, 1,0},
                0, null, null);
        
        float barLength = 40;
        barMesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN, 
                2, new float[] {0,0, 0,6, barLength,6, barLength,0}, 
                2, new float[] {0,0, 0,1, 1,1, 1,0}, 
                0, null, null);
        
        textMesh = new Mesh[4];
        FontUtils.useMetric("6px");
        textMesh[0] = FontUtils.createString("PLAYER", 0, -9, 16, 16);
        textMesh[1] = FontUtils.createString("HP "+0, 41, 0, 16, 16);
        textMesh[2] = FontUtils.createString("SP "+0, 41, 7, 16, 16);
        textMesh[3] = FontUtils.createString("MP "+0, 41, 14, 16, 16);
    }
    
    /**
     * Shows the portion of the mana bar that a skill will use.
     * @param cost 
     */
    public static void previewManaCost(int cost) {
        statCostPreview = cost;
        manaPreview = true; staminaPreview = false;
        // if the cost prediction is bigger than the available mana
        statPreviewTooHigh = cost > BattleManager.getPlayer().stats.mana;
    }
    public static void previewStaminaCost(int cost) { previewStaminaCost(cost, false); }
    public static void previewStaminaCost(int cost, boolean enableTooHighColor) {
        statCostPreview = cost;
        staminaPreview = true; manaPreview = false;
        // if the cost prediction is bigger than the available stamina
        if (enableTooHighColor)
            statPreviewTooHigh = cost > BattleManager.getPlayer().stats.stamina;
        else
            statPreviewTooHigh = false;
    }
    public static void stopStatPreview() {
        manaPreview = false;
        staminaPreview = false;
    }

    public static float getRenderHealth() { return healthRender; }

    /**
     * Renders the player's stat box, detailing how much of each stat the player has,
     * as well as any status conditions the player currently has.
     * This class will set its own shaders and transformation matrices.
     * @param x
     * @param y
     * @param trans
     */
    public static void render(int x, int y, Transform trans) {
        TransformationMatrix model = trans.model;
        
        // ew...
        BattleManager.getPlayer().updateShake(Window.delta());
        float playerShake = BattleManager.getPlayer().shakeValue();
        FighterStats st = BattleManager.getPlayer().stats;
        int lastHealth = BattleManager.getPlayer().lastHealth();
        int lastStamina = BattleManager.getPlayer().lastStamina();
        int lastMana = BattleManager.getPlayer().lastMana();
        
        // offset to correct location, add pixel shift with playerShake
        statPreviewCycle = Window.time()*9;
        model.setIdentity();
        model.translate(x-80+(int)playerShake, y+ Vars.UI_SCALE);
        model.bind();
        
        // smooth sliding of stat bars
        double d = Window.delta()*60;
        healthRender -= (healthRender-st.health)/10 *d;
        staminaRender -= (staminaRender-st.stamina)/10 *d;
        manaRender -= (manaRender-st.mana)/10 *d;
        boolean renderLastHealth = Math.abs(healthRender-st.health) < .1;
        boolean renderLastStamina = Math.abs(staminaRender-st.stamina) < .1;
        boolean renderLastMana = Math.abs(manaRender-st.mana) < .1;
        // these checks prevent healthRender from rounding to 99 if the value is 100
        // since the render3d values are used for the small numbers next to each bar
        if (renderLastHealth)
            healthRender = st.health;
        if (renderLastStamina)
            staminaRender = st.stamina;
        if (renderLastMana)
            manaRender = st.mana;
        
    // RENDER BORDER BOX
        
        ShaderMap.use("texture_uiblur");
        trans.bind();
        
        Uniform.vec("colorAdd", BattleManager.getPlayer().updateFlash(Window.delta()));
        TextureMap.bindUnfiltered("ui_scalablemenu");
        borderMesh.render();
        Uniform.vec("colorAdd", 0,0,0);
        
    // RENDER STAT BARS
        
        ShaderMap.use("spritesheet_nolight");
        trans.bind();
        
        // translate to stat bar location
        model.translate(40, 0);
        model.bind();
        
        // stat bar background
        TextureMap.bindUnfiltered("ui_statbars");
        renderBar(3, 0, 1, model);
        renderBar(3, 7, 1, model);
        renderBar(3, 14, 1, model);
        // white bar lost/gained section
        if (!renderLastHealth)
            renderBar(6, 0, (float)lastHealth/st.maxHealth, model);
        if (!renderLastStamina)
            renderBar(6, 7, (float)lastStamina/st.maxStamina, model);
        if (!renderLastMana)
            renderBar(6, 14, (float)lastMana/st.maxMana, model);
        // current bar
        renderBar(0, 0, healthRender/st.maxHealth, model);
        renderBar(1, 7, staminaRender/st.maxStamina, model);
        renderBar(2, 14, manaRender/st.maxMana, model);

        // glowing preview of stat cost for a skill
        if (manaPreview) {
            // glowing bar portion
            float c = (float)(Math.sin(statPreviewCycle)+1)*.5f*.75f+.25f;
            Uniform.vec("colorMult", c,c,c,1);
            if (statPreviewTooHigh)
                renderBar(0, 14, manaRender/st.maxMana, model);
            else {
                renderBar(5, 14, manaRender / st.maxMana, model);
                // remaining bar
                Uniform.vec("colorMult", 1, 1, 1, 1);
                float percent = (manaRender - statCostPreview) / st.maxMana;
                percent = Math.max(0, percent);
                renderBar(2, 14, percent, model);
            }
        }
        if (staminaPreview) {
            // glowing bar portion
            float c = (float)(Math.sin(statPreviewCycle)+1)*.5f*.75f+.25f;
            Uniform.vec("colorMult", c,c,c,1);
            if (statPreviewTooHigh)
                renderBar(0, 7, staminaRender/st.maxStamina, model);
            else {
                renderBar(5, 7, staminaRender / st.maxStamina, model);
                // remaining bar
                Uniform.vec("colorMult", 1, 1, 1, 1);
                float percent = (staminaRender - statCostPreview) / st.maxStamina;
                percent = Math.max(0, percent);
                renderBar(1, 7, percent, model);
            }
        }
        
    // RENDER STAT BAR TEXT
        
        ShaderMap.use("texture");
        trans.bind();
        
        // 6px font for stat numbers
        FontUtils.useMetric("6px");
        TextureMap.bindUnfiltered("font_6px");
        
        int val = (int)healthRender;
        if (lastHealthText != val) {
            lastHealthText = val;
            textMesh[1].destroy();
            textMesh[1] = FontUtils.createString("HP "+val, 41, 0, 16, 16);
        }
        val = (int)staminaRender;
        if (lastStaminaText != val) {
            lastStaminaText = val;
            textMesh[2].destroy();
            textMesh[2] = FontUtils.createString("SP "+val, 41, 7, 16, 16);
        }
        val = (int)manaRender;
        if (lastManaText != val) {
            lastManaText = val;
            textMesh[3].destroy();
            textMesh[3] = FontUtils.createString("MP "+val, 41, 14, 16, 16);
        }
        for (Mesh m : textMesh)
            m.render();
        
    // RENDER STATUS EFFECT ICONS
        
        model.translate(79, -13);
        model.bind();
        for (StatusEffect s : BattleManager.getPlayer().statusEffects) {
            TextureMap.bindUnfiltered(s.spriteName);
            statusIconMesh.render();
            
            model.translate(0, 10); // TODO after four stat boxes this will overlap the outer box, shift to the left
            model.bind();
        }
    }
    private static void renderBar(int index, int yOffset, float percent, TransformationMatrix model) {
        model.push();
        model.translate(0, yOffset);
        model.scale(percent, 1, 1);
        model.bind();
        Uniform.vec("spriteInfo", 8,1,index);
        barMesh.render();
        model.pop();
        model.bind();
    }

    public static void renderAnimations(float posX, float posY, TransformationMatrix model) {
        model.setIdentity();
        model.translate(posX, posY);
        model.bind();

        BattleManager.getPlayer().renderAnim();
    }
}
