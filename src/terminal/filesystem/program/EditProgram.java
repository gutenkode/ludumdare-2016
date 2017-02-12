package terminal.filesystem.program;

import map.MapLoader;
import scenes.Editor;
import terminal.TerminalSession;
import terminal.filesystem.Directory;

import java.util.StringTokenizer;

/**
 * Created by Peter on 1/24/17.
 */
public class EditProgram extends Program {

    private TerminalSession session;

    public EditProgram(Directory p) {
        super("edit", p);
    }

    @Override
    public void init(TerminalSession s) {
        session = s;
        session.addLine("Editor console v0.1");
    }

    @Override
    public void close() {}


    @Override
    public void parse(String s) {
        switch (s) {
            case "help":
                session.addLine("new [name] x,y - Load new map with [name] and dimensions x,y.");
                break;
            case "new":
                session.addLine("Usage: new [name] x,y");
                break;
            default:
                if (s.startsWith("new ")) {
                    StringTokenizer tok = new StringTokenizer(s);
                    if (tok.countTokens() != 3)
                        session.addLine("Usage: new [name] x,y");
                    else {
                        tok.nextToken();
                        String name = tok.nextToken();
                        String dims = tok.nextToken();
                        if (dims.indexOf(',') == -1)
                            session.addLine("Usage: new [name] x,y");
                        else {
                            String[] xy = dims.split(",");
                            if (xy.length != 2)
                                session.addLine("Usage: new [name] x,y");
                            else {
                                try {
                                    int x = Integer.parseInt(xy[0]);
                                    int y = Integer.parseInt(xy[1]);

                                    MapLoader.makeEmptyMap(name,x,y);
                                    Editor.loadMap(name);
                                    session.addLine("Map created.");
                                } catch (NumberFormatException e) {
                                    session.addLine("Usage: new [name] x,y");
                                }
                            }
                        }
                    }

                } else
                    session.addLine("Invalid command.\nType 'help' for a list of commands.");
                break;
        }
    }
}
