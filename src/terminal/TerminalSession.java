package terminal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import mote4.util.matrix.ModelMatrix;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import main.Vars;
import terminal.filesystem.Directory;
import terminal.filesystem.Node;
import terminal.filesystem.program.DefaultProgram;
import terminal.filesystem.program.Program;

/**
 * A "login session" for a terminal, created when the player uses a terminal entity.
 * Manages text and active programs.
 * @author Peter
 */
public class TerminalSession {
    
    public static final int MAX_LINES = 20, WRITE_SPEED = 5, WIDTH = 9, HEIGHT = 14;
    private static final String METRIC = "monospace";
    
    private ArrayList<ScrollingText> writtenLines;
    private ArrayList<Boolean> inverted;
    private Queue<String> bufferedLines;
    private Mesh writeLine;
    
    private Program rootProgram, activeProgram;
    private Directory currentDirectory;
    
    public TerminalSession(Directory d) {
        writtenLines = new ArrayList<>();
        inverted = new ArrayList<>();
        bufferedLines = new LinkedList<>();
        
        FontUtils.useMetric(METRIC);
        writeLine = FontUtils.createString("", 0,0, WIDTH,HEIGHT);
        
        rootProgram = new DefaultProgram();
        rootProgram.init(this);
        currentDirectory = d;
    }
    
    /**
     * The contents of the line the player is currently writing.
     * @param s 
     */
    public void setWriteLine(String s) {
        FontUtils.useMetric(METRIC);
        writeLine.destroy();
        //FontUtils.setCharPixelWidth(9);
        writeLine = FontUtils.createString(s, 0,0, WIDTH,HEIGHT);
        //FontUtils.setCharPixelWidth(16);
    }
    /**
     * Append a line to the written text instantly, with no write-in delay.
     * Passes the queue for lines to write completely.
     * @param s 
     */
    public void addCompleteLine(String s, boolean inv) {
            if (s.contains("\n")) 
            {
                // split multi-line strings
                String[] arr = s.split("\n");
                for (String s1 : arr) {
                    ScrollingText t = new ScrollingText(s1,METRIC, 0,0, WIDTH,HEIGHT, WRITE_SPEED);
                    t.complete();
                    writtenLines.add(t);
                    inverted.add(inv);
                }
            } else {
                //FontUtils.setCharPixelWidth(9);
                ScrollingText t = new ScrollingText(s,METRIC, 0,0, WIDTH,HEIGHT, WRITE_SPEED);
                t.complete();
                //FontUtils.setCharPixelWidth(16);
                writtenLines.add(t);
                inverted.add(inv);
            }
            // while instead of if - this call can add multiple lines at once
            while (writtenLines.size() > MAX_LINES) {
                writtenLines.remove(0);
                inverted.remove(0);
            }
    }
    /**
     * Add a line to the queue for writing to the console.
     * Lines are added after the previous line is finished writing out.
     * @param s 
     */
    public void addLine(String s) {
        if (s.contains("\n")) 
        {
            // split multi-line strings
            String[] arr = s.split("\n");
            for (String s1 : arr)
                bufferedLines.offer(s1);
        }
        else
            bufferedLines.offer(s);
    }
    /**
     * Clear the contents of this terminal.
     */
    public void clear() {
        for (ScrollingText t : writtenLines)
            t.destroy();
        writtenLines.clear();
        inverted.clear();
    }
    
    /**
     * Whether this session is currently displaying the input bar.
     * Input can still be set but it will not be displayed until it is active again.
     * It is recommended to not give input while input is not active.
     * @return 
     */
    public boolean inputActive() { return bufferedLines.isEmpty(); }
    
    public void startProgram(Program p) {
        activeProgram = p;
        p.init(this);
    }
    public void closeProgram() {
        activeProgram.close();
        activeProgram = null;
    }

    public Directory getCurrentDirectory() { return currentDirectory; }
    public boolean changeDirectory(String s) {
        for (Node n : currentDirectory.contents())
            if (n.name().equals(s))
                if (n instanceof Directory) {
                    currentDirectory = (Directory)n;
                    return true;
                } else {
                    return false;
                }
        return false;
    }
    public boolean moveToParentDirectory() {
        Directory d = currentDirectory.parent();
        if (d != null) {
            currentDirectory = d;
            return true;
        }
        return false;
    }
    
    public void render(ModelMatrix model) {
        if ((writtenLines.isEmpty() || writtenLines.get(writtenLines.size()-1).isDone()) && !bufferedLines.isEmpty()) {
            String s = bufferedLines.remove();
            writtenLines.add(new ScrollingText(s,METRIC, 0,0, WIDTH,HEIGHT, WRITE_SPEED));
            inverted.add(false);
            if (writtenLines.size() > MAX_LINES) {
                writtenLines.get(0).destroy();
                writtenLines.remove(0);
                inverted.remove(0);
            }
        }

        //FontUtils.setCharPixelWidth(9);
        TextureMap.bindUnfiltered("font_terminal");
        for (int i = 0; i < writtenLines.size(); i++) {
        //for (ScrollingText t : writtenLines) {
            ScrollingText t = writtenLines.get(i);
            if (inverted.get(i)) {
                TextureMap.bindUnfiltered("font_terminal_inv");
                t.render();
                TextureMap.bindUnfiltered("font_terminal");
            } else
                t.render();
            model.translate(0, Vars.UI_SCALE);
            model.makeCurrent();
        }
        //FontUtils.setCharPixelWidth(16);
        if (bufferedLines.isEmpty())
            writeLine.render();
    }
    public void parse(String s) {
        if (activeProgram != null)
            activeProgram.parse(s.trim());
        else
            rootProgram.parse(s.trim());
    }
    public void destroy() {
        for (ScrollingText t : writtenLines)
            t.destroy();
        writeLine.destroy();
    }
}
