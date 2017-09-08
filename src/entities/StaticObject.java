package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Peter
 */
public class StaticObject extends Entity {

    private static Random rand = new Random();
    
    private enum Type {
        BARREL,
        CRATE,
        FLUORESCENT,
        CEILING,
        CHAIN,
        PIPE;
    }

    private float floatHeight, val0, val1;

    private final String model;
    private final float origVal;
    private final Type TYPE;
    private final boolean solid;
    private boolean b1;
    
    public StaticObject(float x, float y, String model, float val) {
        origVal = val0 = val;
        this.model = model;
        posX = x+.5f;
        posY = y+.5f;
        hitboxW = hitboxH = .3f;
        
        switch(model) {
            case "Barrel":
                TYPE = Type.BARREL;
                solid = true;
                val1 = (x+y*.7f+val0)%.6f;
                val1 -= .3f;
                posX += val1*.5;
                posY -= val1*.5;
                break;
            case "Crate":
                TYPE = Type.CRATE;
                solid = true;
                val1 = (x+y*.7f+val0)%.6f;
                val1 -= .3f;
                posX += val1*.5;
                posY -= val1*.5;
                break;
            case "Fluorescent":
                TYPE = Type.FLUORESCENT;
                solid = false;
                b1 = true;
                val1 = 60;
                break;
            case "Ceiling":
                TYPE = Type.CEILING;
                solid = false;
                break;
            case "Chain":
                TYPE = Type.CHAIN;
                solid = true;
                posX = x;
                posY = y;
                val1 = (int)val%10;
                val1 /= 2;
                val0 = (int)val/10;
                if (val0 == 0) {
                    hitboxW = val1;
                    posX += val1;
                    hitboxH = 0;
                } else {
                    hitboxH = val1;
                    posY += val1;
                    hitboxW = 0;
                }
                break;
            case "Pipe":
                TYPE = Type.PIPE;
                solid = true;
                val0 = val*(float)Math.PI/2;
                hitboxW = hitboxH = .2f;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized StaticObject type: "+model);
        }
    }

    @Override
    public void onRoomInit() {
        floatHeight = MapManager.getTileHeight((int)posX, (int)posY);
        switch (TYPE) {
            case BARREL:
                floatHeight += val0;
                break;
            case CRATE:
                floatHeight += val0+.4f;
                break;
            case CEILING:
                floatHeight = 2.2f;
                break;
            case FLUORESCENT:
                floatHeight += 1.75f;
                break;
            case CHAIN:
                floatHeight = 0;
                break;
            case PIPE:
            default:
                break;
        }
        tileHeight = (int)floatHeight;
    }

    @Override
    public void update() {
        switch (TYPE) {
            case FLUORESCENT:
                val1--;
                if (val1 <= 0) {
                    b1 = !b1;
                    if (b1)
                        val1 = rand.nextInt(200);
                    else
                        val1 = rand.nextInt(9)+3;
                    MapManager.refreshLighting();
                }
                break;
            default:
                break;
        }
    }
    
    @Override
    public boolean isSolid() { return solid; }

    @Override
    public void render(TransformationMatrix model) {
        model.setIdentity();
        
        switch (TYPE) {
            case BARREL:
                Uniform.varFloat("spriteInfo", 1,1,0);
                model.translate(posX, posY, floatHeight);
                model.rotate((float)Math.PI/2, 1, 0, 0);
                model.scale(.65f, .7f, .65f);
                model.rotate(val1, 0, 1, 0);
                model.makeCurrent();
                TextureMap.bindUnfiltered("obj_barrel");
                MeshMap.render("barrel");
                break;
            case CRATE:
                Uniform.varFloat("spriteInfo", 1,1,0);
                model.translate(posX, posY, floatHeight);
                model.scale(.4f,.4f,.4f);
                model.rotate(val1, 0, 0, 1);
                model.makeCurrent();
                TextureMap.bindUnfiltered("obj_crate");
                MeshMap.render("cube");
                break;
            case FLUORESCENT:
                Uniform.varFloat("spriteInfo", 2,1,0);
                if (b1) {
                    Uniform.varFloat("spriteInfoEmissive", 2, 1, 1);
                    Uniform.varFloat("emissiveMult", 3);
                }
                model.translate(posX, posY, floatHeight);
                model.scale(.25f,.75f,1);
                model.rotate((float)Math.PI, 1, 0, 0);
                model.makeCurrent();
                TextureMap.bindUnfiltered("obj_fluorescent");
                MeshMap.render("quad");
                Uniform.varFloat("emissiveMult", 0);
                break;
            case CEILING:
                //model.scale(1,1,1);
                model.translate(posX, posY, floatHeight);
                //model.rotate((float)Math.PI, 1, 0, 0);
                //model.makeCurrent();
                TextureMap.bindUnfiltered("obj_ceiling");
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        Uniform.varFloat("spriteInfo", 1,2,0);
                        model.makeCurrent();
                        MeshMap.render("quad");
                        
                        Uniform.varFloat("spriteInfo", 1,2,1);
                        model.translate(0, -2, 0);
                        model.rotate(-(float)Math.PI/2, 1, 0, 0);
                        model.makeCurrent();
                        MeshMap.render("quad");
                        
                        model.rotate((float)Math.PI/2, 1, 0, 0);
                        model.translate(2, 2, 0);
                    }
                    model.translate(-4*2, 2);
                }
                break;
            case CHAIN:
                Uniform.varFloat("spriteInfo", 1/(val1*2),.5f,0);
                model.translate(posX, posY, floatHeight+1);
                model.rotate((float)Math.PI/2, 1, 0, 0);
                model.rotate(((float)Math.PI/2)*val0, 0, 1, 0);
                model.scale(val1, -1,1);
                model.makeCurrent();
                TextureMap.bindUnfiltered("obj_chain");
                glEnable(GL_CULL_FACE);
                {
                    glCullFace(GL_BACK);
                    MeshMap.render("quad");
                    model.rotate((float) Math.PI, 0, 1, 0);
                    model.makeCurrent();
                    MeshMap.render("quad");
                }
                glDisable(GL_CULL_FACE);
                break;
            case PIPE:
                Uniform.varFloat("spriteInfo", 1,1,0);
                model.translate(posX, posY, floatHeight+1.3f);
                //model.rotate((float)Math.PI/2, 1, 0, 0);
                model.scale(.32f, .32f, .32f);
                model.rotate((float)Math.PI+val0, 0, 0, 1);
                model.makeCurrent();
                TextureMap.bindUnfiltered("obj_pipe");
                MeshMap.render("pipe");
                break;
        }
    }

    @Override
    public String getName() { return "Static: "+TYPE.name(); }
    public String getAttributeString() {
        return super.getAttributeString()+"\ntype:"+TYPE.name();
    }
    @Override
    public String serialize() {
        return this.getClass().getSimpleName() +","+ (int)(posX-.5) +","+ (int)(posY-.5) +","+ model +","+ origVal;
    }

    @Override
    public boolean hasLight() { return TYPE == Type.FLUORESCENT && b1; }
    @Override
    public float[] lightPos() { return new float[] {posX,posY,floatHeight}; }
    @Override
    public float[] lightColor() { return new float[] {4,4,4}; }
}
