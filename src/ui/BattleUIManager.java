package ui;

import java.util.ArrayList;
import java.util.Stack;
import mote4.util.matrix.ModelMatrix;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import nullset.Const;
import nullset.Input;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import scenes.RootScene;
import ui.components.DialogueMenu;
import ui.components.EnemySprite;
import ui.components.FlavorTextMenu;
import ui.components.LogMenu;
import ui.components.PlayerStatBar;
import ui.components.ScriptChoiceMenu;
import ui.components.SelectionMenu;
import ui.components.SpriteMenu;
import ui.script.ScriptLoader;
import ui.script.ScriptReader;
import ui.selectionmenubehavior.RootBattleMenu;
import ui.selectionmenubehavior.SelectionMenuBehavior;

/**
 * Static class for managing the UI elements in a battle.
 * @author Peter
 */
public class BattleUIManager implements MenuHandler {
    
    private static final BattleUIManager manager;
    private static final Stack<SelectionMenu> selectionMenus;
    private static ScriptReader currentScript;
    private static ArrayList<EnemySprite> enemySprites; // sprites for all enemies to be drawn
    private static ArrayList<EnemyFighter> fighters; // stored to easily refresh enemy sprites on display resize
    
    private static boolean playerTurn = false,
                           scriptPlaying = false,
                           showDialogue = false,
                           showFlavorText = false,
                           lockFlavorText = false,
                           showSprite = false,
                           showScriptChoice = false;
    
    static {
        manager = new BattleUIManager();
        selectionMenus = new Stack<>();
    }
    
    private BattleUIManager() {
       
    }
    
    public static void update() {
        if (playerTurn) 
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
                //scriptPlaying = false;
                //Input.popLock();
            }
        }
    }
    public static void render(Transform trans) {
        ModelMatrix model = trans.model;
        
    ShaderMap.use("spritesheet_nolight"); // shader bindings are unindented for readability
    trans.makeCurrent();
        // EnemySprite
        for (EnemySprite s : enemySprites) {
            model.setIdentity();
            s.render(model);
        }
        /*
        for (EnemyFighter f : BattleManager.getEnemies()) {
            model.push();
            EnemySprite.render(model, f);
            model.pop();
            model.translate(96*2, 0);
        }
        */
    // PlayerStatBar sets own shaders
        // PlayerStatBar
        PlayerStatBar.render(RootScene.width()/2, RootScene.height()-42-Const.UI_SCALE/2, trans);
        
    ShaderMap.use("texture_color"); // uses the color attribute, for colored text
    trans.makeCurrent();
        // enemy toasts
        TextureMap.bindUnfiltered("font_1");
        for (EnemySprite s : enemySprites) {
            model.setIdentity();
            s.renderToast(model);
        }
        // player toasts
        model.setIdentity();
        model.translate(RootScene.width()/2, RootScene.height()-60);
        BattleManager.getPlayer().renderToast(model);
        
    ShaderMap.use("texture"); // all remaining UI elements use the texture shader
    trans.makeCurrent();
        // LogMenu
        model.setIdentity();
        model.translate(RootScene.width()/2-DialogueMenu.BORDER_W/2-Const.UI_SCALE, 
                        .5f*Const.UI_SCALE);
        model.makeCurrent();
        LogMenu.render(model);
        
        if (showDialogue) {
            model.setIdentity();
            model.translate(RootScene.width()/2-DialogueMenu.BORDER_W/2-Const.UI_SCALE, 
                            .5f*Const.UI_SCALE);
            model.makeCurrent();
            DialogueMenu.render();
        }

        if (showScriptChoice) {
            model.setIdentity();
            model.translate(RootScene.width()/2-DialogueMenu.BORDER_W/2, 
                            RootScene.height()-40-5*Const.UI_SCALE-ScriptChoiceMenu.height());
            model.makeCurrent();
            ScriptChoiceMenu.render(model);
        }

        if (playerTurn) 
        {
            model.setIdentity();
            model.translate(0, Const.UI_SCALE*2+DialogueMenu.BORDER_H);
            for (SelectionMenu sm : selectionMenus) {
                model.translate(Const.UI_SCALE/2, Const.UI_SCALE/2);
                model.makeCurrent();
                model.push();
                sm.render(model);
                model.pop();
            }
            
            if (showFlavorText) 
            {
                float yOffset;
                if (lockFlavorText)
                    yOffset = 0;
                else
                    yOffset = selectionMenus.peek().cursorPos()+.75f;
                yOffset *= Const.UI_SCALE;
                model.translate(selectionMenus.peek().width()+Const.UI_SCALE*2,yOffset);
                model.makeCurrent();
                FlavorTextMenu.render();
                
                if (showSprite) 
                {
                    model.translate(0, Const.UI_SCALE*2+FlavorTextMenu.height());
                    model.makeCurrent();
                    SpriteMenu.render(model);
                }
            }
        }
        
    }
    
    // static methods for encapsulated functions
    
    /**
     * Initialize sprites for rendering enemies.
     * @param f
     */
    public static void initEnemies(ArrayList<EnemyFighter> f) {
        // enemySprites do not have a destroy() method
        fighters = f;
        refreshEnemies();
        LogMenu.clear();
    }
    public static void refreshEnemies() {
        if (fighters != null) {
            enemySprites = new ArrayList<>();
            int startX = RootScene.width()/2-(96*(BattleManager.getEnemies().size()-1));
            int startY = RootScene.height()/2;
            for (EnemyFighter f : fighters) {
                enemySprites.add(new EnemySprite(startX, startY, f));
                startX += 96*2;
            }
        }
    }
    public static void startPlayerTurn() {
        if (playerTurn)
            return;
        
        //Input.pushLock(Input.Lock.MENU);
        playerTurn = true;
        manager.openMenu(new RootBattleMenu(manager));
    }
    public static void endPlayerTurn() {
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
        selectionMenus.pop().destroy();
        if (selectionMenus.empty()) {
            playerTurn = false;
            //Input.popLock();
        } else {
            selectionMenus.peek().onFocus();
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
        Input.popLock();
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
