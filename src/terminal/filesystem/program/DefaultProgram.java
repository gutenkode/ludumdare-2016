package terminal.filesystem.program;

import terminal.TerminalSession;
import terminal.filesystem.Directory;
import terminal.filesystem.Node;
import terminal.filesystem.file.TerminalFile;

/**
 *
 * @author Peter
 */
public class DefaultProgram extends Program {
    
    private TerminalSession session;

    public DefaultProgram() {
        super("root", null);
    }
    
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
                session.addLine("ls          : Show directory contents.");
                session.addLine("cd [dir]    : Move to subdirectory [dir].");
                session.addLine("cd ..       : Move up to parent directory.");
                session.addLine("open [file] : Read contents of file [file].");
                session.addLine("run [prog]  : Run the program [prog].");
                session.addLine("pwd         : View current directory.");
                session.addLine("whoami      : View current login credentials.");
                session.addLine("clear       : Clear the screen.");
                break;
            case "ls":
            case "dir":
            case "list":
                Directory d = session.getCurrentDirectory();
                session.addLine("Directory '"+d.path()+"'");
                for (Node n : d.contents())
                    session.addLine(n.name());
                break;
            case "pwd":
                session.addLine(session.getCurrentDirectory().path());
                break;
            case "whoami":
                session.addLine("You are you.");
                break;
            case "clear":
                session.clear();
                break;
            case "cd ..":
                if (!session.moveToParentDirectory())
                    session.addLine("Directory has no parent.");
                else
                    session.addLine("Moved to directory '"+session.getCurrentDirectory().path()+"'");
                break;
            case "cd":
                session.addLine("Usage: cd [directory]\nExample: cd /secret_files");
                break;
            case "run":
                session.addLine("Usage: run [program]\nExample: run helloworld.exe");
                break;
            case "open":
                session.addLine("Usage: open [file]\nExample: open passwords.txt");
                break;
            default:
                if (s.startsWith("cd "))
                {
                    String dirName = s.substring(3);
                    if (!session.changeDirectory(dirName))
                        session.addLine("Directory not found: "+dirName);
                    else
                        session.addLine("Moved to directory '"+session.getCurrentDirectory().path()+"'");
                }
                else if (s.startsWith("run "))
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
                else if (s.startsWith("ls ")) session.addLine("Usage: ls");
                else if (s.startsWith("pwd ")) session.addLine("Usage: pwd");
                else if (s.startsWith("whoami ")) session.addLine("Usage: whoami");
                else if (s.startsWith("clear ")) session.addLine("Usage: clear");
                else
                    session.addLine("Invalid command.\nType 'help' for a list of commands.");
                break;
        }
    }
    private boolean runProgram(String progName) {
        for (Node n : session.getCurrentDirectory().contents())
            if (n instanceof Program) {
                Program p = (Program)n;
                if (p.name().equals(progName)) {
                    session.startProgram(p);
                    return true;
                }
            }
        return false;
    }
    private boolean openFile(String fileName) {
        for (Node n : session.getCurrentDirectory().contents())
            if (n instanceof TerminalFile) {
            TerminalFile f = (TerminalFile)n;
                if (f.name().equals(fileName)) {
                    session.addLine(f.contents());
                    return true;
                }
            }
        return false;
    }
    
}
