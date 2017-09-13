package ui.components;

import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.MeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import main.Vars;
import rpgbattle.PlayerSkills;
import rpgsystem.Skill;
import rpgsystem.SkillModifier;
import ui.MenuMeshCreator;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * Displays the currently applied skills in a graphical format.
 * Created by Peter on 5/31/17.
 */
public class SkillCostMenu {
    private static Mesh border, text, bars;
    private static ArrayList<Mesh> bartext = new ArrayList<>();
    private static int width, height,
            renderWidth = 0, renderHeight = 0;
    private static boolean redraw = true;
    private static int scaleH = 3, scaleW = 5;

    public static int width() { return width; }
    public static int height() { return height; }

    public static void setRedraw() { redraw = true; }

    private static void redraw() {
        redraw = false;
        if (text != null)
            text.destroy();
        createBarMesh();

        String s = PlayerSkills.currentCapacity()+"/"+PlayerSkills.maxCapacity();

        FontUtils.useMetric("font_1");
        //text = FontUtils.createString(s, Const.UI_SCALE/2, Const.UI_SCALE/2, Const.UI_SCALE, Const.UI_SCALE);
        text = new ScrollingText(s, "font_1", Vars.UI_SCALE/2, 1+PlayerSkills.maxCapacity()*scaleH, Vars.UI_SCALE, Vars.UI_SCALE, 60*3);

        String[] lines = s.split("\n");
        float maxWidth = 0;
        for (String s1 : lines)
            maxWidth = Math.max(maxWidth, FontUtils.getStringWidth(s1));

        height = PlayerSkills.maxCapacity()*scaleH -Vars.UI_SCALE+6;//(lines.length-1)* Vars.UI_SCALE;
        width = Vars.UI_SCALE*scaleW -2*Vars.UI_SCALE+6;//(int)(Vars.UI_SCALE*maxWidth)- Vars.UI_SCALE;
        //border = MenuMeshCreator.create(Vars.UI_SCALE, Vars.UI_SCALE, renderWidth, renderHeight, Vars.UI_SCALE);
    }
    private static void createBarMesh() {
        MeshBuilder builder = new MeshBuilder(2);
        builder.includeColors(3);
        FontUtils.useMetric("6px");
        for (Mesh m : bartext)
            m.destroy();
        bartext.clear();

        int w = Vars.UI_SCALE*scaleW;

        // the background bar
        int max = PlayerSkills.maxCapacity()*scaleH;
        builder.vertices(
                0,0, w,0, 0,max,
                  w,0, 0,max, w,max);
        float c = .15f;
        builder.colors(c,c,c, c,c,c, c,c,c, c,c,c, c,c,c, c,c,c);

        int pos = 0;
        for (Skill s : PlayerSkills.getEquippedSkills()) {
            addBar(builder, pos, s.data.equipCost(), max, w, 0, s.data.name.toUpperCase(), s.data.element.color);
            pos += s.data.equipCost();
            SkillModifier m = PlayerSkills.getLinkedModifier(s);
            if (m != null) {
                addBar(builder, pos, m.cost, max, w, 4, m.name.toUpperCase(), new float[] {1,1,1});
                pos += m.cost;
            }
        }

        if (bars != null)
            bars.destroy();
        bars = builder.constructVAO(GL_TRIANGLES);
    }
    private static void addBar(MeshBuilder builder, int pos, int cost, int max, int w, int offset, String name, float[] barColor) {
        // full bar
        builder.vertices(
                offset,pos* scaleH, w,pos* scaleH, offset,(pos+cost)* scaleH,
                w,pos* scaleH, offset,(pos+cost)* scaleH, w,(pos+cost)* scaleH);
        float c = .3f+cost/150f;
        builder.colors(c,c,c, c,c,c, c,c,c, c,c,c, c,c,c, c,c,c);

        // top bar
        builder.vertices(
                offset,pos* scaleH, w,pos* scaleH, offset,(pos)* scaleH +1,
                w,pos* scaleH, offset,(pos)* scaleH +1, w,(pos)* scaleH +1);
        c = .8f;
        float[] buff = new float[18];
        for (int i = 0; i < buff.length; i++)
            buff[i] = barColor[i%3];
        builder.colors(buff);

        // add text
        bartext.add(FontUtils.createString(name, 1+offset, 2+pos* scaleH, 16, 16));
    }


    public static void render() {
        if (redraw)
            redraw();

        redrawBorder();

        TextureMap.bindUnfiltered("ui_scalablemenu");
        border.render();
        TextureMap.bindUnfiltered("font_1");
        text.render();
    }
    public static void renderBars() {
        bars.render();
    }
    public static void renderBarText() {
        TextureMap.bindUnfiltered("font_6px");
        for (Mesh m : bartext)
            m.render();
    }
    private static void redrawBorder() {
        // the text box will expand out from size 0,0
        boolean redraw = false;
        if (border == null)
            redraw = true;
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
