package ui;

import entities.ScriptTrigger;
import nullset.RootLayer;
import ui.components.SpriteMenu;
import ui.components.DialogueMenu;
import ui.components.SelectionMenu;
import java.util.Stack;
import mote4.util.matrix.ModelMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.ScrollingText;
import nullset.Const;
import nullset.Input;
import rpgbattle.BattleManager;
import ui.components.FlavorTextMenu;
import ui.components.PlayerStatBar;
import ui.components.ScriptChoiceMenu;
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
    private static int logMessageTimeout = 0;

    private static boolean gamePaused = false,
                           scriptPlaying = false,
                           showDialogue = false,
                           showFlavorText = false,
                           lockFlavorText = false,
                           showSprite = false,
                           showScriptChoice = false;
    
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
        if (gamePaused) 
        {
            selectionMenus.peek().update();
        }
        else if (scriptPlaying)
        {
            if (showScriptChoice) {
                int choice = ScriptChoiceMenu.update();
                if (choice != -1) {
                    currentScript.parseChoice(choice, manager);
                    showScriptChoice = false;
                }
            } 
            else if (Input.isKeyNew(Input.Keys.YES)) {
                currentScript.advance(manager);
            }
        }

        if (showDialogue)
            dialogueSlide /= 1.5;
        else
            dialogueSlide += (1-dialogueSlide)/3;

        if (gamePaused || !BattleManager.getPlayer().areStatsFull())
            statBarSlide /= 1.5;
        else
            statBarSlide += (1-statBarSlide)/3;
    }
    public static void render(Transform trans) {
        //trans.model.setIdentity();
        //trans.makeCurrent();
        ModelMatrix model = trans.model;

        // PlayerStatBar, sets own shaders
        if (statBarSlide < .95)
            PlayerStatBar.render(54, RootLayer.height()-42-Const.UI_SCALE/2+(int)(60*statBarSlide), trans);

        // initialize state for the rest of the UI
        ShaderMap.use("texture_uiblur");
        trans.view.setIdentity();
        trans.model.setIdentity();
        trans.makeCurrent();
        
        if (logMessageTimeout > 0) {
            logMessageTimeout--;
            // does not set model to identity as this is the first potentially rendered item
            model.translate(80, RootLayer.height()-80);
            model.makeCurrent();
            TextureMap.bindUnfiltered("font_1");
            logMessage.render();
        }
        // dialogue box needs to play exit animation after a script ends
        if (dialogueSlide < .95) {
            model.setIdentity();
            // the dialogue box will auto-align with the bottom of the screen
            model.translate(0, dialogueSlide*100);
            // and center in the X direction
            model.translate(RootLayer.width()/2-DialogueMenu.BORDER_W/2-Const.UI_SCALE,
                    RootLayer.height()-40-3*Const.UI_SCALE);
            model.makeCurrent();
            DialogueMenu.render();
        }
        if (scriptPlaying || gamePaused)
        {
            if (scriptPlaying)
            {
                // sprite
                // no smooth in/out animation, but not too noticeable next to dialogue bar
                model.setIdentity();
                model.translate(RootLayer.width()/2+DialogueMenu.BORDER_W/2+Const.UI_SCALE,
                        RootLayer.height()-40-3*Const.UI_SCALE);
                model.makeCurrent();
                SpriteMenu.render(model);

                // script choice dialogue
                // !!!! needs a simple animation
                if (showScriptChoice) {
                    model.setIdentity();
                    model.translate(RootLayer.width()/2-DialogueMenu.BORDER_W/2-Const.UI_SCALE,
                            RootLayer.height()-40-5*Const.UI_SCALE-ScriptChoiceMenu.height());
                    model.makeCurrent();
                    ScriptChoiceMenu.render(model);
                }
            }
            if (gamePaused)
            {
                // when exiting the final menu and unpausing the game, the closing animation will still play
                model.setIdentity();
                for (SelectionMenu sm : selectionMenus) {
                    model.translate(Const.UI_SCALE/2, Const.UI_SCALE/2);
                    model.makeCurrent();
                    model.push();
                    sm.render(model);
                    model.pop();
                }
                // flavor text and preview sprite still pop in/out, but resize and move dynamically
                if (showFlavorText) 
                {
                    float yOffset;
                    if (lockFlavorText)
                        yOffset = 0;
                    else
                        yOffset = selectionMenus.peek().cursorPos()+.75f;
                    yOffset *= Const.UI_SCALE;

                    flavorTextRenderYOffset -= (flavorTextRenderYOffset-yOffset)/2;

                    model.translate(selectionMenus.peek().width()+Const.UI_SCALE*2,flavorTextRenderYOffset);
                    model.makeCurrent();
                    FlavorTextMenu.render();

                    // the sprite's position is relative to the flavor text box, model is not reset
                    if (showSprite) 
                    {
                        model.translate(0, Const.UI_SCALE*2+FlavorTextMenu.height()-1);
                        model.makeCurrent();
                        SpriteMenu.render(model);
                    }
                }
            }
        }
        // when a menu is closed it will play an exit animation before being completely destroyed
        // it is visual only and has no affect on menu logic
        if (closingMenu != null) {
            menuCloseTransition += 3+menuCloseTransition*.5;
            if (menuCloseTransition > closingMenu.height()+Const.UI_SCALE/2*(selectionMenus.size()+1)) {
                closingMenu.destroy();
                closingMenu = null;
            } else {
                model.setIdentity();
                model.translate(Const.UI_SCALE / 2 * (selectionMenus.size() + 1),
                        Const.UI_SCALE / 2 * (selectionMenus.size() + 1));
                model.translate(0, -menuCloseTransition);
                model.makeCurrent();
                closingMenu.render(model);
            }
        }
    }
    
    // static methods for encapsulated functions
    
    public static void pauseGame() {
        if (gamePaused)
            return;
        
        Input.pushLock(Input.Lock.MENU);
        gamePaused = true;
        manager.openMenu(new RootIngameMenu(manager));
    }
    public static void logMessage(String message) {
        if (logMessage != null)
            logMessage.destroy();
        logMessage = new ScrollingText(message, "font_1", 0, 0, Const.UI_SCALE, Const.UI_SCALE, 1);
        logMessageTimeout = 100;
    }
    public static void playScript(ScriptTrigger t, String scriptName) {
        trigger = t;
        showDialogue = true;
        scriptPlaying = true;
        Input.pushLock(Input.Lock.SCRIPT);
        currentScript = new ScriptReader(scriptName);
        currentScript.advance(manager);
    }
    
    // menu methods
    
    @Override
    public void openMenu(SelectionMenuBehavior b) {
        SelectionMenu sm = new SelectionMenu(b);
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
        showDialogue = false;
        showFlavorText = false;
        showSprite = false;
        if (closingMenu != null)
            closingMenu.destroy();
        menuCloseTransition = 0;
        closingMenu = selectionMenus.pop();
        if (selectionMenus.empty()) {
            gamePaused = false;
            Input.popLock();
        } else {
            selectionMenus.peek().onFocus();
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
        showDialogue = false;
        scriptPlaying = false;
        Input.popLock();
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