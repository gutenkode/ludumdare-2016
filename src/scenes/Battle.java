package scenes;

import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.MeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.MeshMap;
import org.lwjgl.opengl.GL11;
import rpgbattle.fighter.EnemyFighter;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renders the 3D portion of a battle.
 * @author Peter
 */
public class Battle implements Scene {
    
    private static Mesh background;
    private static String bgName;
    private float fov = 50;

    private static ArrayList<EnemyFighter> enemies;
    /*
        values for panning the camera
        x-coord, y-coord, zoom amount, z-coord (height)
        the two sets of values are target values and current (interpolated) values
     */
    private static float[] cameraPan = new float[8];
    private static double[][] enemyPos = // starting locations for each enemy, based on the number of enemies present
        new double[][] {
            {0,0},
            {-1,0, 1,0},
            {-1.5,-.5, 0,0, 1.5,-.5},
            {-1.5,1, -1,-1, 1,-1, 1.5,1},
            {-1.5,.75, -1,-1, 0,0, 1,-1, 1.5,.75}
        };

    static {
        float startPos = -3f, // start coordinates
              step = .25f;      // will add new grid coordinates at this interval
        float[][][] points = new float[25][25][3]; // how big the grid should be
        
        // construct grid of points for the mesh
        for (int x = 0;  x < points.length; x++)
            for (int y = 0;  y < points[0].length; y++) {
                float sineZdistort = (float)Math.sin(y)/4 -(float)Math.cos(x)/4;
                double dist = Math.pow((float)x/points.length*2-1, 2)+Math.pow((float)y/points.length*2-1, 2);
                float edgeZdistort = (float)Math.pow(dist, 5)*2;
                
                points[x][y] = new float[] {startPos+step*x, // x and y grid coordinates
                                            startPos+step*y,
                                            sineZdistort+edgeZdistort};
            }
        
        MeshBuilder mb = new MeshBuilder(3);
        mb.includeTexCoords(2);
        
        // convert list of points into a list of vertices and texture coordinates
        for (int x = 0;  x < points.length-1; x++)
            for (int y = 0;  y < points[0].length-1; y++) {
                mb.vertices(points[x][y]);
                mb.vertices(points[x+1][y]);
                mb.vertices(points[x][y+1]);
                
                mb.vertices(points[x][y+1]);
                mb.vertices(points[x+1][y]);
                mb.vertices(points[x+1][y+1]);
                
                float texScale = .125f;
                float texX1 = (x-1)*texScale;
                float texX2 = x*texScale;
                float texY1 = (y-1)*texScale;
                float texY2 = y*texScale;
                mb.texCoords(texX1,texY1, texX2,texY1, texX1,texY2, 
                             texX1,texY2, texX2,texY1, texX2,texY2);
            }
        background = mb.constructVAO(GL11.GL_TRIANGLES);
    }

    private Transform trans;
    
    public Battle() {
        trans = new Transform();
    }

    public static void setEnemies(ArrayList<EnemyFighter> enemyList) {
        Arrays.fill(cameraPan,0);
        enemies = new ArrayList<>();
        for (EnemyFighter e : enemyList)
            enemies.add(e);
        // set the positions for each enemy sprite
        for (int i = 0; i < enemies.size(); i++) {
            double posX = enemyPos[enemies.size()-1][2*i];
            double posY = enemyPos[enemies.size()-1][2*i+1];
            //posX += Math.random()*.5 - .25;
            //posY += Math.random()*.5 - .25;
            //enemies.get(i).getSprite().setPos((int)(posX), (int)(posY));
            enemies.get(i).getSprite().setPos((float)posX, (float)posY);
        }
    }

    /**
     * Pan the camera to focus on one enemy.
     * This will also trigger the enemy sprites to glow.
     * @param i The index of the enemy to look at.  Values -2 or lower will look at all enemies.
     *          Any other value resets to the default camera.
     */
    public static void lookAtEnemy(int i, boolean glow) {
        disableEnemyGlow();
        for (EnemyFighter f : enemies)
            f.getSprite().showStats(false);

        if (i >= 0 && i < enemies.size()) {
            if (glow)
                enemies.get(i).getSprite().glow(true);

            cameraPan[0] = enemies.get(i).getSprite().getPosX();
            cameraPan[1] = enemies.get(i).getSprite().getPosY();
            cameraPan[2] = cameraPan[3] = .25f;
        } else if ( i <= -2) {
            cameraPan[0] = cameraPan[1] = 0;
            cameraPan[2] = -.5f;
            cameraPan[3] = .5f;
            if (glow)
                for (int j = 0; j < enemies.size(); j++)
                    enemies.get(j).getSprite().glow(true,-j);
        }
        else
        {
            cameraPan[0] = cameraPan[1] = cameraPan[2] = cameraPan[3] = 0;
        }
    }
    public static void disableEnemyGlow() {
        for (EnemyFighter f : enemies)
            f.getSprite().glow(false);
    }

    public static void setBackground(String bg) {
        bgName = bg;
    }

    @Override
    public void update(double time, double delta) {
        // mouselook
        /*
        mouse = Window.getCursorPos();
        int[] window = Window.getWindowSize();
        mouse[0] /= window[0];
        mouse[1] /= window[1];
        mouse[0] -= .5;
        mouse[1] -= .5;
        */
        double d = delta*60;
        // camera pan
        cameraPan[4] -= d*(cameraPan[4]-cameraPan[0])/10;
        cameraPan[5] -= d*(cameraPan[5]-cameraPan[1])/10;
        cameraPan[6] -= d*(cameraPan[6]-cameraPan[2])/15;
        cameraPan[7] -= d*(cameraPan[7]-cameraPan[3])/15;
    }

    @Override
    public void render(double time, double delta) {
        float cycle = (float)(time*.1);
        glDisable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // background
        ShaderMap.use("battle_bg");
        trans.view.setIdentity();
        trans.view.translate(cameraPan[4]*.1f, cameraPan[5]*.1f, -4+cameraPan[6]); // pull camera back
        trans.view.rotate(cycle, 0, 0, 1);
        trans.makeCurrent();
        Uniform.varFloat("cycle", cycle);
        TextureMap.bindUnfiltered("ui_bg_"+ bgName);
        background.render();

        // floor
        ShaderMap.use("texture");
        trans.view.setIdentity();
        trans.view.translate(0, -.75f, -3.5f+cameraPan[6]); // pull camera back
        trans.view.translate(-cameraPan[4],-cameraPan[7],-cameraPan[5]); // translate to look at enemies
        //trans.view.translate(0,-.5f,0); // slide down
        trans.view.rotate(-1.25f, 1, 0, 0); // rotate down
        trans.view.scale(2, 2, 2);
        trans.makeCurrent();
        TextureMap.bindUnfiltered("ui_floor_tile1");
        MeshMap.render("quad");

        glEnable(GL_DEPTH_TEST);
        // EnemySprites
        ShaderMap.use("spritesheet_nolight");
        trans.view.setIdentity();
        trans.view.translate(0, -.75f, -3.5f+cameraPan[6]); // pull camera back
        trans.view.translate(-cameraPan[4],-cameraPan[7],-cameraPan[5]); // translate to look at enemies
        trans.view.translate(0,1,0); // slide up
        trans.view.rotate(0.25f, 1, 0, 0); // rotate up
        trans.view.scale(.95f,-.95f,.95f);
        trans.makeCurrent();

        for (EnemyFighter f : enemies) {
            f.getSprite().render3d(trans.model);
            //float[] coords = new float[] {.1f,.1f,.1f};
            //float[] coords = new float[] {f.getSprite().getPosX(),.5f,f.getSprite().getPosY()};
            //f.getSprite().setPos2d(trans.get2DCoords(coords));
            float[] pos = new float[] {f.getSprite().getPosX()*.3f -cameraPan[4]*.3f,
                                       f.getSprite().getPosY()*.2f -cameraPan[5]*.2f};
            pos[1] += cameraPan[7]*.2f;

            if (cameraPan[6] > 0)
                pos[0] *= 1+cameraPan[6];
            else
                pos[1] -= cameraPan[6]*.3;
            f.getSprite().setPos2d(pos);
            //System.out.println(Arrays.toString(trans.get2DCoords(coords)));
            //System.out.println(trans.model.matrix());
        }
        // delete entities that have finished their death animation
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).isDead() && enemies.get(i).isDeathAnimationFinished() && !enemies.get(i).hasAnimationsLeft()) {
                enemies.remove(i);
                i--; // to not skip an entry
            }
        }
        trans.model.setIdentity();
    }
    
    @Override
    public void framebufferResized(int width, int height) {
        trans.projection.setPerspective(width, height, 1f, 100f, fov);
    }

    @Override
    public void destroy() {
        
    }
    
}
