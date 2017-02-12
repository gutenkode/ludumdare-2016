package terminal.filesystem;

import mote4.util.FileIO;
import terminal.filesystem.file.TextFile;
import terminal.filesystem.file.UserFile;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Peter on 2/11/17.
 */
public class UserDirectory extends Directory {

    private File file;
    private boolean contentsLoaded = false;

    public UserDirectory(Directory p) {
        super("local_filesys", null, p);
        file = new File(System.getProperty("user.home"));
    }
    public UserDirectory(File f, Directory p) {
        super(f.getName(),null, p);
        file = f;
    }

    public ArrayList<Node> contents() {
        if (!contentsLoaded) {
            children = new ArrayList<>();
            for (File c : file.listFiles())
                addNode(c);
            contentsLoaded = true;
        }
        return children;
    }
    private void addNode(File f) {
        if (f.getName().startsWith("."))
            return; // ignore hidden files
        if (f.isDirectory()) {
            children.add(new UserDirectory(f, this));
        } else if (f.getName().endsWith(".txt")) {
            String name = f.getName();
            name = name.substring(0, name.length()-4);
            String contents = "TODO: actually read in files."; // TODO actually read in files
            children.add(new TextFile(name, contents, this));
        } else {
            children.add(new UserFile(f, this));
        }
    }
}
