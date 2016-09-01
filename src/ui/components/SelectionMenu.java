package ui.components;

import mote4.util.matrix.ModelMatrix;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import nullset.Const;
import nullset.Input;
import org.lwjgl.opengl.GL11;
import ui.MenuMeshCreator;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * A SelectionMenu is the core component of the player's UI.  It displays a list
 * of options the player can choose from.  This can be used for pause menus and
 * item selection menus.
 * @author Peter
 */
public class SelectionMenu {
    
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
    
    private SelectionMenuBehavior b;
    private Mesh border, text;
    private int cursorPos,
                borderW, borderH;
    private float cursorAnimation;
    
    public SelectionMenu(SelectionMenuBehavior b) {
        this.b = b;
        cursorPos = 0;
    }
    
    public int cursorPos() { return cursorPos; }
    public int width() { return borderW; }
    public int height() { return borderH; }
    
    public void onFocus() {
        b.onFocus();
        b.onHighlight(cursorPos);
        
        float maxWidth = FontUtils.getStringWidth("["+b.getTitle()+"]");
        
        FontUtils.useMetric("font_1");
        StringBuilder str = new StringBuilder();
        str.append("[");
        str.append(b.getTitle());
        str.append("]");
        for (int i = 0; i < b.getNumElements(); i++) {
            str.append("\n   ");
            str.append(b.getElementName(i));
            
            float tempWidth = FontUtils.getStringWidth("   "+b.getElementName(i));
            maxWidth = Math.max(maxWidth, tempWidth);
        }
        text = FontUtils.createString(str.toString(), Const.UI_SCALE/3, Const.UI_SCALE/4, Const.UI_SCALE, Const.UI_SCALE);
        
        borderW = (int)(Const.UI_SCALE*maxWidth)-Const.UI_SCALE;
        borderH = (b.getNumElements())*Const.UI_SCALE-Const.UI_SCALE/2;
        border = MenuMeshCreator.create(Const.UI_SCALE, Const.UI_SCALE, borderW, borderH, Const.UI_SCALE);
    }
    
    public void update() {
        cursorAnimation += .05f;
        cursorAnimation %= 1;
        
        if (Input.isKeyNew(Input.Keys.DOWN)) {
            cursorPos++;
            cursorPos %= this.b.getNumElements();
            this.b.onHighlight(cursorPos);
        } else if (Input.isKeyNew(Input.Keys.UP)) {
            cursorPos--;
            if (cursorPos < 0)
                cursorPos = this.b.getNumElements()-1;
            this.b.onHighlight(cursorPos);
        } else if (Input.isKeyNew(Input.Keys.YES)) {
            this.b.onAction(cursorPos);
        } else if (Input.isKeyNew(Input.Keys.NO)) {
            this.b.onClose();
        }
    }
    
    public void render(ModelMatrix model) {
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        text.render();
        
        model.translate(-Const.UI_SCALE/3, Const.UI_SCALE/3.5f);
        model.translate(cursorAnimation*Const.UI_SCALE*.2f, (1+cursorPos)*Const.UI_SCALE);
        model.makeCurrent();
        TextureMap.bindUnfiltered("ui_cursor");
        cursor.render();
    }
    
    public void destroy() {
        border.destroy();
        text.destroy();
    }
}