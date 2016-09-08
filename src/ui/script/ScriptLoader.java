package ui.script;

import mote4.util.FileIO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Loads script dialogue from the current json file.  The list of scripts is
 * populated when a level number is set in MapLevelManager.
 * @author Peter
 */
public class ScriptLoader {

    private static String path = "";
    private static HashMap<String, String[]> scripts;

    /**
     * Changes the filepath for maps to load.  Should be updated when the level number changes.
     * IMPORTANT: calling this method with a different path will clear all currently loaded script files.
     * @param s
     */
    public static void setLevelPath(String s) {
        if (!path.equals(s)) {
            path = s;
            loadScriptFile();
        }
    }

    /**
     * Will open the json file and load the contents into a HashMap.
     */
    private static void loadScriptFile() {
        if (scripts != null)
            scripts.clear();
        else
            scripts = new HashMap<>();
        JSONObject json = new JSONObject(FileIO.readFile("/res/maps/"+path+"/scripts.json"));
        for (String key : json.keySet()) {
            JSONArray arr = json.getJSONArray(key);
            String[] dialogue = new String[arr.length()];
            for (int i = 0; i < dialogue.length; i++)
                dialogue[i] = arr.getString(i);
            scripts.put(key, dialogue);
        }
    }

    public static String[] getScript(String name) {
        return scripts.get(name);
    }
}