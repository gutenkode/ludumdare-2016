package ui.components.selectionMenu;

import mote4.util.matrix.ModelMatrix;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import main.Vars;
import ui.MenuMeshCreator;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Contains multiple behaviors, arranged as tabs in one menu pane.
 * Useful for menus with separate categories, like types of items.
 * Created by Peter on 6/30/17.
 */
public class TabbedSelectionMenu implements SelectionMenu {

    private SelectionMenuBehavior[] b;
    private Mesh border;
    private ScrollingText[] textList;
    private int cursorPos, currentTab,
            borderW, borderH,
            renderBorderW, renderBorderH;
    private float cursorAnimation;

    public TabbedSelectionMenu(SelectionMenuBehavior... b) {
        this.b = b;
        cursorPos = 0;
        currentTab = 0;
        renderBorderW = 0;
        renderBorderH = 0;
    }

    @Override
    public void setCursorPos(int i) {
        cursorPos = i;

        cursorPos %= this.b[currentTab].getNumElements();

        if (cursorPos < 0)
            cursorPos = this.b[currentTab].getNumElements()-1;

        this.b[currentTab].onHighlight(cursorPos);
    }
    @Override
    public int cursorPos() { return cursorPos; }
    @Override
    public int width() { return renderBorderW; }
    @Override
    public int height() { return renderBorderH; }

    @Override
    public void onFocus() {
        b[currentTab].onFocus();
        b[currentTab].onHighlight(cursorPos);

        FontUtils.useMetric("font_1");
        float maxWidth = FontUtils.getStringWidth("["+b[currentTab].getTitle()+"]");

        // dynamic text initialization
        int speed = 1;
        if (textList == null || textList.length != b[currentTab].getNumElements()+1) {
            if (textList != null)
                for (ScrollingText s : textList)
                    s.destroy();
            textList = new ScrollingText[b[currentTab].getNumElements() + 1];
        }
        if (textList[0] == null || !textList[0].getFullStr().equals("["+b[currentTab].getTitle()+"]")) {
            if (textList[0] != null)
                textList[0].destroy();
            textList[0] = new ScrollingText("["+b[currentTab].getTitle()+"]", "font_1", Vars.UI_SCALE/3, Vars.UI_SCALE/4, Vars.UI_SCALE, Vars.UI_SCALE, speed);
        }
        for (int i = 0; i < b[currentTab].getNumElements(); i++) {
            if (textList[i+1] == null || !textList[i+1].getFullStr().equals("   "+b[currentTab].getElementName(i))) {
                if (textList[i+1] != null)
                    textList[i+1].destroy();
                textList[i+1] = new ScrollingText("   " + b[currentTab].getElementName(i), "font_1", Vars.UI_SCALE/3, Vars.UI_SCALE/4 + Vars.UI_SCALE * (i+1), Vars.UI_SCALE, Vars.UI_SCALE, speed);
            }
            float tempWidth = FontUtils.getStringWidth("   "+b[currentTab].getElementName(i));
            maxWidth = Math.max(maxWidth, tempWidth);
        }

        borderW = (int)(Vars.UI_SCALE*maxWidth)- Vars.UI_SCALE;
        borderH = (b[currentTab].getNumElements())* Vars.UI_SCALE- Vars.UI_SCALE/2;
        if (border != null)
            border.destroy();
        border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, renderBorderW, renderBorderH, Vars.UI_SCALE);
    }

    @Override
    public void update() {

    }

    @Override
    public void render(ModelMatrix model) {

    }

    @Override
    public void destroy() {
        for (SelectionMenuBehavior b : b)
            b.onCloseCleanup();
        border.destroy();
        for (ScrollingText s : textList)
            s.destroy();
    }
}
