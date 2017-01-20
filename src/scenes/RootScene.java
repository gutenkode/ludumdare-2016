package scenes;

import mote4.scenegraph.Scene;
import mote4.scenegraph.target.FBO;
import mote4.scenegraph.target.MultiColorFBO;
import mote4.util.texture.TextureMap;
import rpgbattle.BattleManager;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

/**
 * Top-level scene to allow easy switching between sets of Scenes for rendering.
 * @author Peter
 */
public class RootScene implements Scene {
    
    public enum State {
        TITLE,
        INGAME,
        BATTLE_INTRO,
        BATTLE,
        EDITOR;
    }
    
    public static void transitionToBattle() {
        setState(State.BATTLE_INTRO);
        if (transition != null)
            transition.destroy();
        transition = new BattleTransition();
    }
    public static void startBattle() { setState(State.BATTLE); }
    public static void exitBattle() { setState(State.INGAME); }
    public static void setState(State s) {
        currentState = s;
    }
    public static State currentState() {
        return currentState;
    }
    
    private static int renderWidth, renderHeight;
    public static int width() { return renderWidth; }
    public static int height() { return renderHeight; }
    
    private static State currentState;
    private Scene[] title, ingame, battle, editor;
    private static Scene transition;
    private MultiColorFBO sceneFbo;
    private FBO uiFbo;
    
    public RootScene() {
        currentState = State.TITLE;
        title = new Scene[] {new Title(), new TitleUI()};
        ingame = new Scene[] {new Ingame(), new IngameUI(), new TerminalScene()};
        battle = new Scene[] {new Battle(), new BattleUI()};
        editor = new Scene[] {new Editor(), new EditorUI(), new TerminalScene()};
        if (currentState == State.BATTLE) // test battle
            BattleManager.initEnemies("SLIME","SLIME");
    }

    @Override
    public void update(double delta) {
        switch (currentState) {
            case TITLE:
                for (Scene s : title)
                    s.update(delta);
                break;
            case INGAME:
                for (Scene s : ingame)
                    s.update(delta);
                break;
            case BATTLE_INTRO:
                transition.update(delta);
                break;
            case BATTLE:
                for (Scene s : battle)
                    s.update(delta);
                break;
            case EDITOR:
                for (Scene s : editor)
                    s.update(delta);
                break;
        }
    }

    @Override
    public void render(double delta) {
        switch (currentState) {
            case TITLE:
                sceneFbo.makeCurrent();
                title[0].render(delta);
                uiFbo.makeCurrent();
                title[1].render(delta);
                break;
            case INGAME:
                sceneFbo.makeCurrent();
                ingame[0].render(delta);
                uiFbo.makeCurrent();
                ingame[1].render(delta);
                ingame[2].render(delta);
                break;
            case BATTLE_INTRO:
                transition.render(delta);
                break;
            case BATTLE:
                sceneFbo.makeCurrent();
                battle[0].render(delta);
                uiFbo.makeCurrent();
                battle[1].render(delta);
                break;
            case EDITOR:
                sceneFbo.makeCurrent();
                editor[0].render(delta);
                uiFbo.makeCurrent();
                editor[1].render(delta);
                editor[2].render(delta);
                break;
        }
    }

    @Override
    public void framebufferResized(int width, int height) {
        float ratio = width/(float)height;
        
        renderHeight = (1080/3);
        renderWidth = (int)(renderHeight*ratio);
        
        if (sceneFbo != null)
            sceneFbo.destroy();
        sceneFbo = new MultiColorFBO(renderWidth,renderHeight,true,false,2);
        TextureMap.delete("fbo_scene");
        TextureMap.delete("fbo_dofvalue");
        sceneFbo.addToTextureMap("fbo_scene",0);
        sceneFbo.addToTextureMap("fbo_dofvalue",1);
        if (uiFbo != null)
            uiFbo.destroy();
        uiFbo = new FBO(renderWidth,renderHeight,false,false,null);
        TextureMap.delete("fbo_ui");
        uiFbo.addToTextureMap("fbo_ui");
        
        Postprocess.resizeBuffers(renderWidth, renderHeight);

        for (Scene s : title)
            s.framebufferResized(renderWidth,renderHeight);
        for (Scene s : ingame)
            s.framebufferResized(renderWidth,renderHeight);
        for (Scene s : battle)
            s.framebufferResized(renderWidth,renderHeight);
        for (Scene s : editor)
            s.framebufferResized(renderWidth,renderHeight);
    }

    @Override
    public void destroy() {} 
}