import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

/**
 *
 */
public class InputHandler{

    //fields
    private long window;

    /*
    Constructor
     */
    public InputHandler(long windowID){
        window = windowID;
    }

   /*
        return bool if key pressed
    */
    public boolean isKeyPressed(int key){
            return(glfwGetKey(window,key)==GLFW_PRESS);
    }

    /*
        return bool if key released
     */
    public boolean isKeyReleased(int key){
        return(glfwGetKey(window,key)==GLFW_RELEASE);
    }
}