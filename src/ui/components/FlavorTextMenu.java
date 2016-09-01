package ui.components;

import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import nullset.Const;
import ui.MenuMeshCreator;

/**
 * Used for displaying a brief description of items and skills
 * @author Peter
 */
public class FlavorTextMenu {
    
    private static Mesh border, text;
    private static int width, height;
    
    public static int width() { return width; }
    public static int height() { return height; }
    
    public static void setText(String s) {
        if (border != null)
            border.destroy();
        if (text != null)
            text.destroy();
        
        FontUtils.useMetric("font_1");
        text = FontUtils.createString(s, Const.UI_SCALE/2, Const.UI_SCALE/2, Const.UI_SCALE, Const.UI_SCALE);
        
        String[] lines = s.split("\n");
        float maxWidth = 0;
        for (String s1 : lines)
            maxWidth = Math.max(maxWidth, FontUtils.getStringWidth(s1));
        
        height = (lines.length-1)*Const.UI_SCALE;
        width = (int)(Const.UI_SCALE*maxWidth)-Const.UI_SCALE;
        border = MenuMeshCreator.create(Const.UI_SCALE,Const.UI_SCALE, width, height, Const.UI_SCALE);
    }
    
    public static void render() {
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        text.render();
    }
}
