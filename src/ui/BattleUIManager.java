package ui;

import java.util.ArrayList;
import java.util.Stack;

import mote4.scenegraph.Window;
import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.ModelMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import main.Vars;
import main.Input;
import main.RootLayer;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import ui.components.DialogueMenu;
import ui.components.FlavorTextMenu;
import ui.components.LogMenu;
import ui.components.PlayerStatBar;
import ui.components.ScriptChoiceMenu;
import ui.components.selectionMenu.SelectionMenu;
import ui.components.SpriteMenu;
import ui.components.selectionMenu.SingleSelectionMenu;
import ui.components.selectionMenu.TabbedSelectionMenu;
import ui.script.ScriptReader;
import ui.selectionmenubehavior.battle.RootBattleMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Static class for managing the UI elements in a battle.
 * @author Peter
 */
public class BattleUIManager implements MenuHandler {
    
    private static final BattleUIManager manager;
    private static final Stack<SelectionMenu> selectionMenus;
    private static ScriptReader currentScript;
    private static ArrayList<EnemyFighter> enemies;
    
    private static boolean playerTurn = false,
                           scriptPlaying = false,
                           showDialogue = false,
                           showFlavorText = false,
                           lockFlavorText = false,
                           showSprite = false,
                           showScriptChoice = false;
    private static float flavorTextRenderYOffset;
    
    static {
        manager = new BattleUIManager();
        selectionMenus = new Stack<>();
    }
    
    private BattleUIManager() {}
    
    public static void update() {
        if (playerTurn) 
        {
            selectionMenus.peek().update();

            if (showFlavorText) {
                float yOffset;
                if (lockFlavorText)
                    yOffset = 0;
                else
                    yOffset = selectionMenus.peek().cursorPos()+.75f;
                yOffset *= Vars.UI_SCALE;

                flavorTextRenderYOffset -= (flavorTextRenderYOffset-yOffset)/3 *(Window.delta()*60);
            }
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
    }
    public static void render(Transform trans) {
        ModelMatrix model = trans.model;
        
    ShaderMap.use("spritesheet_nolight"); // shader bindings are unindented for readability
    trans.bind();
        // EnemySprite
        for (EnemyFighter f : enemies) {
            f.getSprite().render2d(model);
        }
        // PlayerStatBar sets own shaders
        PlayerStatBar.render(RootLayer.width()/2, RootLayer.height()-42- Vars.UI_SCALE/2, trans);
        ShaderMap.use("spritesheet_nolight");
        PlayerStatBar.renderAnimations(RootLayer.width()/2, RootLayer.height()-10- Vars.UI_SCALE/2, model);

    ShaderMap.use("texture_color"); // uses the color attribute, for colored text
    trans.bind();
        // enemy toasts
        TextureMap.bindUnfiltered("font_1");
        for (EnemyFighter f : enemies) {
            model.setIdentity();
            f.getSprite().renderToast(model);
        }
        // player toasts
        model.setIdentity();
        model.translate(RootLayer.width()/2, RootLayer.height()-60);
        BattleManager.getPlayer().renderToast(model);
        // enemy stat buff text
        TextureMap.bindUnfiltered("font_6px");
        for (EnemyFighter f : enemies) {
            model.setIdentity();
            f.getSprite().renderStatText(model);
        }

    ShaderMap.use("texture"); // log menu is not blurred
    trans.bind();
        // LogMenu
        model.setIdentity();
        model.translate(RootLayer.width()/2-DialogueMenu.BORDER_W/2- Vars.UI_SCALE,
                        .5f* Vars.UI_SCALE);
        model.bind();
        LogMenu.render(model);

    ShaderMap.use("texture_uiblur"); // all remaining UI elements use the texture shader with blurring
    trans.bind();
        
        if (showDialogue) {
            model.setIdentity();
            model.translate(RootLayer.width()/2-DialogueMenu.BORDER_W/2- Vars.UI_SCALE,
                            .5f* Vars.UI_SCALE);
            model.bind();
            DialogueMenu.render();
        }

        if (showScriptChoice) {
            model.setIdentity();
            model.translate(RootLayer.width()/2-DialogueMenu.BORDER_W/2,
                            80);
            model.bind();
            ScriptChoiceMenu.render(model);
        }

        if (playerTurn) 
        {
            model.setIdentity();
            model.translate(0, Vars.UI_SCALE*2+DialogueMenu.BORDER_H);
            for (SelectionMenu sm : selectionMenus) {
                model.translate(Vars.UI_SCALE/2, Vars.UI_SCALE/2);
                model.bind();
                model.push();
                sm.render(model);
                model.pop();
            }
            
            if (showFlavorText) 
            {
                model.translate(selectionMenus.peek().width()+ Vars.UI_SCALE*2,flavorTextRenderYOffset);
                model.bind();
                FlavorTextMenu.render();
                
                if (showSprite) 
                {
                    model.translate(0, Vars.UI_SCALE*2+FlavorTextMenu.height());
                    model.bind();
                    SpriteMenu.render(model);
                }
            }
        }
        
    }
    
    // static methods for encapsulated functions
    
    /**
     * Initialize sprites for rendering enemies.
     * This sets up the UI for a new battle.
     * @param f
     */
    public static void initEnemies(ArrayList<EnemyFighter> f) {
        // enemySprites do not have a destroy() method
        enemies = f;
        LogMenu.clear();
    }

    public static void startPlayerTurn() {
        if (playerTurn)
            return;

        playerTurn = true;
        manager.openMenu(new RootBattleMenu(manager));
        AudioPlayback.playSfx("sfx_menu_playerturn");
    }
    public static void endPlayerTurn() {
        playerTurn = false;
        while (!selectionMenus.isEmpty())
            manager.closeMenu();
    }
    /**
     * Send a message to the battle log.  This is the standard output for
     * events during battle.  The showDialogue method is meant to be used for
     * menu information and cutscenes.
     * @param message 
     */
    public static void logMessage(String message) {
        LogMenu.addLine(message);
    }
    public static void playScript(String scriptName) {
        showDialogue = true;
        scriptPlaying = true;
        Input.pushLock(Input.Lock.SCRIPT);
        currentScript = new ScriptReader(scriptName);
        currentScript.advance(manager);
        //DialogueMenu.setText(script.dialogue[0]);
    }
    public static boolean isScriptPlaying() { return scriptPlaying; }
    
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
        showDialogue = false;
        showFlavorText = false;
        showSprite = false;
        selectionMenus.pop().destroy();
        if (selectionMenus.empty()) {
            playerTurn = false;
        } else {
            selectionMenus.peek().onFocus();
            if (playerTurn)
                AudioPlayback.playSfx("sfx_menu_close_pane");
        }
    }
    
    // dialogue methods
    
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

    // script methods
    
    @Override
    public void displayScriptChoice(String[] s) {
        if (scriptPlaying) {
            showScriptChoice = true;
            ScriptChoiceMenu.setText(s);
        }
    }
    @Override
    public void loadScriptLine(String s) {
        if (scriptPlaying)
            DialogueMenu.setText(s);
    }
    @Override
    public void endScript(boolean b) {
        showDialogue = false;
        scriptPlaying = false;
        Input.popLock(Input.Lock.SCRIPT);
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
