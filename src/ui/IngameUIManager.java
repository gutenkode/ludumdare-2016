package ui;

import entities.ScriptTrigger;
import mote4.scenegraph.Window;
import mote4.util.audio.AudioPlayback;
import main.RootLayer;
import ui.components.*;

import java.util.Stack;
import mote4.util.matrix.ModelMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.ScrollingText;
import main.Vars;
import main.Input;
import rpgbattle.BattleManager;
import ui.components.selectionMenu.SelectionMenu;
import ui.components.selectionMenu.SingleSelectionMenu;
import ui.components.selectionMenu.TabbedSelectionMenu;
import ui.script.ScriptReader;
import ui.selectionmenubehavior.ingame.RootIngameMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Static class for managing the UI elements in the overworld.
 * @author Peter
 */
public class IngameUIManager implements MenuHandler {
    
    private static final IngameUIManager manager;
    private static final Stack<SelectionMenu> selectionMenus;
    private static SelectionMenu closingMenu; // a reference to a recently closed menu is kept to play its closing animation
    private static ScriptReader currentScript; // the object responsible for parsing scripts and script logic
    private static ScriptTrigger trigger; // a reference to the current script trigger is kept in order to tell it whether to reset
    
    private static ScrollingText logMessage; // simple string that appears to announce events in the overworld
    private static double logMessageTimeout = 0;

    private static boolean gamePaused = false,
                           scriptPlaying = false,
                           showDialogue = false,
                           showFlavorText = false,
                           lockFlavorText = false,
                           showSprite = false,
                           showScriptChoice = false,
                           showSkillCostMenu = false;
    
    private static float dialogueSlide, // position slide for the dialogue box
                         statBarSlide, // position slide for the player's stat bar
                         menuCloseTransition, // when a menu is closed it will "slide out" of the window
                         flavorTextRenderYOffset; // flavor text bar will smoothly slide to the correct position

    static {
        manager = new IngameUIManager();
        selectionMenus = new Stack<>();
        dialogueSlide = 1;
        statBarSlide = 1;
        flavorTextRenderYOffset = 0;
    }
    
    public static void update() {
        double delta = (Window.delta()*60);
        if (gamePaused) 
        {
            selectionMenus.peek().update();
            // make flavor text box float to y position
            if (showFlavorText) {
                float yOffset;
                if (lockFlavorText)
                    yOffset = 0;
                else
                    yOffset = selectionMenus.peek().cursorPos()+.75f;
                yOffset *= Vars.UI_SCALE;

                flavorTextRenderYOffset -= (flavorTextRenderYOffset-yOffset)/3 * delta;
            }
        }
        else if (scriptPlaying)
        {
            // update script logic here
            if (showScriptChoice) {
                // update script choice
                int choice = ScriptChoiceMenu.update();
                if (choice != -1) {
                    currentScript.parseChoice(choice, manager);
                    showScriptChoice = false;
                }
            } 
            else if (Input.isKeyNew(Input.Keys.YES)) {
                // advance script dialogue
                currentScript.advance(manager);
                AudioPlayback.playSfx("sfx_menu_open_pane");
            }
        }

        if (showDialogue)
            dialogueSlide -= dialogueSlide * .3 * delta;
        else
            dialogueSlide += (1-dialogueSlide) * .3 * delta;

        if (gamePaused || !BattleManager.getPlayer().areStatsFull())
            statBarSlide -= statBarSlide * .3 * delta;
        else
            statBarSlide += (1-statBarSlide) *.3 * delta;

        // when log text is done printing, display for an amount of time
        if (logMessageTimeout > 0 && logMessage.isDone())
                logMessageTimeout -= Window.delta();
    }
    public static void render(Transform trans) {
        //trans.model.setIdentity();
        //trans.makeCurrent();
        ModelMatrix model = trans.model;

        // PlayerStatBar, sets own shaders
        if (statBarSlide < .95)
            PlayerStatBar.render(54, RootLayer.height()-42- Vars.UI_SCALE/2+(int)(60*statBarSlide), trans);

        // initialize state for the rest of the UI
        ShaderMap.use("texture_uiblur");
        trans.view.setIdentity();
        trans.model.setIdentity();
        trans.makeCurrent();

        // render log message
        if (logMessageTimeout > 0) {
            // does not set model to identity as this is the first potentially rendered item
            model.translate(80, RootLayer.height()-80);
            model.makeCurrent();
            TextureMap.bindUnfiltered("font_1");
            logMessage.render();
        }
        // dialogue box needs to play exit animation after a script ends, so it is here
        if (dialogueSlide < .95) {
            model.setIdentity();
            // the dialogue box will auto-align with the bottom of the screen
            model.translate(0, dialogueSlide*100);
            // and center in the X direction
            model.translate(RootLayer.width()/2-DialogueMenu.BORDER_W/2- Vars.UI_SCALE,
                    RootLayer.height()-40-3* Vars.UI_SCALE);
            model.makeCurrent();
            DialogueMenu.render();
        }
        if (scriptPlaying)
        {
            // talking sprite
            // no smooth in/out animation, but not too noticeable next to dialogue bar
            model.setIdentity();
            model.translate(RootLayer.width()/2+DialogueMenu.BORDER_W/2+ Vars.UI_SCALE,
                    RootLayer.height()-40-3* Vars.UI_SCALE);
            model.makeCurrent();
            SpriteMenu.render(model);

            // script choice dialogue
            // TODO needs a simple animation
            if (showScriptChoice)
            {
                model.setIdentity();
                model.translate(10+RootLayer.width()/2-DialogueMenu.BORDER_W/2- Vars.UI_SCALE,
                        RootLayer.height()-32-5* Vars.UI_SCALE-ScriptChoiceMenu.height());
                model.makeCurrent();
                ScriptChoiceMenu.render(model);
            }
        }
        if (gamePaused)
        {
            // when exiting the final menu and unpausing the game, the closing animation will still play,
            // since it is rendered after this if block
            model.setIdentity();
            for (SelectionMenu sm : selectionMenus) {
                model.translate(Vars.UI_SCALE/2, Vars.UI_SCALE/2);
                model.makeCurrent();
                model.push();
                sm.render(model);
                model.pop();
            }
            // flavor text and preview sprite
            if (showFlavorText)
            {
                model.translate(selectionMenus.peek().width()+ Vars.UI_SCALE*2,flavorTextRenderYOffset);
                model.makeCurrent();
                FlavorTextMenu.render();

                // the sprite's position is relative to the flavor text box, so model is not reset
                if (showSprite)
                {
                    model.translate(0, Vars.UI_SCALE*2+FlavorTextMenu.height());
                    model.makeCurrent();
                    SpriteMenu.render(model);
                }
            }
            if (showSkillCostMenu)
            {
                model.setIdentity();
                float x = RootLayer.width()-SkillCostMenu.width()-Vars.UI_SCALE*3;
                float y = Vars.UI_SCALE;
                model.translate(x,y);
                model.makeCurrent();
                SkillCostMenu.render();
                ShaderMap.use("vertexcolor");
                model.translate(2,2);
                trans.makeCurrent();
                SkillCostMenu.renderBars();
                ShaderMap.use("texture");
                trans.makeCurrent();
                SkillCostMenu.renderBarText();
            }
        }
        // when a menu is closed, it will play an exit animation before being completely destroyed
        // it is visual only and has no effect on menu logic
        if (closingMenu != null) {
            menuCloseTransition += (3+menuCloseTransition*.5) * (Window.delta()*60);
            if (menuCloseTransition > closingMenu.height()+ Vars.UI_SCALE/2*(selectionMenus.size()+1)) {
                closingMenu.destroy();
                closingMenu = null;
            } else {
                model.setIdentity();
                model.translate(Vars.UI_SCALE / 2 * (selectionMenus.size() + 1),
                        Vars.UI_SCALE / 2 * (selectionMenus.size() + 1));
                model.translate(0, -menuCloseTransition);
                model.makeCurrent();
                closingMenu.render(model);
            }
        }
    }
    
    // static methods for encapsulated functions
    
    public static void pauseGame() {
        if (gamePaused || Input.currentLock() != Input.Lock.PLAYER) // can only pause if the player is in control and not already paused
            return;
        Input.pushLock(Input.Lock.MENU);
        gamePaused = true;
        AudioPlayback.playSfx("sfx_menu_open_main");
        manager.openMenu(new RootIngameMenu(manager));
    }
    public static void logMessage(String message) {
        if (logMessage != null)
            logMessage.destroy();
        logMessage = new ScrollingText(message, "font_1", 0, 0, Vars.UI_SCALE, Vars.UI_SCALE, 50);
        logMessageTimeout = 1; // time to display after text is done printing, in seconds
    }
    public static void playScript(ScriptTrigger t, String scriptName) {
        trigger = t;
        showDialogue = true;
        scriptPlaying = true;
        Input.pushLock(Input.Lock.SCRIPT);
        currentScript = new ScriptReader(scriptName);
        currentScript.advance(manager);
    }
    public static void showSkillCostMenu(boolean show) {
        showSkillCostMenu = show;
    }
    
    // menu methods
    
    @Override
    public void openMenu(SelectionMenuBehavior b) {
        SelectionMenu sm = new SingleSelectionMenu(b);
        if (!selectionMenus.empty())
            AudioPlayback.playSfx("sfx_menu_open_pane");
        selectionMenus.push(sm);
        sm.onFocus();
    }
    @Override
    public void openTabbedMenu(SelectionMenuBehavior... b) {
        SelectionMenu sm = new TabbedSelectionMenu(b);
        if (!selectionMenus.empty())
            AudioPlayback.playSfx("sfx_menu_open_pane");
        selectionMenus.push(sm);
        sm.onFocus();
    }
    @Override
    public void setMenuCursorPos(int i) {
        selectionMenus.peek().setCursorPos(i);
    }
    @Override
    public void forceMenuRefocus() {
        selectionMenus.peek().onFocus();
    }
    @Override
    public void closeMenu() {
        if (Input.currentLock() != Input.Lock.MENU)
            return;
        showDialogue = false;
        showFlavorText = false;
        showSprite = false;
        if (closingMenu != null)
            closingMenu.destroy();
        menuCloseTransition = 0;
        closingMenu = selectionMenus.pop();
        if (selectionMenus.empty()) {
            gamePaused = false;
            Input.popLock(Input.Lock.MENU);
            AudioPlayback.playSfx("sfx_menu_close_main");
        } else {
            selectionMenus.peek().onFocus();
            AudioPlayback.playSfx("sfx_menu_close_pane");
        }
    }
    
    // dialogue menus
    
    @Override
    public void showDialogue(String s) {
        DialogueMenu.setText(s);
        showDialogue = true;
        showSprite = false;
    }
    @Override
    public void showDialogue(String s, String sprite) {
        DialogueMenu.setText(s);
        SpriteMenu.setSprite(sprite);
        showDialogue = true;
        showSprite = true;
    }
    @Override
    public void closeDialogue() {
        showDialogue = false;
        showSprite = false;
    }

    // script menus
    
    @Override
    public void displayScriptChoice(String[] s) {
        if (scriptPlaying) {
            showScriptChoice = true;
            ScriptChoiceMenu.setText(s);
        }
    }
    @Override
    public void loadScriptLine(String s) {
        if (scriptPlaying) {
            DialogueMenu.setText(s);
            SpriteMenu.setSprite(currentScript.spriteName);
        }
    }
    @Override
    public void endScript(boolean b) {
        if (Input.currentLock() != Input.Lock.SCRIPT)
            return;
        showDialogue = false;
        scriptPlaying = false;
        Input.popLock(Input.Lock.SCRIPT);
        if (!b)
            trigger.reset();
    }
    
    // flavor text methods
    
    @Override
    public void showFlavorText(boolean lock, String s) {
        lockFlavorText = lock;
        FlavorTextMenu.setText(s);
        showFlavorText = true;
        showSprite = false;
    }
    @Override
    public void showFlavorText(boolean lock, String s, String sprite) {
        lockFlavorText = lock;
        FlavorTextMenu.setText(s);
        SpriteMenu.setSprite(sprite);
        showFlavorText = true;
        showSprite = true;
    }
    @Override
    public void setFlavorTextLock(boolean lock) {
        lockFlavorText = lock;
    }
    @Override
    public void closeFlavorText() {
        showFlavorText = false;
        showSprite = false;
    }
}