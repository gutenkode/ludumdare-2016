package terminal.filesystem.file;

import terminal.filesystem.Directory;
import terminal.filesystem.Node;

/**
 * Created by Peter on 1/24/17.
 */
public abstract class TerminalFile extends Node {

    public TerminalFile(String n, Directory p) {
        super(n,p);
    }

    public abstract String contents();
}
