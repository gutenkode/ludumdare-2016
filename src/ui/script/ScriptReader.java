package ui.script;

import java.util.Arrays;
import map.MapManager;
import rpgsystem.Inventory;
import rpgsystem.Item;
import ui.IngameUIManager;
import ui.MenuHandler;

/**
 * Contains the logic for parsing a script.  Used by a MenuHandler, and makes
 * callbacks to the MenuHandler to tell it which actions to show to the player.
 * @author Peter
 *
 * Scripts are interpreted as lines of dialogue to display in order.
 * They can use the following control flow commands:
 * $PROMPT_["string1"]_[$tag1]_["string2"]_[$tag2] etc.
 *      Displays a dialogue prompt, and will jump to the specified tags.
 * $GOTO [$tag]
 *      Jumps to a tag.
 * $RESET
 *      Ends this script immediately but does not mark it as read.
 *      It will be triggered again.
 * $INVCHECK [item enum name] [$tag]
 *      Jumps if the player has an item.
 *      The item identifier is the enum name, not the game name.
 * $SECRALRT [$tag]
 *      Jumps if a security alert is triggered.
 * $SPRITE ["sprite_name"]
 *      Sets the sprite to display.
*       Always starts as "talk_none".
 * [$tag]
 *      Any other line starting with $ is a tag.
 *      Tags are skipped when printing lines, but other commands can jump to them.
 */

public class ScriptReader {
    public String spriteName;
    int index;
    private String[] dialogue, tagList; // used only in $PROMPT statements

    public ScriptReader(String s) {
        index = 0;
        dialogue = ScriptLoader.getScript(s);
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
        } else if (s.startsWith("$GOTO")) {
            // search the dialogue list for the tag to jump to
            String tag = s.substring(s.indexOf(' ')+1);
            gotoTag(tag);
            advance(eh); // call advance again
        } else if (s.startsWith("$RESET")) {
            // don't play this script and leave it in an untriggered state
            eh.endScript(false);
        } else if (s.startsWith("$INVCHECK")) {
            // if the player has the specified item, jump to a tag
            String[] list = s.split(" ");
            for (Item i : Inventory.get().keySet())
                if (i.name().equals(list[1]))
                    gotoTag(list[2]);
            advance(eh); // call advance again
        } else if (s.startsWith("$SECRALRT")) {
            // if the security alert is triggered, jump to a tag
            String[] list = s.split(" ");
            if (MapManager.getTimelineState().isAlertTriggered())
                gotoTag(list[1]);
            advance(eh); // call advance again
        } else if (s.startsWith("$TRIGGER_SECRALRT")) {
            MapManager.getTimelineState().triggerAlert(true);
            IngameUIManager.logMessage("Security alert triggered.");
            advance(eh); // call advance again
        } else if (s.startsWith("$SETVAR")) { // $SETVAR_key_value
            // set an environment variable
            String[] list = s.split("_");
            MapManager.getTimelineState().setVar(list[1],list[2]);
            advance(eh); // call advance again
        } else if (s.startsWith("$TESTVAR")) { // $TESTVAR_key_value_$TAG
            // if an environment variable matches a value, jump
            String[] list = s.split("_");
            if (MapManager.getTimelineState().getVar(list[1]).equals(list[2])) {
                gotoTag(list[3]);
            }
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