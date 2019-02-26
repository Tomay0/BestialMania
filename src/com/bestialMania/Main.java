package com.bestialMania;

import com.bestialMania.state.State;
import com.bestialMania.state.menu.Menu;
import org.lwjgl.glfw.*;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC11.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    public static boolean AUDIO = true;//if audio does not load this will be set to false
    public static final double TICKS_PER_SECOND = 60;//constant tick rate at which the game updates, independent of the render time.
    private long window;// The window handle
    private long audioDevice, audioContext;

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
        if(Settings.ANTIALIASING) glfwWindowHint(GLFW_SAMPLES, Settings.SAMPLES);
        glfwWindowHint(GLFW_RESIZABLE,GLFW_FALSE);

        // Create the window
        if(Settings.FULLSCREEN) {//note that in fullscreen the width/height is adjusted to your monitor's resolution
            Settings.WIDTH = vidmode.width();
            Settings.HEIGHT = vidmode.height();
        }
        window = glfwCreateWindow(Settings.WIDTH,Settings.HEIGHT,"Bestial Mania", Settings.FULLSCREEN ? monitor : NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");


        //set window position
        if(!Settings.FULLSCREEN) glfwSetWindowPos(window,
                (vidmode.width() - Settings.WIDTH) / 2,
                (vidmode.height() - Settings.HEIGHT) / 2);

        //CONTEXT CURRENT
        glfwMakeContextCurrent(window);

        glfwSwapInterval(Settings.VSYNC ? 1 : 0);// VSync

        glfwShowWindow(window);
        GL.createCapabilities();

        //OpenAL (audio)
        //some init code they tell you to use on the github
        if(AUDIO) {
            try {
                String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
                audioDevice = alcOpenDevice(defaultDevice);

                int[] attributes = {0};
                audioContext = alcCreateContext(audioDevice, attributes);
                alcMakeContextCurrent(audioContext);

                ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
                ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

                if (!alCapabilities.OpenAL10) {
                    System.err.println("Open AL is not supported!");
                    AUDIO = false;
                }
            }catch(Exception e) {
                System.err.println("Could not load audio");
                e.printStackTrace();
                AUDIO = false;
            }
        }


        //create the input handler for the window
        inputHandler = new InputHandler(window);

        //initial game state
        Menu menu = new Menu(this,inputHandler);
        menu.setCurrentState(Menu.MenuState.MAIN_MENU);


        //some OpenGL settings
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //some OpenAL settings
        if(AUDIO) {

            alDistanceModel(AL_EXPONENT_DISTANCE_CLAMPED);//clamped = gain doesn't get higher than 1
            alListener3f(AL_POSITION,0,0,0);//doesn't change
            alListener3f(AL_VELOCITY,0,0,0);
            float[] orientation = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
            alListenerfv(AL_ORIENTATION, orientation);
        }

    }

    /**
     * Main run loop
     */
    private void loop() {
        double prevTime = glfwGetTime();

        double delta = 0;
        double delta2 = 0;
        double timeInterval = 1.0f/TICKS_PER_SECOND;

        int fps = 0;

        //Run until you click X or press ESC
        while ( !glfwWindowShouldClose(window) && running) {
            double currentTime = glfwGetTime();
            double timeChange = currentTime-prevTime;
            delta += timeChange;
            delta2 += timeChange;

            //update game at fixed time interval
            while(delta > timeInterval) {
                inputHandler.update();
                currentState.update();
                delta-=timeInterval;
            }

            //render at speed your PC is capable of, for faster machines use a frame interpolation amount.
            currentState.render((float) (delta/timeInterval));
            //calculate fps
            fps++;
            if(delta2 > 1) {
                System.out.println(fps + "FPS");
                fps = 0;
                delta2--;
            }

            //swap buffers to show new frame
            glfwSwapBuffers(window);

            //window events
            glfwPollEvents();

            prevTime = currentTime;
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

        if(AUDIO) {
            alcDestroyContext(audioContext);
            alcCloseDevice(audioDevice);
        }

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