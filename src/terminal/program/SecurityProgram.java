package terminal.program;

import map.MapManager;
import terminal.TerminalSession;

/**
 *
 * @author Peter
 */
public class SecurityProgram implements Program {

    @Override
    public String name() { return "secr"; }

    @Override
    public void init(TerminalSession s) {
        s.addLine("Security alert deactivated.\nHave a nice day.");
        MapManager.getTimelineState().triggerAlert(false);
        s.closeProgram();
    }

    @Override
    public void close() {}

    @Override
    public void parse(String s) {}
    
}
