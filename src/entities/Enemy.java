package entities;

import map.MapManager;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vector.Vector2f;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;
import rpgbattle.BattleManager;
import rpgbattle.EnemyData;
import scenes.RootScene;

import java.util.Arrays;


/**
 *
 * @author Peter
 */
public class Enemy extends Entity {
    
    private static Mesh mesh;
    
    private float velX, velY;
    
    private int patrolIndex;
    private float[][] patrol;
    private float patrolDelay;
    
    private String enemyName, spriteName;
    
    static {
        mesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN, 
                3, new float[] {0,0,0, 
                                1,0,0, 
                                1,0,2,
                                0,0,2}, 
                2, new float[] {0,1,
                                1,1, 
                                1,0, 
                                0,0}, 
                0, null, new float[] {0,1,0, 0,1,0, 0,1,0, 0,1,0});
    }
    
    public Enemy(int x, int y, String en) {
        hitboxW = hitboxH = .3f;
        enemyName = en;

        // enemyName is the API name, all upper-case
        patrol = EnemyData.getPatrol(enemyName, x+.5f,y+.5f);
        spriteName = EnemyData.getIngameSprite(enemyName);
    }
    
    @Override
    public void onRoomInit() {
        velX = velY = 0;
        // reinitialize at the first node
        posX = patrol[0][0];
        posY = patrol[0][1];
        tileHeight = MapManager.getTileHeight((int)posX,(int)posY);
        patrolIndex = 0;
        patrolDelay = patrol[0][3];
    }
    
    @Override
    public void update() {
        // if the enemy is at the current node
        if (Math.abs(posX - patrol[patrolIndex][0]) < .125 &&
            Math.abs(posY - patrol[patrolIndex][1]) < .125)
        {
            // wait at the node until the delay is 0
            patrolDelay -= .1;
            if (patrolDelay <= 0) {
                // load the next patrol node
                patrolIndex++;
                patrolIndex %= patrol.length;
                patrolDelay = patrol[patrolIndex][3];
            }
        } else {
            // an enemy will move at a constant speed in the direction
            // of the player, if the player is close enough
            Vector2f vec = new Vector2f((float)(posX-MapManager.getPlayer().posX), (float)(posY-MapManager.getPlayer().posY));
            if (vec.length() < 2) {
                vec.normalise();
                velX -= vec.x;
                velY -= vec.y;
            } else {
                // else, move towards next patrol node
                vec = new Vector2f((float)(posX-patrol[patrolIndex][0]), (float)(posY-patrol[patrolIndex][1]));
                if (vec.length() > .1) { // if the enemy is not already close enough to the node
                    vec.normalise();
                    velX -= vec.x;
                    velY -= vec.y;
                }
            }
        }
        velX *= .025;
        velY *= .025;
        if (MapManager.entityCollidesWithSolidEntities(this, velX, 0) ||
            MapManager.entityCollidesWithMap(this,velX, 0))
        {
            velX = 0;
        }
        if (MapManager.entityCollidesWithSolidEntities(this, 0, velY) ||
            MapManager.entityCollidesWithMap(this, 0, velY))
        {
            velY = 0;
        }
        posX += velX;
        posY += velY;
    }
    
    @Override
    public void render(TransformationMatrix model) {
        model.translate((float)posX-.5f, (float)posY, tileHeight);
        model.makeCurrent();
        Uniform.varFloat("spriteInfo", 1,1,0);
        TextureMap.bindUnfiltered(spriteName);
        mesh.render();
    }
    
    @Override
    public void playerPointIn() {
        if (MapManager.getPlayer().tileHeight == tileHeight) 
        {
            // BattleManager -> load enemies for battle
            BattleManager.initEnemies(enemyName,enemyName,enemyName);

            // RootScene -> go to battle
            RootScene.transitionToBattle();

            // MapManager -> delete this entity
            MapManager.removeEntity(this);
        }
    }
}