package terminal.filesystem.program;

import map.MapManager;
import terminal.TerminalSession;
import terminal.filesystem.Directory;

/**
 *
 * @author Peter
 */
public class SecurityProgram extends Program {

    private TerminalSession session;

    public SecurityProgram(Directory p) {
        super("secr", p);
    }

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
                    session.addLine("I don't know what they've told you, but I");
                    session.addLine("wouldn't listen too closely. I can assure you");
                    session.addLine("that your best interests are not at heart.");
                    session.addLine("Please don't forget about this little chat we had.\nBe seeing you.");

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
