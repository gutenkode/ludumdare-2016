package terminal.program;

import terminal.TerminalSession;

/**
 * A program receives/prints text from a terminal.  A Program does not need to
 * care about anything other than processing input and telling a TerminalSession
 * what to display.
 * @author Peter
 */
public interface Program {
    public String name();
    public void init(TerminalSession s);
    public void close();
    public void parse(String s);
}
