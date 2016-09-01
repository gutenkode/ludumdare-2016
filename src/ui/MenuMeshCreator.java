package ui;

import mote4.util.vertex.builder.MeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Peter
 */
public class MenuMeshCreator {
    /**
     * Utility to create meshes for menu backgrounds.
     * The texture coordinates are broken into thirds - the inside ninth is used
     * for the entire inner x,y,w,h box while the outer 8 are for the border.
     * @param x X coordinate of starting corner.
     * @param y Y coordinate of ending corner.
     * @param w Width of the box.
     * @param h Height of the box.
     * @param s Border size.  The border is a box outside of the specified
     * x,y,w,h boundaries.
     * @return 
     */
    public static Mesh create(int x, int y, int w, int h, int s) {
        
        MeshBuilder b = new MeshBuilder(2);
        b.includeTexCoords(2);
        // top left
        b.vertices(x-s,y-s,
                   x-s,y,
                   x,y-s,
                   x-s,y,
                   x,y,
                   x,y-s);
        b.texCoords(0,0,
                    0,1/3f, 
                    1/3f,0,
                    0,1/3f,
                    1/3f,1/3f, 
                    1/3f,0);
        // top
        b.vertices(x,y-s,
                   x,y,
                   x+w,y-s,
                   x,y,
                   x+w,y,
                   x+w,y-s);
        b.texCoords(1/3f,0,
                    1/3f,1/3f, 
                    2/3f,0,
                    1/3f,1/3f,
                    2/3f,1/3f, 
                    2/3f,0);
        //top right
        b.vertices(x+w,y-s,
                   x+w,y,
                   x+w+s,y-s,
                   x+w,y,
                   x+w+s,y,
                   x+w+s,y-s);
        b.texCoords(2/3f,0, 
                    2/3f,1/3f, 
                    1,0,
                    2/3f,1/3f,
                    1,1/3f, 
                    1,0);
        // left
        b.vertices(x-s,y,
                   x-s,y+h,
                   x,y,
                   x-s,y+h,
                   x,y+h,
                   x,y);
        b.texCoords(0,1/3f,
                    0,2/3f, 
                    1/3f,1/3f,
                    0,2/3f,
                    1/3f,2/3f, 
                    1/3f,1/3f);
        // right
        b.vertices(x+w,y,
                   x+w,y+h,
                   x+w+s,y,
                   x+w,y+h,
                   x+w+s,y+h,
                   x+w+s,y);
        b.texCoords(2/3f,1/3f, 
                    2/3f,2/3f, 
                    1,1/3f,
                    2/3f,2/3f,
                    1,2/3f,  
                    1,1/3f);
        // bottom left
        b.vertices(x-s,y+h,
                   x-s,y+h+s,
                   x,y+h,
                   x-s,y+h+s,
                   x,y+h+s,
                   x,y+h);
        b.texCoords(0,2/3f, 
                    0,1, 
                    1/3f,2/3f,
                    0,1,
                    1/3f,1, 
                    1/3f,2/3f);
        // bottom right
        b.vertices(x+w,y+h,
                   x+w,y+h+s,
                   x+w+s,y+h,
                   x+w,y+h+s,
                   x+w+s,y+h+s,
                   x+w+s,y+h);
        b.texCoords(2/3f,2/3f, 
                    2/3f,1, 
                    1,2/3f,
                    2/3f,1,
                    1,1, 
                    1,2/3f);
        // bottom
        b.vertices(x,y+h,
                   x,y+h+s,
                   x+w,y+h,
                   x,y+h+s,
                   x+w,y+h+s,
                   x+w,y+h);
        b.texCoords(1/3f,2/3f, 
                    1/3f,1, 
                    2/3f,2/3f,
                    1/3f,1,
                    2/3f,1, 
                    2/3f,2/3f);
        // center
        b.vertices(x,y,
                   x,y+h,
                   x+w,y,
                   x,y+h,
                   x+w,y+h,
                   x+w,y);
        b.texCoords(1/3f,1/3f, 
                    1/3f,2/3f, 
                    2/3f,1/3f,
                    1/3f,2/3f,
                    2/3f,2/3f, 
                    2/3f,1/3f);
        
        return b.constructVAO(GL11.GL_TRIANGLES);
    }
}
