import static org.lwjgl.glfw.GLFW.*;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWGamepadState;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 */
public class InputHandler{

    //fields
    private long window;
    private HashSet<Integer> activeControllers = new HashSet<>();
    ArrayList<ByteBuffer>gamepadButtonStates = new ArrayList<>();
    ArrayList<FloatBuffer>gamepadAxisStates = new ArrayList<>();

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
            gamepadAxisStates.add(0, BufferUtils.createFloatBuffer(6));
            gamepadButtonStates.add(0, BufferUtils.createByteBuffer(15));
        }
        if(glfwJoystickIsGamepad(GLFW_JOYSTICK_2)){
            activeControllers.add(GLFW_JOYSTICK_2);
            gamepadAxisStates.add(1, BufferUtils.createFloatBuffer(6));
            gamepadButtonStates.add(1, BufferUtils.createByteBuffer(15));
        }
        if(glfwJoystickIsGamepad(GLFW_JOYSTICK_3)){
            activeControllers.add(GLFW_JOYSTICK_3);
            gamepadAxisStates.add(2, BufferUtils.createFloatBuffer(6));
            gamepadButtonStates.add(2, BufferUtils.createByteBuffer(15));
        }
        if(glfwJoystickIsGamepad(GLFW_JOYSTICK_4)){
            activeControllers.add(GLFW_JOYSTICK_4);
            gamepadAxisStates.add(3, BufferUtils.createFloatBuffer(6));
            gamepadButtonStates.add(3, BufferUtils.createByteBuffer(15));
        }
    }

    // remove a joystick from list of active controllers (for if player quit) and clear the controller's states
    public void removeController(int joystickId){
        activeControllers.remove(joystickId);
        gamepadAxisStates.add(joystickId, null);
        gamepadButtonStates.add(joystickId, null);
    }

    // returns an array of booleans based on buttons pressed or released
    public void updateControllerState(int joystickId){
        // throw exception if joystick is not one plugged in
        if(!activeControllers.contains(joystickId)){
            throw new RuntimeException("Joystick id: " + joystickId + " Is not an Active controller!");
        }

        //throw exception if joystick was being used but ungracefully disconnected
        if(!glfwJoystickPresent(joystickId)){
            throw new RuntimeException("Joystick id: " + joystickId + " Is disconnected!");
        }
        //add the input buffers to the arrayLists
        gamepadButtonStates.add(joystickId, glfwGetJoystickButtons(joystickId));
        gamepadAxisStates.add(joystickId, glfwGetJoystickAxes(joystickId));
    }

    //Returns boolean if a button is pressed for a given joystick
    public boolean isGamepadButtonPressed(int joystickId, String button){
        if(button.equals("A")){ return  gamepadButtonStates.get(joystickId).get(0)==GLFW_PRESS; }
        else if(button.equals("B")){ return  gamepadButtonStates.get(joystickId).get(1)==GLFW_PRESS; }
        else if(button.equals("X")){ return  gamepadButtonStates.get(joystickId).get(2)==GLFW_PRESS; }
        else if(button.equals("Y")){ return  gamepadButtonStates.get(joystickId).get(3)==GLFW_PRESS; }
        else if(button.equals("LEFT_BUMPER")){ return  gamepadButtonStates.get(joystickId).get(4)==GLFW_PRESS; }
        else if(button.equals("RIGHT_BUMPER")){ return  gamepadButtonStates.get(joystickId).get(5)==GLFW_PRESS; }
        else if(button.equals("BACK")){ return  gamepadButtonStates.get(joystickId).get(6)==GLFW_PRESS; }
        else if(button.equals("START")){ return  gamepadButtonStates.get(joystickId).get(7)==GLFW_PRESS; }
        else if(button.equals("GUIDE")){ return  gamepadButtonStates.get(joystickId).get(8)==GLFW_PRESS; }
        else if(button.equals("LEFT_THUMB")){ return  gamepadButtonStates.get(joystickId).get(9)==GLFW_PRESS; }
        else if(button.equals("RIGHT_THUMB")){ return  gamepadButtonStates.get(joystickId).get(10)==GLFW_PRESS; }
        else if(button.equals("DPAD_UP")){ return  gamepadButtonStates.get(joystickId).get(11)==GLFW_PRESS; }
        else if(button.equals("DPAD_RIGHT")){ return  gamepadButtonStates.get(joystickId).get(12)==GLFW_PRESS; }
        else if(button.equals("DPAD_DOWN")){ return  gamepadButtonStates.get(joystickId).get(13)==GLFW_PRESS; }
        else if(button.equals("DPAD_LEFT")){ return  gamepadButtonStates.get(joystickId).get(14)==GLFW_PRESS; }
        else{ throw new RuntimeException("String Button Id not recognised!"); }
    }


    public HashSet<Integer> getActiveControllers() { return activeControllers; }
}