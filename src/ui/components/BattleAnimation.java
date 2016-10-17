package ui.components;

import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;

/**
 * Renders sprite effects in a battle, such as effects for skills and attacks.
 *
 * Created by Peter on 10/17/16.
 */
public class BattleAnimation {
    public enum Type {
        ICE("anim_ice", 5,7, .5f),
        FIRE("anim_fire", 5,4, .5f),
        ELEC("anim_elec", 5,6, .5f);

        public final String SPRITE_NAME;
        public final int WIDTH, HEIGHT;
        public final float FRAME_ADD;
        Type(String sn, int w, int h, float f) {
            SPRITE_NAME = sn;
            WIDTH = w;
            HEIGHT = h;
            FRAME_ADD = f;
        }
    }

    public final Type TYPE;
    private float index;
    private int delay;

    public BattleAnimation(Type t) {
        TYPE = t;
        index = 0;
        delay = 0;
    }

    public boolean render() {
        if (delay > 0) {
            delay--;
            return false;
        }

        TextureMap.bindUnfiltered(TYPE.SPRITE_NAME);
        Uniform.varFloat("spriteInfo", TYPE.WIDTH,TYPE.HEIGHT, (int)index);
        EnemySprite.sprite.render();

        index += TYPE.FRAME_ADD;
        return index >= TYPE.WIDTH*TYPE.HEIGHT;
    }
}
