package entities;

import map.MapManager;
import mote4.scenegraph.Window;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.builder.StaticMeshBuilder;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Peter
 */
public class Water extends Entity {
    
    private static Mesh mesh;
    
    private float offset = 0;
    
    static {
        mesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN, 
                3, new float[] {-10,-10,.1f, 
                                -10,10,.1f, 
                                10,10,.1f,
                                10,-10,.1f}, 
                2, new float[] {-10,-10,
                                -10,10, 
                                10,10, 
                                10,-10}, 
                0, null, new float[] {0,0,1, 0,0,1, 0,0,1, 0,0,1});
    }
    
    public Water(int h) {
        posX = 0;
        posY = 0;
        tileHeight = h;
    }
    
    @Override
    public void update() {
        offset += Window.delta()*.1;
        offset %= 2;

        posX = (int)MapManager.getPlayer().posX;
        posY = (int)MapManager.getPlayer().posY;
    }
    
    @Override
    public void render(TransformationMatrix model) {
        float addHeight = (float)Math.sin(offset*Math.PI)*.1f;

        model.translate(posX+offset, posY+offset/2, tileHeight+addHeight);
        model.makeCurrent();
        
        Uniform.varFloat("spriteInfo", 1,1,1);
        TextureMap.bindUnfiltered("entity_water");
        //for (int i = 0; i < 6; i++)
        //{
            Uniform.varFloat("colorMult", 1,1,1,.5f);
            //Uniform.varFloat("colorMult", 1,1,1,.65f-.1f*i);
            //model.translate((float)Math.sin(offset*Math.PI), (float)Math.sin(offset*Math.PI)/2, .1f);
            model.translate(0,0, .1f);
            model.makeCurrent();
            mesh.render();
        //}
        Uniform.varFloat("colorMult", 1,1,1,1);
    }

    @Override
    public String getName() { return "Water"; }
    @Override
    public String serialize() {
        return getName()+","+tileHeight();
    }
}
