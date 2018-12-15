import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import rendering.Shader;
import rendering.Texture;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // The window handle
    private long window;

    public void run() {
        init();
        loop();
        terminate();
    }

    /**
     * Initialize GLFW/OpenGL
     */
    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        //GLFWErrorCallback.createPrint(System.err).set();

        //Init GLFW
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        //details about the monitor you are using
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        // Configure the window
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        int width = 640;
        int height = 480;
        // Create the window
        window = glfwCreateWindow(width, height, "Happiness is an illusion", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");


        //set window position
        glfwSetWindowPos(window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2);

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        /*glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });*/

        //CONTEXT CURRENT
        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);// VSync

        glfwShowWindow(window);
        GL.createCapabilities();
    }

    /**
     * Main run loop
     */
    private void loop() {
        //background colour
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        //load some shader
        Shader shader = new Shader("res/shaders/test_vertex.glsl","res/shaders/test_fragment.glsl");
        Texture texture = new Texture("res/textures/sexy.png");
        Rect2D obj = new Rect2D(-0.5f,-0.5f,0.5f,0.5f,texture);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) && glfwGetKey(window,GLFW_KEY_ESCAPE) != GLFW_PRESS) {

            //clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            shader.bind();
            obj.draw();


            //swap buffers to show new frame
            glfwSwapBuffers(window);

            //window events
            glfwPollEvents();
        }
    }

    /**
     * Runs when the window is closed
     */
    private void terminate() {
        //glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new Main().run();
    }

}