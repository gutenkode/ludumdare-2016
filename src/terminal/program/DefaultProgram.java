package terminal.program;

import terminal.CurrentFiles;
import terminal.CurrentPrograms;
import terminal.TerminalSession;
import terminal.file.TerminalFile;

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
                session.addLine("ls - Show directory contents.");
                //session.addLine("move <dir> - Move to subdirectory \"dir\".");
                //session.addLine("move .. - Move to parent directory.");
                session.addLine("open [file] - Read contents of file [file].");
                session.addLine("run [prog] - Run the program [prog].");
                //session.addLine("help - Display this text.");
                session.addLine("clear - Clear the screen.");
                break;
            case "ls":
            case "dir":
            case "list":
                for (TerminalFile f : CurrentFiles.getList())
                    session.addLine(f.name());
                for (Program p : CurrentPrograms.getList())
                    session.addLine(p.name()+".exe");
                break;
            case "pwd":
                session.addLine("/");
                break;
            case "whoami":
                session.addLine("You are you.");
                break;
            case "clear":
                session.clear();
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
                    if (!openFile(fileName))
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
    private boolean openFile(String fileName) {
        for (TerminalFile f : CurrentFiles.getList())
            if (f.name().equals(fileName)) {
                session.addLine(f.contents());
                return true;
            }
        return false;
    }
    
}
