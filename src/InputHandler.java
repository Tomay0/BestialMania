import static org.lwjgl.glfw.GLFW.*;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.ByteOrder;
import java.nio.DoubleBuffer;


/**
 *
 */
public class InputHandler{

    //fields
    private long window;

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

    // return bool if left mouse button released
    public boolean isMouseLeftReleased(){ return(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT)==GLFW_RELEASE); }

    // return bool if right mouse button pressed
    public boolean isMouseRightPressed(){ return(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT)==GLFW_PRESS); }

    //return bool if right mouse button released
    public boolean isMouseRightReleased(){ return(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT)==GLFW_RELEASE); }

    //

    /*
        KEYBOARD STUFF
     */
    //return bool if key pressed
    public boolean isKeyPressed(int key){ return(glfwGetKey(window,key)==GLFW_PRESS); }

    //return bool if key released
    public boolean isKeyReleased(int key){ return(glfwGetKey(window,key)==GLFW_RELEASE); }


    /*
        GAMEPAD STUFF
     */
}