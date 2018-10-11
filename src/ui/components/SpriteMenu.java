package ui.components;

import mote4.util.matrix.TransformationMatrix;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import main.Vars;
import org.lwjgl.opengl.GL11;
import ui.MenuMeshCreator;

/**
 * Displays a sprite for character portraits or items.
 * @author Peter
 */
public class SpriteMenu {
    
    private static Mesh border, sprite;
    private static String spriteName;
    public static final int BORDER_W = 32+ Vars.UI_SCALE,
                            BORDER_H = 32+ Vars.UI_SCALE;
    
    static {
        border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, BORDER_W, BORDER_H, Vars.UI_SCALE);
        sprite = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN,
                                   2, new float[] {0,0, 0,64, 64,64, 64,0},
                                   2, new float[] {0,0, 0,1, 1,1, 1,0},
                                   0, null, null);
    }
    
    public static void setSprite(String s) {
        spriteName = s;
    }
    
    public static void render(TransformationMatrix model) {
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered(spriteName);
        model.translate(Vars.UI_SCALE/2, Vars.UI_SCALE/2);
        model.bind();
        sprite.render();
    }
}
