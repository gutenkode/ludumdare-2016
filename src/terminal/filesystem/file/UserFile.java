package terminal.filesystem.file;

import terminal.filesystem.Directory;

import java.io.File;

/**
 * Created by Peter on 2/11/17.
 */
public class UserFile extends TerminalFile {

    private File file;

    public UserFile(File f, Directory p) {
        super(f.getName(),p);
    }

    @Override
    public String contents() {
        return "Unrecognized filetype: cannot open.";
    }
}
