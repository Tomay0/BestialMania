package com.bestialMania;

import com.bestialMania.state.State;
import com.bestialMania.state.menu.Menu;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;// The window handle

    private InputHandler inputHandler;//Input handler
    private State currentState;
    private boolean running = true;

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
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);

        // Configure the window
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT,GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_DEPTH_BITS,32);
        if(DisplaySettings.ANTIALIASING) glfwWindowHint(GLFW_SAMPLES,DisplaySettings.SAMPLES);
        glfwWindowHint(GLFW_RESIZABLE,GLFW_FALSE);

        // Create the window
        window = glfwCreateWindow(DisplaySettings.WIDTH, DisplaySettings.HEIGHT, "Happiness is an illusion", DisplaySettings.FULLSCREEN ? monitor : NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");


        //set window position
        if(!DisplaySettings.FULLSCREEN) glfwSetWindowPos(window,
                (vidmode.width() - DisplaySettings.WIDTH) / 2,
                (vidmode.height() - DisplaySettings.HEIGHT) / 2);

        //CONTEXT CURRENT
        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);// VSync

        glfwShowWindow(window);
        GL.createCapabilities();

        //create the input handler for the window
        inputHandler = new InputHandler(window);

        //initial game state
        Menu menu = new Menu(this,inputHandler);
        menu.setCurrentState(Menu.MenuState.PLAYER_SELECT);

        //some OpenGL settings
        glClearColor(0.1f, 0.75f, 1.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);//TODO change to GL_BACK after implementing the projection matrix
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Main run loop
     */
    private void loop() {

        //Run until you click X or press ESC
        while ( !glfwWindowShouldClose(window) && glfwGetKey(window,GLFW_KEY_ESCAPE) != GLFW_PRESS && running) {
            inputHandler.update();
            currentState.update();
            currentState.render();

            //swap buffers to show new frame
            glfwSwapBuffers(window);

            //window events
            glfwPollEvents();
        }
    }

    /**
     * Exit the game
     */
    public void quit() {
        running = false;
    }

    /**
     * Runs when the window is closed
     */
    private void terminate() {
        currentState.cleanUp();//delete things from the current state
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    /**
     * Get the current state
     */
    public State getCurrentState(){
        return currentState;
    }

    /**
     * Change the current state
     */
    public void setCurrentState(State newState){
        currentState = newState;
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }
}