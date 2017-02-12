package terminal.filesystem.file;

import terminal.filesystem.Directory;
import terminal.filesystem.Node;

/**
 * Created by Peter on 1/24/17.
 */
public class TextFile extends TerminalFile {

    private String contents;
    public TextFile(String n, String c, Directory p) {
        super(n,p);
        contents = c;
    }

    @Override
    public String name() {
        return name+".txt";
    }

    @Override
    public String contents() {
        return contents;
    }
}
