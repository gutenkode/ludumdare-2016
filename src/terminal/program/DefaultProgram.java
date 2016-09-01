package terminal.program;

import terminal.CurrentPrograms;
import terminal.TerminalSession;

/**
 *
 * @author Peter
 */
public class DefaultProgram implements Program {
    
    private TerminalSession session;

    @Override
    public String name() { return "root"; }
    
    @Override
    public void init(TerminalSession s) {
        session = s;
        session.addLine("Creating terminal session...");
    }
    
    @Override
    public void close() {}
    
    @Override
    public void parse(String s) {
        switch (s) {
            case "help":
                session.addLine("Available commands:");
                session.addLine("list - Show directory contents.");
                //session.addLine("move <dir> - Move to subdirectory \"dir\".");
                //session.addLine("move .. - Move to parent directory.");
                session.addLine("open <file> - Read contents of file \"file\".");
                session.addLine("run <prog> - Run the program \"prog\".");
                //session.addLine("help - Display this text.");
                session.addLine("clear - Clear the terminal screen.");
                session.addLine("exit - Close terminal session.");
                break;
            case "list":
                session.addLine("Directory contents:");
                for (Program p : CurrentPrograms.getList())
                    session.addLine(p.name()+".exe");
                break;
            case "clear":
                session.clear();
                break;
            case "exit":
                session.addLine("Error: could not terminate session.");
                break;
            default:
                if (s.startsWith("run "))
                {
                    String progName = s.substring(4);
                    if (!runProgram(progName))
                        session.addLine("Program not found: "+progName);
                }
                else if (s.startsWith("open "))
                {
                    String fileName = s.substring(5);
                    session.addLine("File not found: "+fileName);
                }
                else
                    session.addLine("Invalid command.\nType 'help' for a list of commands.");
                break;
        }
    }
    private boolean runProgram(String progName) {
        for (Program p : CurrentPrograms.getList())
            if ((p.name()+".exe").equals(progName)) {
                session.startProgram(p);
                return true;
            }
        return false;
    }
    
}
