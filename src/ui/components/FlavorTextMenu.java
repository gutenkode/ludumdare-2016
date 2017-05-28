package ui.components;

import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import nullset.Vars;
import ui.MenuMeshCreator;

/**
 * Used for displaying a brief description of items and skills
 * @author Peter
 */
public class FlavorTextMenu {
    
    private static Mesh border, text;
    private static int width, height,
                       renderWidth = 0, renderHeight = 0;
    
    public static int width() { return width; }
    public static int height() { return height; }
    
    public static void setText(String s) {
        if (border != null)
            border.destroy();
        if (text != null)
            text.destroy();
        
        FontUtils.useMetric("font_1");
        //text = FontUtils.createString(s, Const.UI_SCALE/2, Const.UI_SCALE/2, Const.UI_SCALE, Const.UI_SCALE);
        text = new ScrollingText(s, "font_1", Vars.UI_SCALE/2, Vars.UI_SCALE/2, Vars.UI_SCALE, Vars.UI_SCALE, 3);

        String[] lines = s.split("\n");
        float maxWidth = 0;
        for (String s1 : lines)
            maxWidth = Math.max(maxWidth, FontUtils.getStringWidth(s1));

        height = (lines.length-1)* Vars.UI_SCALE;
        width = (int)(Vars.UI_SCALE*maxWidth)- Vars.UI_SCALE;
        border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, renderWidth, renderHeight, Vars.UI_SCALE);
    }
    
    public static void render() {
        redrawBorder();
        
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        text.render();
    }
    private static void redrawBorder() {
        // the text box will expand out from size 0,0
        boolean redraw = false;
        if (renderHeight > height) {
            renderHeight -= (renderHeight-height)/2;
            redraw = true;
        } else if (renderHeight < height) {
            renderHeight += (height-renderHeight)/2;
            redraw = true;
        }
        if (renderWidth > width) {
            renderWidth -= (renderWidth-width)/3;
            redraw = true;
        } else if (renderWidth < width) {
            renderWidth += (width-renderWidth)/3;
            redraw = true;
        }
        if (redraw) {
            if (border != null)
                border.destroy();
            border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, renderWidth, renderHeight, Vars.UI_SCALE);
        }
    }
}
