package com.bestialMania.state;

import com.bestialMania.DisplaySettings;
import com.bestialMania.InputHandler;
import com.bestialMania.InputListener;
import com.bestialMania.Main;
import com.bestialMania.object.beast.Beast;
import com.bestialMania.object.beast.Player;
import com.bestialMania.object.gui.Object2D;
import com.bestialMania.rendering.*;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.model.OBJLoader;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.state.menu.Menu;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Test class reflecting the actual game.
 * TODO:
 * Expand this in the future to allow for multiple maps and have 1 class that deals with all
 * the tracking of the players in the game and their respective lives/scores.
 */
public class Game implements State, InputListener {
    private Main main;
    private InputHandler inputHandler;
    private MasterRenderer masterRenderer;
    private MemoryManager memoryManager;

    //Renderers
    private Renderer testRenderer, renderer2D;


    //players in the game
    private Player player;

    /**
     * Initialize a game
     */
    public Game(Main main, InputHandler inputHandler, int player1controller) {
        this.main = main;
        this.inputHandler = inputHandler;
        masterRenderer = new MasterRenderer();
        memoryManager = new MemoryManager();

        //add this class as a listener
        inputHandler.addListener(this);
        inputHandler.setCursorDisabled();

        //set up shaders
        Shader testShader = new Shader("res/shaders/test_vertex.glsl","res/shaders/test_fragment.glsl");
        testShader.bindTextureUnits(Arrays.asList("textureSampler"));

        //projection matrix
        Matrix4f projection = new Matrix4f();
        projection.perspective(70,(float)DisplaySettings.WIDTH/(float)DisplaySettings.HEIGHT,0.1f,100);
        testShader.setUniformMatrix4(testShader.getUniformLocation("projectionMatrix"),projection);

        Shader shader2D = new Shader("res/shaders/gui_vertex.glsl","res/shaders/gui_fragment.glsl");
        shader2D.bindTextureUnits(Arrays.asList("textureSampler"));

        //Framebuffer for the 3D scene
        Framebuffer sceneFbo;
        if(DisplaySettings.ANTIALIASING) {
            sceneFbo = Framebuffer.createMultisampledFramebuffer3Dto2D(memoryManager,DisplaySettings.WIDTH, DisplaySettings.HEIGHT);

        }else{
            sceneFbo = Framebuffer.createFramebuffer3Dto2D(memoryManager,DisplaySettings.WIDTH, DisplaySettings.HEIGHT);
        }
        masterRenderer.addFramebuffer(sceneFbo);

        //create renderers
        testRenderer = sceneFbo.createRenderer(testShader);
        renderer2D = masterRenderer.getWindowFramebuffer().createRenderer(shader2D);

        //The scene renderered as a quad
        Object2D sceneObject = new Object2D(memoryManager,0,0,sceneFbo.getTexture(0));
        sceneObject.addToRenderer(renderer2D);

        /*


            THE PLAYER
            - only player 1 at the moment


         */

        //create the beast you play as (JIMMY)
        Texture jimmyTexture = Texture.loadImageTexture3D(memoryManager,"res/textures/jimmy_tex.png");
        Model jimmyModel = OBJLoader.loadOBJ(memoryManager,"res/models/jimmy.obj");
        Beast beast = new Beast(jimmyModel,jimmyTexture);
        beast.linkToRenderer(testRenderer);

        //link beast to a player object
        player = new Player(inputHandler,1,player1controller,beast);
        player.linkToRenderer(testRenderer);



        //test object so you can see movement
        Matrix4f testObjectMatrix = new Matrix4f();
        testObjectMatrix.translate(2.0f,0,0.5f);
        testObjectMatrix.scale(0.1f,0.1f,0.1f);

        ShaderObject testObject = testRenderer.createObject(jimmyModel);
        testObject.addTexture(0,jimmyTexture);
        testObject.addUniform(new UniformMatrix4(testShader,"modelMatrix",testObjectMatrix));
    }

    /**
     * Key press
     */
    @Override
    public void keyEvent(boolean pressed, int key) {
        //go to main menu on ESC press
        //TODO: make this a pause menu instead
        if(pressed && key==GLFW_KEY_ESCAPE) {
            cleanUp();
            Menu menu = new Menu(main,inputHandler);
            menu.setCurrentState(Menu.MenuState.MAIN_MENU);
        }
    }

    /**
     * Update the game
     */
    @Override
    public void update() {
        player.update();
    }

    /**
     * Render
     */
    @Override
    public void render() {
        masterRenderer.render();
    }

    /**
     * Clean up all memory and listeners
     */
    @Override
    public void cleanUp() {
        inputHandler.setCursorEnabled();
        inputHandler.removeListener(this);
        memoryManager.cleanUp();
    }

    //unneeded most likely as all remove will be in cleanUp
    @Override
    public void removeObjects() { }

    @Override
    public void mouseEvent(boolean pressed, int button) {}

    @Override
    public void controllerEvent(int controller, boolean pressed, int button) {}
}
