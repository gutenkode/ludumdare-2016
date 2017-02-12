package nullset;

import mote4.scenegraph.Layer;
import mote4.scenegraph.Scene;
import mote4.scenegraph.target.FBO;
import mote4.scenegraph.target.MultiColorFBO;
import mote4.util.texture.TextureMap;
import rpgbattle.BattleManager;
import scenes.*;

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

    private MultiColorFBO sceneFbo;
    private FBO uiFbo;

    static {
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
    public void update(double delta) {
        switch (state) {
            case BATTLE_INTRO:
                transition.update(delta);
                break;
            default:
                for (Scene s : current)
                    s.update(delta);
                break;
        }
    }
    @Override
    public void render(double delta) {
        switch (state) {
            case BATTLE_INTRO:
                sceneFbo.makeCurrent();
                transition.render(delta);
                break;
            case TITLE:
            case BATTLE:
                sceneFbo.makeCurrent();
                current[0].render(delta);
                uiFbo.makeCurrent();
                current[1].render(delta);
                break;
            case INGAME:
            case EDITOR:
                sceneFbo.makeCurrent();
                current[0].render(delta);
                uiFbo.makeCurrent();
                current[1].render(delta);
                current[2].render(delta);
                break;
        }
    }
    @Override
    public void makeCurrent() {}
    @Override
    public void framebufferResized(int width, int height) {
        float ratio = width/(float)height;

        renderHeight = Const.WINDOW_HEIGHT; // 1080/3;
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

    public static void setState(State s) {
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

    public static void transitionToBattle() {
        setState(State.BATTLE_INTRO);
        if (transition != null)
            transition.destroy();
        transition = new BattleTransition();
    }
    public static void startBattle() { setState(State.BATTLE); }
    public static void exitBattle() { setState(State.INGAME); }
}
