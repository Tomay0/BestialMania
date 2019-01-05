package com.bestialMania;

import static org.lwjgl.glfw.GLFW.*;

import com.bestialMania.object.gui.Button;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWJoystickCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.awt.event.ActionListener;
import java.nio.DoubleBuffer;
import java.util.*;

public class InputHandler{
    private static final int N_BUTTONS = 14;//number of buttons on a controller to check
    private static final int N_CONTROLLERS = 4;//number of controller slots available

    //fields
    private long window;
    private Set<Integer> activeControllers = new HashSet<>();
    private GLFWGamepadState[] gamepadStates = new GLFWGamepadState[N_CONTROLLERS];
    private boolean[][] gamepadButtonStates = new boolean[N_CONTROLLERS][N_BUTTONS];//button states. true = pressed (Only have this because controller events aren't a thing in GLFW

    //mouse
    private DoubleBuffer mouseXPos = BufferUtils.createDoubleBuffer(1);
    private DoubleBuffer mouseYPos = BufferUtils.createDoubleBuffer(1);

    //listeners for the inputs
    private Set<InputListener> listeners = new HashSet<>();


    /**
    Constructor
    Sets up the window
     */
    public InputHandler(long windowID){
        window = windowID;
        //intialize gamepad state buffers
        for(int i = 0;i<N_CONTROLLERS;i++) {
            gamepadStates[i] = new GLFWGamepadState(BufferUtils.createByteBuffer(40));
        }


        //apply key events
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(action==GLFW_PRESS || action==GLFW_RELEASE) {
                    for(InputListener listener : listeners) {
                        listener.keyEvent(action==GLFW_PRESS,key);
                    }
                }
            }
        });

        //apply mouse events
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if(action==GLFW_PRESS || action==GLFW_RELEASE) {
                    for(InputListener listener : listeners) {
                        listener.mouseEvent(action==GLFW_PRESS,button);
                    }
                }
            }
        });

        //controller events had to be done manually :(
    }

    /*


        LISTENERS


     */

    /**
     * Adds an input listener
     */
    public void addListener(InputListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an input listener
     */
    public void removeListener(InputListener listener) {
        listeners.remove(listener);
    }
    /*


        MOUSE


     */

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

    // return bool if left mouse button held
    public boolean isMouseLeftPressed(){ return(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT)==GLFW_PRESS);}


    // return bool if right mouse button held
    public boolean isMouseRightPressed(){ return(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT)==GLFW_PRESS); }

    /*


        KEYBOARD


     */


    //return bool if key pressed
    public boolean isKeyPressed(int key){ return(glfwGetKey(window,key)==GLFW_PRESS); }


    /*


        GAMEPAD (controllers)


     */

    /** updates controllers
     *
      */
    public void update(){
        activeControllers.clear();
        //update button states
        for(int controller = 0;controller<N_CONTROLLERS;controller++) {
            if(glfwJoystickIsGamepad(controller)) {
                activeControllers.add(controller);

                //update the gamepad state
                glfwGetGamepadState(controller, gamepadStates[controller]);

                //check for button press/release events
                for(int button = 0;button<N_BUTTONS;button++) {
                    //press
                    if(isGamepadButtonPressed(controller,button)) {
                        if(!gamepadButtonStates[controller][button]) {
                            gamepadButtonStates[controller][button] = true;
                            //call event
                            for(InputListener listener : listeners) {
                                listener.controllerEvent(controller,true,button);
                            }
                        }
                    }
                    //release
                    else {
                        if(gamepadButtonStates[controller][button]) {
                            gamepadButtonStates[controller][button] = false;
                            //call event
                            for(InputListener listener : listeners) {
                                listener.controllerEvent(controller,false,button);
                            }

                        }
                    }
                }
            }
        }
    }

    //Returns boolean if a button is pressed for a given joystick
    public boolean isGamepadButtonPressed(int joystickId, int button){
        if(!activeControllers.contains(joystickId)){
            throw new RuntimeException("joystickID is not in activeControllers!");
        }
        if(button < 0 || button > N_BUTTONS){
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

    public Set<Integer> getActiveControllers() { return activeControllers; }

    public boolean isControllerActive(int joystickId) {return activeControllers.contains(joystickId);}
}