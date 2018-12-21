package com.bestialMania;

import com.bestialMania.object.gui.Object2D;
import org.joml.Matrix4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import com.bestialMania.rendering.*;
import com.bestialMania.rendering.shader.*;
import com.bestialMania.rendering.model.*;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    // The window handle
    private long window;
    //Input handler
    private InputHandler inputHandler;

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

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        /*glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });*/

        //create the input handler for the window
        inputHandler = new InputHandler(window);

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
        glClearColor(0.1f, 0.75f, 1.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_CULL_FACE);
        //glCullFace(GL_FRONT);//TODO change to GL_BACK after implementing the projection matrix

        //TODO some basic lighting
        //Vector3f lightDir = new Vector3f(-1,2,1);

        //load some shader
        Shader shader = new Shader("res/shaders/test_vertex.glsl","res/shaders/test_fragment.glsl");
        shader.bindTextureUnits(Arrays.asList("textureSampler"));
        Shader shader2D = new Shader("res/shaders/gui_vertex.glsl","res/shaders/gui_fragment.glsl");

        //Framebuffer for the 3D scene
        Framebuffer sceneFbo = new Framebuffer(DisplaySettings.WIDTH,DisplaySettings.HEIGHT,1,true);


        //jimmy
        Texture jimmyTexture = Texture.loadImageTexture3D("res/textures/jimmy_tex.png");
        Model jimmyModel = OBJLoader.loadOBJ("res/models/jimmy.obj");
        //jimmy matrix
        Matrix4f jimmyMatrix = new Matrix4f();
        jimmyMatrix.translate(0,-0.5f,0.0f);
        jimmyMatrix.scale(0.1f,0.1f,0.1f);

        //Master renderer, does all the scene rendering
        MasterRenderer masterRenderer = new MasterRenderer();

        masterRenderer.addFramebuffer(sceneFbo);

        //create renderer which will render within the window with the shader created above
        Renderer renderer = sceneFbo.createRenderer(shader);
        Renderer renderer2D = masterRenderer.getWindowFramebuffer().createRenderer(shader2D);

        //an object
        ShaderObject object = renderer.createObject(jimmyModel);
        object.addTexture(0,jimmyTexture);
        object.addUniform(new UniformMatrix4(shader,"modelMatrix",jimmyMatrix));

        //The scene renderered as a quad
        Object2D sceneObject = new Object2D(0,0,sceneFbo.getTexture(0));
        sceneObject.addToRenderer(renderer2D);

        //#############################
        //some 2d rectangle
        Object2D object2D = new Object2D(0,100,"res/textures/sexy.png");
        object2D.addToRenderer(renderer2D);
        //#############################



        //Run until you click X or press ESC
        while ( !glfwWindowShouldClose(window) && glfwGetKey(window,GLFW_KEY_ESCAPE) != GLFW_PRESS) {

            System.out.println(inputHandler.getMousePosition());

            inputHandler.updateControllerState();

            //update jimmy
            if(inputHandler.isControllerActive(GLFW_JOYSTICK_1)) {
                System.out.println("controller 1 active");
                jimmyMatrix.rotateY(inputHandler.gamepadLeftJoystickPosition(GLFW_JOYSTICK_1).x/100.0f);
            }else{
                jimmyMatrix.rotateY(0.01f);
            }

            //render the scene
            masterRenderer.render();

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