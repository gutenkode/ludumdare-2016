package terminal;

import terminal.file.TerminalFile;
import terminal.file.TextFile;

import java.util.ArrayList;

/**
 *
 * @author Peter
 */
public class CurrentFiles {
    
    private static ArrayList<TerminalFile> files;
    
    static {
        files = new ArrayList<>();
        files.add(new TextFile("asdf", "Hello, world!\nLine two."));
    }
    
    public static void add(TerminalFile f) { files.add(f); }
    public static ArrayList<TerminalFile> getList() { return files; }
}
