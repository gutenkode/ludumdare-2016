package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;

import java.util.Random;

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
        CEILING;
    }
    
    private float val0, val1;
    
    private final Type TYPE;
    private final boolean solid;
    private boolean b1;
    
    public StaticObject(float x, float y, String model, float val) {
        val0 = val;
        posX = x+.5f;
        posY = y+.5f;
        hitboxW = .3f;
        hitboxH = .3f;
        
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
            default:
                throw new IllegalArgumentException("Unrecognized StaticObject type: "+model);
        }
    }

    @Override
    public void onRoomInit() {
        tileHeight = MapManager.getTileHeight((int)posX, (int)posY);
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
                model.translate(posX, posY, tileHeight+val0);
                model.rotate((float)Math.PI/2, 1, 0, 0);
                model.scale(.65f, .7f, .65f);
                model.rotate(val1, 0, 1, 0);
                model.makeCurrent();
                TextureMap.bindUnfiltered("obj_barrel");
                MeshMap.render("barrel");
                break;
            case CRATE:
                Uniform.varFloat("spriteInfo", 1,1,0);
                model.translate(posX, posY, tileHeight+val0+.4f);
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
                model.translate(posX, posY, tileHeight+1.75f);
                model.scale(.25f,.75f,1);
                model.rotate((float)Math.PI, 1, 0, 0);
                model.makeCurrent();
                TextureMap.bindUnfiltered("obj_fluorescent");
                MeshMap.render("quad");
                Uniform.varFloat("emissiveMult", 0);
                break;
            case CEILING:
                //model.scale(1,1,1);
                model.translate(posX, posY, 2.2f);
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
        }
    }

    @Override
    public String getName() { return "Static: "+TYPE.name(); }

    @Override
    public boolean hasLight() { return TYPE == Type.FLUORESCENT && b1; }
    @Override
    public float[] lightPos() { return new float[] {posX,posY,tileHeight+1.75f}; }
    @Override
    public float[] lightColor() { return new float[] {4,4,4}; }
}
