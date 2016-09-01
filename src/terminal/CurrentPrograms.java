package terminal;

import java.util.ArrayList;
import terminal.program.*;

/**
 *
 * @author Peter
 */
public class CurrentPrograms {
    private static ArrayList<Program> programs;
    
    static {
        programs = new ArrayList<>();
        programs.add(new VillageProgram());
        programs.add(new LevelChangeProgram());
        programs.add(new SecurityProgram());
    }
    
    public static void add(Program p) { programs.add(p); }
    public static ArrayList<Program> getList() { return programs; }
}
