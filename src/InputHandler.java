import static org.lwjgl.glfw.GLFW.*;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWGamepadState;
import java.nio.DoubleBuffer;
import java.util.HashSet;

/**
 *
 */
public class InputHandler{

    //fields
    private long window;
    private HashSet<Integer> activeControllers = new HashSet<>();
    private GLFWGamepadState[] gamepadStates = new GLFWGamepadState[4];

    /*
    Constructor
    Sets up the window
     */
    public InputHandler(long windowID){
        window = windowID;
    }

    /*
        MOUSE STUFF
     */
    private DoubleBuffer mouseXPos = BufferUtils.createDoubleBuffer(1);
    private DoubleBuffer mouseYPos = BufferUtils.createDoubleBuffer(1);

    //get current mouse position
    public Vector2f getMousePosition(){
        mouseXPos.rewind();
        mouseYPos.rewind();
        glfwGetCursorPos(window, mouseXPos, mouseYPos);
        mouseXPos.clear();
        mouseYPos.clear();
        return new Vector2f( (float)mouseXPos.get(), (float)mouseYPos.get() );
    }

    //set the mouse position to unbounded (for camera movement) and hides the cursor
    public void setCursorDisabled(){ glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED); }

    //set the mouse position to bounded (for menus etc) and shows the cursor
    public void setCursorEnabled(){ glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL); }

    // return bool if left mouse button pressed
    public boolean isMouseLeftPressed(){ return(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT)==GLFW_PRESS); }

    // return bool if right mouse button pressed
    public boolean isMouseRightPressed(){ return(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT)==GLFW_PRESS); }

    /*
        KEYBOARD STUFF
     */
    //return bool if key pressed
    public boolean isKeyPressed(int key){ return(glfwGetKey(window,key)==GLFW_PRESS); }


    /*
        GAMEPAD STUFF (controllers)
     */
    //add a present controller to list of active controllers. (to check for accidental disconnect)(only 4 player)
    public void addControllersAndPlayers(){
        if(glfwJoystickIsGamepad(GLFW_JOYSTICK_1)){
            activeControllers.add(GLFW_JOYSTICK_1);
            gamepadStates[0] = new GLFWGamepadState(BufferUtils.createByteBuffer(40));
        }
        if(glfwJoystickIsGamepad(GLFW_JOYSTICK_2)){
            activeControllers.add(GLFW_JOYSTICK_2);
            gamepadStates[1] = new GLFWGamepadState(BufferUtils.createByteBuffer(40));
        }
        if(glfwJoystickIsGamepad(GLFW_JOYSTICK_3)){
            activeControllers.add(GLFW_JOYSTICK_3);
            gamepadStates[2] = new GLFWGamepadState(BufferUtils.createByteBuffer(40));
        }
        if(glfwJoystickIsGamepad(GLFW_JOYSTICK_4)){
            activeControllers.add(GLFW_JOYSTICK_4);
            gamepadStates[3] = new GLFWGamepadState(BufferUtils.createByteBuffer(40));
        }
    }

    // remove a joystick from list of active controllers (for if player quit) and clear the controller's states
    public void removeController(int joystickId){
        activeControllers.remove(joystickId);
        gamepadStates[joystickId] = null;
    }

    // returns an array of booleans based on buttons pressed or released
    public void updateControllerState(){

        //update all active GamePad states
        for(int joystickId : activeControllers) {

            //throw exception if joystick was being used but ungracefully disconnected
            if (!glfwJoystickPresent(joystickId)) {
                throw new RuntimeException("Joystick id: " + joystickId + " Is disconnected!");
            }

            //update the gamepad state
            glfwGetGamepadState(joystickId, gamepadStates[joystickId]);
        }
    }

    //Returns boolean if a button is pressed for a given joystick
    public boolean isGamepadButtonPressed(int joystickId, int button){
        if(!activeControllers.contains(joystickId)){
            throw new RuntimeException("joystickID is not in activeControllers!");
        }
        if(button < 0 || button > 14){
            throw new RuntimeException("Button to check is not recognised!");
        }
        //return gamepadButtonStates[joystickId].get(button)==GLFW_PRESS;
        return gamepadStates[joystickId].buttons().get(button) == GLFW_PRESS;
    }

    //return vector with left gamepad joystick position
    public Vector2f gamepadLeftJoystickPosition(int joystickId){
        if(!activeControllers.contains(joystickId)){
            throw new RuntimeException("joystickID is not in activeControllers!");
        }
        float x = gamepadStates[joystickId].axes().get(0);
        float y = gamepadStates[joystickId].axes().get(1);
        return new Vector2f(x, y);
    }

    //return vector with right gamepad joystick position
    public Vector2f gamepadRightJoystickPosition(int joystickId){
        if(!activeControllers.contains(joystickId)){
            throw new RuntimeException("joystickID is not in activeControllers!");
        }
        float x = gamepadStates[joystickId].axes().get(2);
        float y = gamepadStates[joystickId].axes().get(3);
        return new Vector2f(x, y);
    }

    //returns float position of gamepad Left trigger
    public float gamepadLeftTriggerPosition(int joystickId){
        if(!activeControllers.contains(joystickId)){
            throw new RuntimeException("joystickID is not in activeControllers!");
        }
        return gamepadStates[joystickId].axes().get(4);
    }

    //returns float position of gamepad right trigger
    public float gamepadRightTriggerPosition(int joystickId){
        if(!activeControllers.contains(joystickId)){
            throw new RuntimeException("joystickID is not in activeControllers!");
        }
        return gamepadStates[joystickId].axes().get(5);
    }

    public HashSet<Integer> getActiveControllers() { return activeControllers; }
}