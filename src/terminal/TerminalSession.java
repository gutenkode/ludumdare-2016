package terminal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import mote4.util.matrix.ModelMatrix;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.ScrollingText;
import nullset.Const;
import terminal.program.DefaultProgram;
import terminal.program.Program;

/**
 * A "login session" for a terminal, created when the player uses a terminal entity.
 * Manages text and active programs.
 * @author Peter
 */
public class TerminalSession {
    
    public static final int MAX_LINES = 20, WRITE_SPEED = 5, WIDTH = 9, HEIGHT = 14;
    private static final String METRIC = "monospace";
    
    private ArrayList<ScrollingText> writtenLines;
    private Queue<String> bufferedLines;
    private Mesh writeLine;
    
    private Program rootProgram, activeProgram;
    
    public TerminalSession() {
        writtenLines = new ArrayList<>();
        bufferedLines = new LinkedList<>();
        
        FontUtils.useMetric(METRIC);
        writeLine = FontUtils.createString("", 0,0, WIDTH,HEIGHT);
        
        rootProgram = new DefaultProgram();
        rootProgram.init(this);
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
    public void addCompleteLine(String s) {
            if (s.contains("\n")) 
            {
                // split multi-line strings
                String[] arr = s.split("\n");
                for (String s1 : arr) {
                    ScrollingText t = new ScrollingText(s1,METRIC, 0,0, WIDTH,HEIGHT, WRITE_SPEED);
                    t.complete();
                    writtenLines.add(t);
                }
            } else {
                //FontUtils.setCharPixelWidth(9);
                ScrollingText t = new ScrollingText(s,METRIC, 0,0, WIDTH,HEIGHT, WRITE_SPEED);
                t.complete();
                //FontUtils.setCharPixelWidth(16);
                writtenLines.add(t);
            }
            // while instead of if - this call can add multiple lines at once
            while (writtenLines.size() > MAX_LINES)
                writtenLines.remove(0);
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
    
    public void render(ModelMatrix model) {
        if ((writtenLines.isEmpty() || writtenLines.get(writtenLines.size()-1).isDone()) && !bufferedLines.isEmpty()) {
            String s = bufferedLines.remove();
            writtenLines.add(new ScrollingText(s,METRIC, 0,0, WIDTH,HEIGHT, WRITE_SPEED));
            if (writtenLines.size() > MAX_LINES) {
                writtenLines.get(0).destroy();
                writtenLines.remove(0);
            }
        }

        //FontUtils.setCharPixelWidth(9);
        for (ScrollingText t : writtenLines) {
            t.render();
            model.translate(0, Const.UI_SCALE);
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
