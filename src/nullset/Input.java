package nullset;

import java.util.Arrays;
import java.util.Stack;
import mote4.scenegraph.Window;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCharCallbackI;

/**
 * Centralized input management.  All input checks for the game should be done
 * through this class, as it provides and easy-to-edit abstraction.
 * @author Peter
 */
public class Input {
    
    public enum Lock {
        NONE,
        PLAYER, // player moving around overworld maps
        TEXTBOX, // dialogue scenes
        MENU, // player selects options from a menu
        SCRIPT, // a script is playing
        TERMINAL, // player is using a command terminal
        ELEVATOR, // an elevator is moving
        FADE; // Postprocess.fadeOut() has been called
    }
    
    public enum Keys {
        YES(0),
        NO(1),
        UP(2),
        DOWN(3),
        LEFT(4),
        RIGHT(5),
        SPRINT(6),
        BACKSPACE(7),
        ENTER(8),
        UP_ARROW(9),
        TIMELINE_1(10),
        TIMELINE_2(11),
        TIMELINE_3(12),
        ESC(13);
        
        int index;
        Keys(int i) {
            index = i;
        }
    }
    
    private static boolean[] isNew, isDown;
    private static boolean recordTyped = false;
    private static Stack<Lock> currentLock;
    private static StringBuilder charBuffer;
    
    static {
        isNew  = new boolean[Keys.values().length];
        isDown = new boolean[Keys.values().length];
        charBuffer = new StringBuilder();
        
        currentLock = new Stack<>();
        currentLock.push(Lock.NONE);
    }
    
    public static void pushLock(Lock l) {
        currentLock.add(l);
        System.out.println("+"+l.name());
    }
    public static void popLock() {
        System.out.println("-"+currentLock.peek().name());
        if (currentLock.size() > 1)
            currentLock.pop();
    }
    public static Lock currentLock() {
        return currentLock.peek();
    }
    
    public static boolean isKeyDown(Keys k) {
        return isDown[k.index];
    }
    public static boolean isKeyNew(Keys k) {
        boolean b =  isNew[k.index];
        isNew[k.index] = false;
        return b;
    }
    public static void clearKeys() {
        Arrays.fill(isNew, false);
        Arrays.fill(isDown, false);
    }
    
    /**
     * Return any typed characters since getTyped() was last called.
     * @return 
     */
    public static String getTyped() {
        String s = charBuffer.toString();
        charBuffer = new StringBuilder();
        return s;
    }
    /**
     * Dump any typed characters
     */
    public static void flushTyped() {
    }
    /**
     * Whether to save a buffer of typed characters or not.
     * Disabled by default to prevent recording large amounts of junk data.
     * @param b 
     */
    public static void recordTyped(boolean b) {
        recordTyped = b;
    }
    
    public static void createCharCallback() {
        glfwSetCharCallback(Window.getWindowID(), (long window, int c) -> {
            if (recordTyped)
                charBuffer.append((char)c);
        });
    }
    
    /**
     * Override the default key callback created by the engine.
     */
    public static void createKeyCallback() {
        glfwSetKeyCallback(Window.getWindowID(), (long window, int key, int scancode, int action, int mods) -> {
            // GLFW_PRESS, GLFW_REPEAT, GLFW_RELEASE
            switch (key) {
                case GLFW_KEY_Z:
                    callbackAction(action, 0);
                    break;
                case GLFW_KEY_X:
                    callbackAction(action, 1);
                    break;
                // up arrow triggers two buttons
                case GLFW.GLFW_KEY_UP:
                    callbackAction(action, 9);
                case GLFW_KEY_W:
                    callbackAction(action, 2);
                    break;
                case GLFW_KEY_S:
                case GLFW_KEY_DOWN:
                    callbackAction(action, 3);
                    break;
                case GLFW_KEY_A:
                case GLFW_KEY_LEFT:
                    callbackAction(action, 4);
                    break;
                case GLFW_KEY_D:
                case GLFW_KEY_RIGHT:
                    callbackAction(action, 5);
                    break;
                case GLFW.GLFW_KEY_LEFT_SHIFT:
                    callbackAction(action, 6);
                    break;
                case GLFW.GLFW_KEY_BACKSPACE:
                    callbackAction(action, 7);
                    break;
                case GLFW.GLFW_KEY_ENTER:
                    callbackAction(action, 8);
                    break;
                case GLFW.GLFW_KEY_1:
                    callbackAction(action, 10);
                    break;
                case GLFW.GLFW_KEY_2:
                    callbackAction(action, 11);
                    break;
                case GLFW.GLFW_KEY_3:
                    callbackAction(action, 12);
                    break;
                case GLFW.GLFW_KEY_ESCAPE:
                    callbackAction(action, 13);
                    break;
            }
        });
    }
    private static void callbackAction(int action, int i) {
        isNew[i] = (action == GLFW_PRESS);
        isDown[i] = (action != GLFW_RELEASE);
    }
}
