package terminal.filesystem.program;

import terminal.TerminalSession;
import terminal.filesystem.Directory;
import terminal.filesystem.Node;


/**
 * A program receives/prints text from a terminal.  A Program does not need to
 * care about anything other than processing input and telling a TerminalSession
 * what to display.
 * @author Peter
 */
public abstract class Program extends Node {

    public Program(String n, Directory p) {
        super(n, p);
    }

    @Override
    public String name() {
        return name+".exe";
    }
    public abstract void init(TerminalSession s);
    public abstract void close();
    public abstract void parse(String s);
}
