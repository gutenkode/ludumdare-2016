package ui.components;

import mote4.util.matrix.ModelMatrix;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
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
    private Mesh border;//, text;
    private ScrollingText[] textList;
    private int cursorPos,
                borderW, borderH,
                renderBorderW, renderBorderH;
    private float cursorAnimation;
    
    public SelectionMenu(SelectionMenuBehavior b) {
        this.b = b;
        cursorPos = 0;
        renderBorderW = 0;
        renderBorderH = 0;
    }

    /**
     * Put the cursor on a specific index, useful for initializing a menu
     * on an index other than zero.  The index will be clamped
     * to the actual range of the menu.
     * @param i
     */
    public void setCursorPos(int i) {
        cursorPos = i;

        cursorPos %= this.b.getNumElements();

        if (cursorPos < 0)
            cursorPos = this.b.getNumElements()-1;

        this.b.onHighlight(cursorPos);
    }
    
    public int cursorPos() { return cursorPos; }
    public int width() { return renderBorderW; }
    public int height() { return renderBorderH; }
    
    public void onFocus() {
        b.onFocus();
        b.onHighlight(cursorPos);

        FontUtils.useMetric("font_1");
        float maxWidth = FontUtils.getStringWidth("["+b.getTitle()+"]");

        // static text initialization
        /*
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
        */
        // dynamic text initialization
        int speed = 1;
        if (textList == null || textList.length != b.getNumElements()+1) {
            if (textList != null)
                for (ScrollingText s : textList)
                    s.destroy();
            textList = new ScrollingText[b.getNumElements() + 1];
        }
        if (textList[0] == null || !textList[0].getFullStr().equals("["+b.getTitle()+"]")) {
            if (textList[0] != null)
                textList[0].destroy();
            textList[0] = new ScrollingText("["+b.getTitle()+"]", "font_1", Const.UI_SCALE/3, Const.UI_SCALE/4, Const.UI_SCALE, Const.UI_SCALE, speed);
        }
        for (int i = 0; i < b.getNumElements(); i++) {
            if (textList[i+1] == null || !textList[i+1].getFullStr().equals("   "+b.getElementName(i))) {
                if (textList[i+1] != null)
                    textList[i+1].destroy();
                textList[i+1] = new ScrollingText("   " + b.getElementName(i), "font_1", Const.UI_SCALE/3, Const.UI_SCALE/4 + Const.UI_SCALE * (i+1), Const.UI_SCALE, Const.UI_SCALE, speed);
            }
            float tempWidth = FontUtils.getStringWidth("   "+b.getElementName(i));
            maxWidth = Math.max(maxWidth, tempWidth);
        }

        borderW = (int)(Const.UI_SCALE*maxWidth)-Const.UI_SCALE;
        borderH = (b.getNumElements())*Const.UI_SCALE-Const.UI_SCALE/2;
        if (border != null)
            border.destroy();
        border = MenuMeshCreator.create(Const.UI_SCALE, Const.UI_SCALE, renderBorderW, renderBorderH, Const.UI_SCALE);
    }
    
    public void update() {
        redrawBorder();

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
    private void redrawBorder() {
        // the text box will expand out from size 0,0
        boolean redraw = false;
        if (renderBorderH > borderH) {
            renderBorderH -= (renderBorderH-borderH)/2;
            redraw = true;
        } else if (renderBorderH < borderH) {
            renderBorderH += (borderH-renderBorderH)/2;
            redraw = true;
        }
        if (renderBorderW > borderW) {
            renderBorderW -= (renderBorderW-borderW)/3;
            redraw = true;
        } else if (renderBorderW < borderW) {
            renderBorderW += (borderW-renderBorderW)/3;
            redraw = true;
        }
        if (redraw) {
            if (border != null)
                border.destroy();
            border = MenuMeshCreator.create(Const.UI_SCALE, Const.UI_SCALE, renderBorderW, renderBorderH, Const.UI_SCALE);
        }
    }
    
    public void render(ModelMatrix model) {
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        for (Mesh m : textList)
            m.render();
        //text.render();
        
        model.translate(-Const.UI_SCALE/3, Const.UI_SCALE/3.5f);
        model.translate(cursorAnimation*Const.UI_SCALE*.2f, (1+cursorPos)*Const.UI_SCALE);
        model.makeCurrent();
        TextureMap.bindUnfiltered("ui_cursor");
        cursor.render();
    }
    
    public void destroy() {
        border.destroy();
        for (ScrollingText s : textList)
            s.destroy();
        //text.destroy();
    }
}