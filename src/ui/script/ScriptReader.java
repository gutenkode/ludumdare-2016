package ui.script;

import java.util.Arrays;
import map.MapManager;
import rpgsystem.Inventory;
import rpgsystem.Item;
import ui.MenuHandler;

/**
 * Contains the logic for parsing a script.  Used by a MenuHandler, and makes
 * callbacks to the MenuHandler to tell it which actions to show to the player.
 * @author Peter
 */

public class ScriptReader {
    public String spriteName;
    int index;
    private Script script;
    private String[] dialogue, tagList;

    public ScriptReader(Script s) {
        script = s;
        index = 0;
        dialogue = s.dialogue;
        spriteName = "talk_none";
    }
    /**
     * Tells the script to advance to the next scene.
     * It will either tell the event handler to load the next line of dialogue
     * or display a question prompt.
     * @param eh
     */
    public void advance(MenuHandler eh) {
        index++;
        if (index > dialogue.length) {
            eh.endScript(true);
            return;
        }
        String s = dialogue[index-1];
        if (s.startsWith("$PROMPT")) {
            String[] list = s.split("_");
            String[] choices = new String[(list.length-1)/2]; // only include dialogue options
            for (int i = 0; i < choices.length; i++)
                choices[i] = list[1+2*i];
            tagList = new String[(list.length-1)/2]; // make a list of tags to jump to after the decision
            for (int i = 0; i < choices.length; i++)
                tagList[i] = list[2+2*i];
            eh.displayScriptChoice(choices);
            //eh.displayScriptChoice(dialogue);
            /*
            switch (tok.nextToken()) {
                case "$PROMPT":
                    break;
                case "$TAG":
                        break;
                default:
                    throw new IllegalArgumentException("Invalid control tag when parsing script: "+s);
            }*/
        } else if (s.startsWith("$GOTO")) {
            // search the dialogue list for the tag to jump to
            String tag = s.substring(s.indexOf(' ')+1);
            gotoTag(tag);
            advance(eh); // call advance again
        } else if (s.startsWith("$RESET")) {
            // don't play this script and leave it in an untriggered state
            eh.endScript(false);
            //return;
            //advance(eh); // call advance again
        } else if (s.startsWith("$INVCHECK")) {
            // if the player has the specified item, jump to a tag
            String[] list = s.split("_");
            for (Item i : Inventory.get())
                if (i.name.equals(list[1]))
                    gotoTag(list[2]);
            advance(eh); // call advance again
        } else if (s.startsWith("$SECRALRT")) {
            // if the security alert is triggered, jump to a tag
            String[] list = s.split("_");
            if (MapManager.getTimelineState().isAlertTriggered())
                gotoTag(list[1]);
            advance(eh); // call advance again
        } else if (s.startsWith("$SPRITE")) {
            // change the talksprite
            String[] list = s.split(" ");
            spriteName = list[1];
            advance(eh); // call advance again
        } else if (s.startsWith("$")) {
            advance(eh); // this is a tag or unrecognized command, skip it
        } else
            eh.loadScriptLine(s);
    }
    private void gotoTag(String tag) {
        boolean found = false;
        int ind = 0;
        while (!found) {
            if (dialogue[ind].equals(tag))
                found = true;
            else
                ind++;
            if (ind == dialogue.length)
                throw new IllegalStateException("Could not find tag in dialogue: "+tag+"\n"+Arrays.toString(dialogue));
        }
        index = ind;
    }
    /**
     * The result of the player's choice from a prompt.
     * The number is the index of the decision made.
     * @param i 
     * @param eh 
     */
    public void parseChoice(int i, MenuHandler eh) {
        gotoTag(tagList[i]);
        advance(eh);
    }
    public boolean hasMoreLines() {
        return index < dialogue.length;
    }
    public String spriteName() {
        return spriteName;
    }
}