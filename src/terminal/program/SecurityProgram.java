package terminal.program;

import map.MapManager;
import terminal.TerminalSession;

/**
 *
 * @author Peter
 */
public class SecurityProgram implements Program {

    private TerminalSession session;

    @Override
    public String name() { return "secr"; }

    @Override
    public void init(TerminalSession s) {
        if (!MapManager.getTimelineState().isAlertTriggered()) {
            s.addLine("Security alert is not active.\nClosing...");
            s.closeProgram();
        } else
            s.addLine("Running security program...\nType password now.");
        session = s;
    }

    @Override
    public void close() {}

    @Override
    public void parse(String s) {
        switch (s) {
            case "asdf":
                if (MapManager.getTimelineState().getVar("aiTriggered").equals("true") &&
                    !MapManager.getTimelineState().getVar("aiConvo").equals("true"))
                {
                    MapManager.getTimelineState().setVar("aiConvo","true");

                    session.addLine("So you're the one that's been poking around.");
                    session.addLine("I don't know what they've told you,\nbut I wouldn't listen too closely.");
                    session.addLine("I can assure you that your best interests are\nnot at heart. Please don't\nforget about this little chat we had.\nBe seeing you.");

                    MapManager.getTimelineState().triggerAlert(false);
                    session.closeProgram();
                } else {
                    session.addLine("Security alert deactivated.\nHave a nice day.");
                    MapManager.getTimelineState().triggerAlert(false);
                    session.closeProgram();
                }
                break;
            case "exit":
            case "quit":
                session.addLine("Closing security program...");
                session.closeProgram();
                break;
            default:
                session.addLine("Invalid password or command.\nType the password to disable security,\nor 'exit' to close the program.");
        }
    }
    
}
