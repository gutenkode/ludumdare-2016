package terminal;

import java.util.ArrayList;

/**
 *
 * @author Peter
 */
public class CurrentFiles {
    
    private static ArrayList<String> files;
    
    static {
        files = new ArrayList<>();
    }
    
    public static void add(String p) { files.add(p); }
    public static ArrayList<String> getList() { return files; }
}
