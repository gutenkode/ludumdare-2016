package scenes;

import mote4.scenegraph.Scene;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import nullset.Const;
import nullset.Input;
import terminal.TerminalSession;

/**
 * Renders terminal text on the screen.
 * @author Peter
 */
public class TerminalScene implements Scene {
    
    private static boolean terminalOpen = false;
    private static TerminalSession session;
    private static String INPUT_HEAD = ""; // should be left blank, as programs can also define their own input heads
    
    private StringBuilder input; // TODO, this should be saved as part of a TerminalSession to preserve state
    private String lastInput;
    private Transform trans;
    private int backspaceDelay, // frames to wait while holding down backspace before auto-delete is enabled
                backspaceCooldown, // don't delete a character every single frame
                cursorCooldown; // frame delay for blinking the cursor
    private boolean showCursor = false;
    
    private static boolean closeTerminal;
    
    public TerminalScene() {
        trans = new Transform();
        input = new StringBuilder();
        closeTerminal = false;
    }
    
    @Override
    public void update(double delta) {
        if (Input.currentLock() == Input.Lock.TERMINAL) 
        {
            if (cursorCooldown <= 0) {
                showCursor = !showCursor;
                if (showCursor)
                    cursorCooldown = 10;
                else
                    cursorCooldown = 6;
                updateWriteLine();
            } else
                cursorCooldown--;
            
            if (session.inputActive())
            {
                String s = Input.getTyped();
                if (!s.isEmpty()) 
                {
                    input.append(s);
                    updateWriteLine();
                } 
                else { 
                    if (Input.isKeyDown(Input.Keys.BACKSPACE)) 
                    {
                        backspaceCooldown--;
                        if (Input.isKeyNew(Input.Keys.BACKSPACE) || (backspaceDelay <= 0 && backspaceCooldown <= 0)) {
                            backspaceCooldown = 2;
                            if (input.length() > 0) {
                                input.deleteCharAt(input.length()-1);
                                updateWriteLine();
                            }
                        } else
                            backspaceDelay--;
                    } else
                        backspaceDelay = 15;
                    //if (GLFW.glfwGetKey(Window.getWindowID(), GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS)
                    if (Input.isKeyNew(Input.Keys.ENTER)) 
                    {
                        lastInput = input.toString();
                        session.addCompleteLine(INPUT_HEAD+input.toString());
                        session.parse(input.toString());
                        input = new StringBuilder();
                        updateWriteLine();
                    }
                    if (Input.isKeyNew(Input.Keys.UP_ARROW))
                    {
                        if (lastInput != null) {
                            input = new StringBuilder();
                            input.append(lastInput);
                            updateWriteLine();
                        }
                    }
                }
            } else {
                Input.getTyped();
            }
            if (Input.isKeyNew(Input.Keys.ESC))
                closeTerminal();
            
            if (closeTerminal) {
                closeTerminal();
                closeTerminal = false;
            }
        }
    }
    private void updateWriteLine() {
        if (showCursor)
            session.setWriteLine(INPUT_HEAD+input.toString()+(char)(16*14-5));
        else
            session.setWriteLine(INPUT_HEAD+input.toString());
    }

    @Override
    public void render(double delta) {
        if (Input.currentLock() == Input.Lock.TERMINAL) 
        {
            ShaderMap.use("texture");
            TextureMap.bindUnfiltered("font_terminal");
            trans.model.setIdentity();
            trans.makeCurrent();
            session.render(trans.model);
        }
    }

    @Override
    public void framebufferResized(int width, int height) {
        trans.projection.setOrthographic(0, 0, width, height, -1, 1);
        trans.view.setIdentity();
        trans.view.translate(width/2-200,Const.UI_SCALE/2);
    }

    @Override
    public void destroy() {}
    
    /**
     * Displays a terminal interface with the given session.
     * @param s 
     */
    public static void openTerminal(TerminalSession s) {
        if (!terminalOpen) {
            Postprocess.setDOFCoef(-1);
            session = s;
            Input.pushLock(Input.Lock.TERMINAL);
            Input.recordTyped(true);
            terminalOpen = true;
        }
    }
    /**
     * Closes the terminal interface.  The session is not destroyed, and is
     * maintained in the Terminal map entity.
     */
    public static void closeTerminal() {
        if (terminalOpen) {
            Postprocess.setDOFCoef(0);
            //session.destroy();
            session = null;
            Input.popLock();
            Input.recordTyped(false);
            terminalOpen = false;
        }
    }
    
    /**
     * Alternate version of closeTerminal that should be called from Programs.
     * This version will not cause crashes :P
     */
    public static void closeTerminalFromProgram() {
        closeTerminal = true;
    }
    
}
