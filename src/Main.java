import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import rendering.MasterRenderer;
import rendering.Renderer;
import rendering.ShaderObject;
import rendering.model.Rect2D;
import rendering.shader.Shader;
import rendering.Texture;
import rendering.model.Model;
import rendering.model.OBJLoader;
import rendering.shader.UniformMatrix4;

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
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);//TODO change to GL_BACK after implementing the projection matrix

        //TODO some basic lighting
        //Vector3f lightDir = new Vector3f(-1,2,1);

        //load some shader
        Shader shader = new Shader("res/shaders/test_vertex.glsl","res/shaders/test_fragment.glsl");
        shader.bindTextureUnits(Arrays.asList("textureSampler"));

        //jimmy
        Texture jimmyTexture = Texture.loadImageTexture3D("res/textures/jimmy_tex.png");
        Model jimmyModel = OBJLoader.loadOBJ("res/models/jimmy.obj");
        //jimmy matrix
        Matrix4f jimmyMatrix = new Matrix4f();
        jimmyMatrix.translate(0,-0.5f,0.0f);
        jimmyMatrix.scale(0.1f,0.1f,0.1f);

        //some 2d rectangle
        Model rect = new Rect2D(-1,-1,0,0);
        Texture daddyTexture = Texture.loadImageTexture3D("res/textures/sexy.png");

        //Master renderer, does all the scene rendering
        MasterRenderer masterRenderer = new MasterRenderer();

        //create renderer which will render within the window with the shader created above
        Renderer renderer = masterRenderer.getWindowFramebuffer().createRenderer(shader);

        //an object
        ShaderObject object = renderer.createObject(jimmyModel);
        object.addTexture(0,jimmyTexture);
        object.addUniform(new UniformMatrix4(shader,"modelMatrix",jimmyMatrix));

        //another object
        ShaderObject object2 = renderer.createObject(rect);
        object2.addTexture(0,daddyTexture);
        object2.addUniform(new UniformMatrix4(shader,"modelMatrix",new Matrix4f()));

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