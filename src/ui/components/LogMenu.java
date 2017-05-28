package ui.components;

import mote4.util.matrix.ModelMatrix;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import nullset.Vars;
import ui.MenuMeshCreator;

/**
 * Similar to a DialogueMenu, but logs multiple messages.  Used during battles.
 * @author Peter
 */
public class LogMenu {
    
    private static Mesh border;
    private static ScrollingText[] text;
    private static int offset;
    public static final int NUM_LINES = 4,
                            BORDER_W = 350, 
                            BORDER_H = Vars.UI_SCALE*(NUM_LINES-1);
    
    static {
        text = new ScrollingText[NUM_LINES];
        offset = 0;
        border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, BORDER_W, BORDER_H, Vars.UI_SCALE);
    }
    
    public static void addLine(String s) {
        for (String s1 : s.split("\n"))
            addOneLine(s1);
    }
    private static void addOneLine(String s) {
        if (text[0] != null) {
            text[0].destroy();
            offset += Vars.UI_SCALE;
        }
        
        // shift down the list
        for (int i = 1; i < NUM_LINES; i++) {
            text[i-1] = text[i];
        }
        
        //StringBuilder buildString = new StringBuilder();
        //for (int i = 0; i < log.length; i++)
        //    buildString.append(log[i]).append("\n");
        
        //FontUtils.useMetric("font_1");
        text[NUM_LINES-1] = new ScrollingText(s, "font_1", Vars.UI_SCALE/2, Vars.UI_SCALE/2, Vars.UI_SCALE, Vars.UI_SCALE, 2);
    }
    
    public static void render(ModelMatrix model) {
        TextureMap.bindUnfiltered("ui_scalablemenu_blur");
        border.render();
        
        model.translate(0, offset);
        for (int i = 0; i < NUM_LINES-1; i++)
            if (text[i] != null) {
                TextureMap.bindUnfiltered("font_1");
                model.makeCurrent();
                text[i].render();
                model.translate(0, Vars.UI_SCALE);
            }
        if (offset > 0) {
            offset -= 4;
            if (offset < 0)
                offset = 0;
        } else {
            model.makeCurrent();
            text[NUM_LINES-1].render();
        }
    }
    public static void clear() {
        for (ScrollingText t : text)
            if (t != null)
                t.destroy();
        text = new ScrollingText[NUM_LINES];
        offset = 0;
    }
}