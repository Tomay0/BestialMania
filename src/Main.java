import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import rendering.shader.Shader;
import rendering.Texture;
import rendering.model.Model;
import rendering.model.OBJLoader;
import rendering.shader.Uniform;
import rendering.shader.UniformMatrix4;
import rendering.shader.UniformTexture;

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
        glClearColor(0.1f, 0.75f, 1.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);//TODO change to GL_BACK after implementing the projection matrix

        //TODO some basic lighting
        //Vector3f lightDir = new Vector3f(-1,2,1);

        //load some shader
        Shader shader = new Shader("res/shaders/test_vertex.glsl","res/shaders/test_fragment.glsl");

        //daddy
        Texture texture = new Texture("res/textures/sexy.png");
        Rect2D obj = new Rect2D(-0.5f,-0.5f,0.5f,0.5f,texture);

        //daddy matrix
        //Matrix4f m = new Matrix4f();
        //m.identity();
        //UniformMatrix4 uniform1 = new UniformMatrix4(shader,"modelMatrix",m);

        //jimmy
        Texture t2 = new Texture("res/textures/jimmy_tex.png");
        Model m1 = OBJLoader.loadOBJ("res/models/jimmy.obj");

        //jimmy matrix
        Matrix4f m2 = new Matrix4f();
        m2.translate(0,-0.5f,0.0f);
        m2.scale(0.1f,0.1f,0.1f);

        UniformMatrix4 uniform2 = new UniformMatrix4(shader,"modelMatrix",m2);
        UniformTexture texture1 = new UniformTexture(shader,"textureSampler",0,t2);//more flexible way of binding textures

        //Run until you click X or press ESC
        while ( !glfwWindowShouldClose(window) && glfwGetKey(window,GLFW_KEY_ESCAPE) != GLFW_PRESS) {

            //clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            //bind the shader
            shader.bind();

            //draw daddy
            //uniform1.bindUniform();
            //obj.draw();

            //update jimmy
            m2.rotateY(0.01f);

            //draw jimmy
            uniform2.bindUniform();
            texture1.bindUniform();
            m1.draw();

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