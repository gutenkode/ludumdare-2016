package ui.components.selectionMenu;

import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.ModelMatrix;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import main.Vars;
import main.Input;
import org.lwjgl.opengl.GL11;
import ui.MenuMeshCreator;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * A SingleSelectionMenu contains one pane of options.
 * @author Peter
 */
public class SingleSelectionMenu implements SelectionMenu {

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

    public SingleSelectionMenu(SelectionMenuBehavior b) {
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

    @Override
    public int cursorPos() { return cursorPos; }
    @Override
    public int width() { return renderBorderW; }
    @Override
    public int height() { return renderBorderH; }

    @Override
    public void onFocus() {
        b.onFocus();
        b.onHighlight(cursorPos);

        FontUtils.useMetric("font_1");
        float maxWidth = FontUtils.getStringWidth("["+b.getTitle()+"]");

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
            textList[0] = new ScrollingText("["+b.getTitle()+"]", "font_1", Vars.UI_SCALE/3, Vars.UI_SCALE/4, Vars.UI_SCALE, Vars.UI_SCALE, speed);
        }
        for (int i = 0; i < b.getNumElements(); i++) {
            if (textList[i+1] == null || !textList[i+1].getFullStr().equals("   "+b.getElementName(i))) {
                if (textList[i+1] != null)
                    textList[i+1].destroy();
                textList[i+1] = new ScrollingText("   " + b.getElementName(i), "font_1", Vars.UI_SCALE/3, Vars.UI_SCALE/4 + Vars.UI_SCALE * (i+1), Vars.UI_SCALE, Vars.UI_SCALE, speed);
            }
            float tempWidth = FontUtils.getStringWidth("   "+b.getElementName(i));
            maxWidth = Math.max(maxWidth, tempWidth);
        }

        borderW = (int)(Vars.UI_SCALE*maxWidth)- Vars.UI_SCALE;
        borderH = (b.getNumElements())* Vars.UI_SCALE- Vars.UI_SCALE/2;
        if (border != null)
            border.destroy();
        border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, renderBorderW, renderBorderH, Vars.UI_SCALE);
    }

    @Override
    public void update() {
        redrawBorder();

        cursorAnimation += .05f;
        cursorAnimation %= 1;

        if (Input.isKeyNew(Input.Keys.DOWN)) {
            cursorPos++;
            cursorPos %= this.b.getNumElements();
            this.b.onHighlight(cursorPos);
            AudioPlayback.playSfx("sfx_menu_hover");
        } else if (Input.isKeyNew(Input.Keys.UP)) {
            cursorPos--;
            if (cursorPos < 0)
                cursorPos = this.b.getNumElements()-1;
            this.b.onHighlight(cursorPos);
            AudioPlayback.playSfx("sfx_menu_hover");
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
            border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, renderBorderW, renderBorderH, Vars.UI_SCALE);
        }
    }

    @Override
    public void render(ModelMatrix model) {
        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        for (Mesh m : textList)
            m.render();
        //text.render3d();

        model.translate(-Vars.UI_SCALE/3, Vars.UI_SCALE/3.5f);
        model.translate(cursorAnimation* Vars.UI_SCALE*.2f, (1+cursorPos)* Vars.UI_SCALE);
        model.makeCurrent();
        TextureMap.bindUnfiltered("ui_cursor");
        cursor.render();
    }

    @Override
    public void destroy() {
        b.onCloseCleanup();
        border.destroy();
        for (ScrollingText s : textList)
            s.destroy();
        //text.destroy();
    }
}