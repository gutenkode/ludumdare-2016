package terminal.filesystem;

import terminal.filesystem.file.TextFile;
import terminal.filesystem.program.EditProgram;
import terminal.filesystem.program.LevelChangeProgram;
import terminal.filesystem.program.SecurityProgram;
import terminal.filesystem.program.VillageProgram;

import java.util.ArrayList;

/**
 * Created by Peter on 2/11/17.
 */
public class DefaultFilesystem {
    public static Directory getDefaultFilesystem() {
        Directory d = new Directory("ROOT",new ArrayList<>(), null);
        d.addChild(new SecurityProgram(null));
        d.addChild(new TextFile("notes", "Hello, world!\nLine two.", null));
        Directory d2 = new Directory("secret",new ArrayList<>(),null);
        d.addChild(d2);
        d2.addChild(new TextFile("password", "Security override password: asdf", null));
        d2.addChild(new EditProgram(null));
        d2.addChild(new VillageProgram(null));
        d2.addChild(new LevelChangeProgram(null));
        d2.addChild(new UserDirectory(null));

        return d;
    }
}
