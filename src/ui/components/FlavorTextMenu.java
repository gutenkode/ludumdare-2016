package ui.components;

import mote4.scenegraph.Window;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import main.Vars;
import ui.MenuMeshCreator;

/**
 * Used for displaying a brief description of items and skills
 * @author Peter
 */
public class FlavorTextMenu {
    
    private static Mesh border, text;
    private static int width = 0, height = 0, lastWidth, lastHeight,
                       renderWidth = 0, renderHeight = 0;
    private static double menuExpandStartTime;
    private static final double MENU_EXPAND_TIME_SECS = 0.2;
    
    public static int width() { return renderWidth; }
    public static int height() { return renderHeight; }
    
    public static void setText(String s) {
        if (border != null)
            border.destroy();
        if (text != null)
            text.destroy();
        
        FontUtils.useMetric("font_1");
        //text = FontUtils.createString(s, Const.UI_SCALE/2, Const.UI_SCALE/2, Const.UI_SCALE, Const.UI_SCALE);
        text = new ScrollingText(s, "font_1", Vars.UI_SCALE/2, Vars.UI_SCALE/2, Vars.UI_SCALE, Vars.UI_SCALE, 60*3);

        String[] lines = s.split("\n");
        float maxWidth = 0;
        for (String s1 : lines)
            maxWidth = Math.max(maxWidth, FontUtils.getStringWidth(s1));

        menuExpandStartTime = Window.time();
        renderHeight = lastHeight = height;
        renderWidth = lastWidth = width;
        height = (lines.length-1)* Vars.UI_SCALE;
        width = (int)(Vars.UI_SCALE*maxWidth)- Vars.UI_SCALE;
        //border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, lastWidth, lastHeight, Vars.UI_SCALE);
    }
    
    public static void render() {
        redrawBorder();
        
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        text.render();
    }
    private static void redrawBorder() {
        double currentTime = Window.time();
        if (currentTime <= (menuExpandStartTime + MENU_EXPAND_TIME_SECS) ||
                (renderHeight != height && renderWidth != width))
        {
            double step = Vars.smoothStep(menuExpandStartTime, menuExpandStartTime + MENU_EXPAND_TIME_SECS, currentTime);
            renderWidth = (int)Vars.lerp(lastWidth, width, step);
            renderHeight = (int)Vars.lerp(lastHeight, height, step);

            if (border != null)
                border.destroy();
            border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, renderWidth, renderHeight, Vars.UI_SCALE);
        }
    }
}
