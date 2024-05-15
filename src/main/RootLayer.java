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

    private static RootLayer rootLayer;
    static { rootLayer = new RootLayer(); }

    public enum State {
        TITLE,
        INGAME,
        BATTLE_INTRO,
        BATTLE,
        EDITOR;
    }
    private int renderWidth, renderHeight;
    private State state;
    private Scene[] current, title, ingame, battle, editor;
    private Scene transition;
    private MultiColorFBO sceneFbo, uiFbo;


    public static RootLayer getInstance() { return rootLayer; }
    private RootLayer() {
        super(null);

        title  = new Scene[] {new Title(),  new TitleUI()};
        ingame = new Scene[] {new Ingame(), new IngameUI(), new TerminalScene()};
        battle = new Scene[] {new Battle(), new BattleUI()};
        editor = new Scene[] {new Editor(), new EditorUI(), new TerminalScene()};

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
        sceneFbo = new MultiColorFBO(renderWidth*renderScale,renderHeight*renderScale,2,true,false,GL_RGBA16,GL_RGBA16);
        TextureMap.delete("fbo_scene");
        TextureMap.delete("fbo_post_values");
        sceneFbo.addToTextureMap("fbo_scene",0,true);
        sceneFbo.addToTextureMap("tex_post_values",1,true);
        if (uiFbo != null)
            uiFbo.destroy();
        int[] buffers = new int[] {-1, sceneFbo.getColorBufferID(1)};
        uiFbo = new MultiColorFBO(renderWidth*renderScale,renderHeight*renderScale,2,false,false,buffers,GL_RGBA16,GL_RGBA16);
        TextureMap.delete("fbo_ui");
        uiFbo.addToTextureMap("fbo_ui",0,true);

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
    public void destroy() {
        for (Scene s : title)
            s.destroy();
        for (Scene s : ingame)
            s.destroy();
        for (Scene s : battle)
            s.destroy();
        for (Scene s : editor)
            s.destroy();
    }

    private void setState(State s) {
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
        return getInstance().state;
    }

    public static int width() { return getInstance().renderWidth; }
    public static int height() { return getInstance().renderHeight; }

    /**
     * Starts a transition to battle.
     * Called by BattleManager.
     */
    public void transitionToBattle() {
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
        getInstance().setState(State.BATTLE);
    }
    public static void exitBattle() {
        loadIngame();
    }
    public static void loadIngame() {
        getInstance().setState(State.INGAME);
        AudioPlayback.playMusic("mus_field",true);
    }
    public static void loadEditor() {
        getInstance().setState(State.EDITOR);
        AudioPlayback.stopMusic();
    }
    public static void loadTitle() {
        getInstance().setState(State.TITLE);
        AudioPlayback.stopMusic();
    }
}
