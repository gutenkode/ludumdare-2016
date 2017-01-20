package entities;

import map.MapManager;
import mote4.scenegraph.Window;
import mote4.util.matrix.TransformationMatrix;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import nullset.Input;
import org.lwjgl.opengl.GL11;
import rpgbattle.BattleManager;

/**
 *
 * @author Peter
 */
public class Player extends Entity {
    
    private static Mesh mesh;

    private static int[][] dirMat = new int[][] {{5,4,3},
                                                 {6,0,2},
                                                 {7,0,1}};
    
    static {
        int spriteHeight = 24;
        int spriteWidth = 16;
        float mW = 1;
        float mH = mW/spriteWidth*spriteHeight;
        
        mW /= 2;
        mesh = StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLE_FAN, 
                3, new float[] { mW,0, -1, 
                                 mW,0, mH-1, 
                                -mW,0, mH-1,
                                -mW,0, -1}, 
                2, new float[] {1,1,
                                1,0,
                                0,0, 
                                0,1}, 
                0, null, new float[] {0,1,0, 0,1,0, 0,1,0, 0,1,0});
    }
    
    private float walkSpeed = .010f, // acceleration speed
                  runSpeed = .016f,
                  velX, velY, // movement  velocity
                  elevatorHeight; // smooth height transition
    private double[] camera = new double[2];
    private double[] lastCursorPos = new double[2];
    
    private int targetDirection = 0, drainStaminaDelay;
    private int[][] spriteMapInd = new int[][] {
                                    {0,1,2,3,4,3,2,1}, // corresponding tilesheet row to direction index
                                    {10,11,11,10,10,10,11,11}}; // number of frames in animation
    private boolean flipSprite = false; // mirror sprite for left/right reuse
    private float spriteFrameCycle = 0, // frame animation for walking
                  direction = 0; // used to set sprite frames
    
    public Player(int x, int y) {
        posX = x;
        posY = y;
        hitboxW = .3f;
        hitboxH = .2f;
        lastCursorPos = Window.getCursorPos();
    }
    
    @Override
    public void onRoomInit() {
        setTileHeight();
        elevatorHeight = tileHeight;
    }
    private void setTileHeight() {
        // if there's a walkable entity right above the player it will "step up" onto it
        // this is to make elevators work
        // otherwise, ignore it, allowing the creation of raised catwalks
        Entity e = MapManager.getWalkableEntityOnTile((int)posX, (int)posY);
        if (e != null && tileHeight+1 >= e.tileHeight)
            tileHeight = e.tileHeight;// Math.max(tileHeight, e.tileHeight);
        else
            tileHeight = MapManager.getTileHeight((int)posX, (int)posY);
    }
    
    /**
     * An active elevator will set the correct height for the player here.
     * @param h 
     */
    public void setElevatorHeight(float h) {
        elevatorHeight = h;
    }
    
    public float elevatorHeight() { return elevatorHeight; }
    /**
     * The direction the player is facing, in a range of 0-8.
     * Starts/ends facing down.
     * @return 
     */
    public float facingDirection() { return direction; }

    public double[] cameraRot() { return camera; }
    
    @Override
    public void update() {
        setTileHeight();
        
        setSpriteDirection();

        if (Input.currentLock() == Input.Lock.PLAYER) {
            // first person camera rotation
            double[] c = Window.getCursorPos();
            camera[0] += (lastCursorPos[0]-c[0])*.005;
            camera[1] += (c[1]-lastCursorPos[1])*.005;

            camera[1] = Math.max(-Math.PI*.4, Math.min(Math.PI*.4, camera[1]));

            lastCursorPos = c;
        }
        
        float accel;
        boolean running = false;
        if (Math.abs(targetDirection-direction) > 2)
            accel = 0; // don't move until the player sprite is facing "forward"
        else {
            if (Input.isKeyDown(Input.Keys.SPRINT) &&
                BattleManager.getPlayer().stats.stamina > 0) {
                accel = runSpeed;
                running = true;
            } else
                accel = walkSpeed;
        }

        float[] chg = topDownMovement(accel);
        //float[] chg = firstPersonMovement(accel);
        
        if (running && (chg[0] != 0 || chg[1] != 0)) {
            if (drainStaminaDelay <= 0) {
                BattleManager.getPlayer().drainStamina(1);
                drainStaminaDelay = 2;
            }
        }
        drainStaminaDelay--;
        
        // walking animation
        float spriteChg = Math.abs(chg[0])+Math.abs(chg[1]);
        if (spriteChg == 0)
            spriteFrameCycle = 0;
        else {
            spriteFrameCycle += spriteChg*15;
            spriteFrameCycle %= spriteMapInd[1][(int)direction];
        }
        
        velX += chg[0];
        velY += chg[1];
        
        posX += velX;
        posY += velY;
        
        velX *= .75f;
        velY *= .75f;
    }
    private float[] topDownMovement(float accel) {
        float chgX = 0;
        if (Input.currentLock() == Input.Lock.PLAYER)
        {
            if (Input.isKeyDown(Input.Keys.RIGHT)) {
                chgX = accel;
            } else if (Input.isKeyDown(Input.Keys.LEFT)) {
                chgX = -accel;
            }
        }
        if (MapManager.entityCollidesWithSolidEntities(this, velX+chgX, 0) ||
                MapManager.entityCollidesWithMapAndWalkableEntities(this, velX+chgX, 0)) {
            chgX = 0;
            velX = 0;
        }

        float chgY = 0;
        if (Input.currentLock() == Input.Lock.PLAYER)
        {
            if (Input.isKeyDown(Input.Keys.UP)) {
                chgY = -accel;
            } else if (Input.isKeyDown(Input.Keys.DOWN)) {
                chgY = accel;
            }
        }
        if (MapManager.entityCollidesWithSolidEntities(this, 0, velY+chgY) ||
                MapManager.entityCollidesWithMapAndWalkableEntities(this, 0, velY+chgY)) {
            chgY = 0;
            velY = 0;
        }

        // keep diagonal speed the same as one-directional
        if (chgX != 0 && chgY != 0) {
            chgX *= .7;
            chgY *= .7;
        }

        return new float[] {chgX, chgY};
    }
    private float[] firstPersonMovement(float accel) {
        float chgX = 0;
        boolean lr = false, ud = false;
        if (Input.currentLock() == Input.Lock.PLAYER)
        {
            if (Input.isKeyDown(Input.Keys.RIGHT)) {
                lr = true;
                chgX = (float)Math.cos(camera[0])*-accel;
            } else if (Input.isKeyDown(Input.Keys.LEFT)) {
                lr = true;
                chgX = (float)Math.cos(camera[0])*accel;
            }
            if (Input.isKeyDown(Input.Keys.UP)) {
                ud = true;
                chgX += (float)Math.sin(camera[0])*accel;
            } else if (Input.isKeyDown(Input.Keys.DOWN)) {
                ud = true;
                chgX += (float)Math.sin(camera[0])*-accel;
            }
        }
        if (MapManager.entityCollidesWithSolidEntities(this, velX+chgX, 0) ||
                MapManager.entityCollidesWithMapAndWalkableEntities(this, velX+chgX, 0)) {
            chgX = 0;
            velX = 0;
        }

        float chgY = 0;
        if (Input.currentLock() == Input.Lock.PLAYER)
        {
            if (Input.isKeyDown(Input.Keys.RIGHT)) {
                lr = true;
                chgY = (float)Math.sin(camera[0])*accel;
            } else if (Input.isKeyDown(Input.Keys.LEFT)) {
                lr = true;
                chgY = (float)Math.sin(camera[0])*-accel;
            }
            if (Input.isKeyDown(Input.Keys.UP)) {
                ud = true;
                chgY += (float)Math.cos(camera[0])*accel;
            } else if (Input.isKeyDown(Input.Keys.DOWN)) {
                ud = true;
                chgY += (float)Math.cos(camera[0])*-accel;
            }
        }
        if (MapManager.entityCollidesWithSolidEntities(this, 0, velY+chgY) ||
                MapManager.entityCollidesWithMapAndWalkableEntities(this, 0, velY+chgY)) {
            chgY = 0;
            velY = 0;
        }

        // keep diagonal speed the same as one-directional
        if (ud && lr) {
            chgX *= .7;
            chgY *= .7;
        }

        return new float[] {chgX, chgY};
    }

    /**
     * Set direction for sprite rendering, does not affect velocity
     * or collision detection.
     */
    private void setSpriteDirection() {
        int matIndX = 1, matIndY = 1;
        
        if (Input.currentLock() == Input.Lock.PLAYER) 
        {
            if (Input.isKeyDown(Input.Keys.UP)) 
            {
                matIndY = 0;
            } 
            else if (Input.isKeyDown(Input.Keys.DOWN)) 
            {
                matIndY = 2;
            } 

            if (Input.isKeyDown(Input.Keys.RIGHT)) 
            {
                matIndX = 2;
            } 
            else if (Input.isKeyDown(Input.Keys.LEFT)) 
            {
                matIndX = 0;
            }
            // don't change direction of no buttons are pressed
            if (matIndY != 1 || matIndX != 1)
                targetDirection = dirMat[matIndY][matIndX];
        }
        
        // rotate the player sprite until it faces the current direction
        // loops smoothly over the gap between 7 and 0
        if (direction < targetDirection) {
            if (targetDirection-direction > 4)
                direction -= .25f;
            else
                direction += .25f;
        } else if (direction > targetDirection) {
            if (direction-targetDirection > 4)
                direction += .25f;
            else
                direction -= .25f;
        }
        if (direction < 0)
            direction += 8;
        direction %= 8;
        
        // flip sprite if facing left
        flipSprite = (direction >= 5);
    }
    
    @Override
    public void render(TransformationMatrix model) {
        if (Input.currentLock() == Input.Lock.ELEVATOR)
            model.translate(posX, posY+.2f, elevatorHeight+1);
        else
            model.translate(posX, posY+.2f, tileHeight+1);
        
        if (flipSprite)
            model.scale(-1, 1, 1);
        model.makeCurrent();
        TextureMap.bindUnfiltered("entity_player");
        
        int spriteInd = 12*spriteMapInd[0][(int)direction];
        if (spriteFrameCycle > 0)
            spriteInd += (int)spriteFrameCycle+1;
        //int spriteInd = 4*direction+(int)spriteFrameCycle;
        Uniform.varFloat("spriteInfo", 12,5,spriteInd);
        
        mesh.render();
    }

    @Override
    public String getName() { return "Player"; }
}
