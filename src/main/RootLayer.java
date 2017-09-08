package main;

import mote4.scenegraph.Layer;
import mote4.scenegraph.Scene;
import mote4.scenegraph.target.MultiColorFBO;
import mote4.util.audio.AudioPlayback;
import mote4.util.texture.TextureMap;
import scenes.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 2/10/17.
 */
public class RootLayer extends Layer {

    public enum State {
        TITLE,
        INGAME,
        BATTLE_INTRO,
        BATTLE,
        EDITOR;
    }
    private static int renderWidth, renderHeight;
    private static State state;
    private static Scene[] current, title, ingame, battle, editor;
    private static Scene transition;

    private MultiColorFBO sceneFbo, uiFbo;

    static { // TODO remove all static code
        title = new Scene[] {new Title(), new TitleUI()};
        ingame = new Scene[] {new Ingame(), new IngameUI(), new TerminalScene()};
        battle = new Scene[] {new Battle(), new BattleUI()};
        editor = new Scene[] {new Editor(), new EditorUI(), new TerminalScene()};
    }

    public RootLayer() {
        super(null);
        setState(State.TITLE);
    }

    @Override
    public void update(double time, double delta) {
        switch (state) {
            case BATTLE_INTRO:
                transition.update(time, delta);
                break;
            default:
                for (Scene s : current)
                    s.update(time, delta);
                break;
        }
    }
    @Override
    public void render(double time, double delta) {
        uiFbo.makeCurrent(0); // clear the UI buffer now to avoid problems with clearing the FBO buffer
        glClear(GL_COLOR_BUFFER_BIT);

        switch (state) {
            case BATTLE_INTRO:
                sceneFbo.makeCurrent();
                transition.render(time, delta);
                break;
            case TITLE:
            case BATTLE:
                sceneFbo.makeCurrent();
                current[0].render(time, delta);
                uiFbo.makeCurrent();
                current[1].render(time, delta);
                break;
            case INGAME:
            case EDITOR:
                sceneFbo.makeCurrent();
                current[0].render(time, delta);
                uiFbo.makeCurrent();
                current[1].render(time, delta);
                current[2].render(time, delta);
                break;
        }
    }
    @Override
    public void makeCurrent() {}

    /**
     * Rebuilds framebuffers used for rendering without changing the resolution.
     */
    public void refreshFramebuffer() {
        framebufferResized(renderWidth, renderHeight);
    }

    @Override
    public void framebufferResized(int width, int height) {
        float ratio = width/(float)height;

        renderHeight = Vars.WINDOW_HEIGHT; // 1080/3;
        renderWidth = (int)(renderHeight*ratio);

        int renderScale = 1;
        if (Vars.useSSAA())
            renderScale = 2;

        if (sceneFbo != null)
            sceneFbo.destroy();
        sceneFbo = new MultiColorFBO(renderWidth*renderScale,renderHeight*renderScale,true,false,2);
        TextureMap.delete("fbo_scene");
        TextureMap.delete("fbo_dofvalue");
        sceneFbo.addToTextureMap("fbo_scene",0);
        sceneFbo.addToTextureMap("fbo_dofvalue",1);
        if (uiFbo != null)
            uiFbo.destroy();
        int[] buffers = new int[] {-1, sceneFbo.getColorBufferID(1)};
        uiFbo = new MultiColorFBO(renderWidth*renderScale,renderHeight*renderScale,false,false,buffers);
        //uiFbo = new FBO(renderWidth,renderHeight,false,false,null);
        TextureMap.delete("fbo_ui");
        uiFbo.addToTextureMap("fbo_ui",0);

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

    private static void setState(State s) {
        state = s;
        switch (state) {
            case TITLE:
                current = title;
                break;
            case INGAME:
                current = ingame;
                break;
            case BATTLE:
                current = battle;
                break;
            case EDITOR:
                current = editor;
                break;
            case BATTLE_INTRO:
                break;
            default:
                throw new IllegalStateException("Invalid state.");
        }
    }
    public static State getState() {
        return state;
    }

    public static int width() { return renderWidth; }
    public static int height() { return renderHeight; }

    /**
     * Starts a transition to battle.
     * Called by BattleManager.
     */
    public static void transitionToBattle() {
        setState(State.BATTLE_INTRO);
        if (transition != null)
            transition.destroy();
        transition = new BattleTransition();
    }

    /**
     * Begins a battle after a transition.
     * Called by BattleTransition, should not be called otherwise.
     */
    public static void startBattle() {
        setState(State.BATTLE);
    }
    public static void exitBattle() {
        loadIngame();
    }
    public static void loadIngame() {
        setState(State.INGAME);
        AudioPlayback.playMusic("mus_field",true);
    }
    public static void loadEditor() {
        setState(State.EDITOR);
        AudioPlayback.stopMusic();
    }
    public static void loadTitle() {
        setState(State.TITLE);
        AudioPlayback.stopMusic();
    }
}
