package ui.components;

import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.ModelMatrix;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import main.Vars;
import main.Input;
import org.lwjgl.opengl.GL11;
import ui.MenuMeshCreator;

/**
 * Displays a list of text options for the player to choose from, 
 * which are defined in a script.
 * @author Peter
 */
public class ScriptChoiceMenu {
    
    private static Mesh cursor;
    
    static {
        cursor = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN, 
                2, new float[] {0,0, 
                                0,16, 
                                24,16,
                                24,0}, 
                2, new float[] {0,0,
                                0,1, 
                                1,1, 
                                1,0}, 
                0, null, null);
    }
    
    private static int cursorPos, numElements;
    private static float cursorAnimation;
    private static Mesh border, text;
    private static int width, height;
    
    public static int width() { return width; }
    public static int height() { return height; }
    
    public static void setText(String[] s) {
        cursorPos = 0;
        numElements = s.length;
        if (border != null)
            border.destroy();
        if (text != null)
            text.destroy();
        
        float maxWidth = 0;
        
        FontUtils.useMetric("font_1");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            str.append("   ");
            str.append(s[i]);
            str.append("\n");
            
            float tempWidth = FontUtils.getStringWidth("   "+s[i]);
            maxWidth = Math.max(maxWidth, tempWidth);
        }
        text = FontUtils.createString(str.toString(), Vars.UI_SCALE/3, Vars.UI_SCALE/4, Vars.UI_SCALE, Vars.UI_SCALE);
        
        width = (int)(Vars.UI_SCALE*maxWidth)- Vars.UI_SCALE;
        height = (s.length-1)* Vars.UI_SCALE- Vars.UI_SCALE/2;
        border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, width, height, Vars.UI_SCALE);
    }
    
    /**
     * Allows the player to use this menu and select their choice.
     * @return The index of the player's selection, -1 otherwise.
     */
    public static int update() {
        cursorAnimation += .05f;
        cursorAnimation %= 1;
        
        if (Input.isKeyNew(Input.Keys.DOWN)) {
            cursorPos++;
            cursorPos %= numElements;
            AudioPlayback.playSfx("sfx_menu_hover");
        } else if (Input.isKeyNew(Input.Keys.UP)) {
            cursorPos--;
            if (cursorPos < 0)
                cursorPos = numElements-1;
            AudioPlayback.playSfx("sfx_menu_hover");
        } else if (Input.isKeyNew(Input.Keys.YES)) {
            AudioPlayback.playSfx("sfx_menu_select");
            return cursorPos;
        }
        return -1;
    }
    
    public static void render(ModelMatrix model) {
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        text.render();
        
        model.translate(-Vars.UI_SCALE/3, Vars.UI_SCALE/3.5f);
        model.translate(cursorAnimation* Vars.UI_SCALE*.2f, cursorPos* Vars.UI_SCALE);
        model.makeCurrent();
        TextureMap.bindUnfiltered("ui_cursor");
        cursor.render();
    }
}
