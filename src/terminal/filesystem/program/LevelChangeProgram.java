package terminal.filesystem.program;

import map.MapLevelManager;
import scenes.TerminalScene;
import terminal.TerminalSession;
import terminal.filesystem.Directory;

/**
 *
 * @author Peter
 */
public class LevelChangeProgram extends Program {

    private TerminalSession session;

    public LevelChangeProgram(Directory p) {
        super("level", p);
    }

    @Override
    public void init(TerminalSession s) {
        session = s;
        session.addLine("Loading level.exe...");
    }

    @Override
    public void close() {
        session.addLine("Closing level.exe...");
    }

    @Override
    public void parse(String s) {
        switch (s) {
            case "help":
                session.addLine("advance: move to next level");
                session.addLine("reset: go to level 1");
                session.addLine("exit: close level.exe");
                break;
            case "exit":
                session.closeProgram();
                break;
            case "advance":
                MapLevelManager.incrementCurrentLevel();
                TerminalScene.closeTerminalFromProgram();
                break;
            case "reset":
                MapLevelManager.setCurrentLevel(1);
                TerminalScene.closeTerminalFromProgram();
                break;
            default:
                session.addLine("Invalid command.");
        }
    }
    
}
