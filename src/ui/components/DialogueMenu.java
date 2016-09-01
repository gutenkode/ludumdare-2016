package ui.components;

import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import nullset.Const;
import ui.MenuMeshCreator;

/**
 * A DialogueMenu is the main box for dialogue and description text.
 * It displays as a long bar, as is standard in RPGs.
 * @author Peter
 */
public class DialogueMenu {
    
    private static Mesh border;
    private static ScrollingText text;
    public static final int BORDER_W = 350, 
                            BORDER_H = 48;
    
    static {
        border = MenuMeshCreator.create(Const.UI_SCALE,Const.UI_SCALE, BORDER_W, BORDER_H, Const.UI_SCALE);
    }
    
    public static void setText(String s) {
        if (text != null)
            text.destroy();
        
        //FontUtils.useMetric("font_1");
        text = new ScrollingText(s, "font_1", Const.UI_SCALE/2, Const.UI_SCALE/2, Const.UI_SCALE, Const.UI_SCALE, 2);
    }
    
    public static void render() {
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        text.render();
    }
}
