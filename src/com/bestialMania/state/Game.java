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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;

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
    private Renderer renderer2D;

    //players in the game
    private List<Player> players = new ArrayList<>();

    private List<Renderer> renderers = new ArrayList<>();


    /**
     * Initialize a game
     */
    public Game(Main main, InputHandler inputHandler, List<Integer> players) {
        this.main = main;
        this.inputHandler = inputHandler;
        masterRenderer = new MasterRenderer();
        memoryManager = new MemoryManager();

        int windowWidth = players.size()<3 ? DisplaySettings.WIDTH : DisplaySettings.WIDTH/2;
        int windowHeight =  players.size()==1 ? DisplaySettings.HEIGHT : DisplaySettings.HEIGHT/2;

        //add this class as a listener
        inputHandler.addListener(this);
        inputHandler.setCursorDisabled();

        //set up shaders
        Shader testShader = new Shader("res/shaders/test_vertex.glsl","res/shaders/test_fragment.glsl");
        testShader.bindTextureUnits(Arrays.asList("textureSampler"));
        //lighting
        testShader.setUniformVector3f(testShader.getUniformLocation("lightDirection"),new Vector3f(-0.86f, -0.5f, 0.1f).normalize());
        testShader.setUniformVector3f(testShader.getUniformLocation("lightColor"),new Vector3f(1.0f, 1.0f, 1.0f));

        //projection matrix
        Matrix4f projection = new Matrix4f();
        projection.perspective(70,(float)windowWidth/(float)windowHeight,0.1f,100);
        testShader.setUniformMatrix4(testShader.getUniformLocation("projectionMatrix"),projection);

        Shader shader2D = new Shader("res/shaders/gui_vertex.glsl","res/shaders/gui_fragment.glsl");
        shader2D.bindTextureUnits(Arrays.asList("textureSampler"));


        //2d renderer
        renderer2D = masterRenderer.getWindowFramebuffer().createRenderer(shader2D);

        //create the beast you play as (JIMMY)
        Texture jimmyTexture = Texture.loadImageTexture3D(memoryManager,"res/textures/jimmy_tex.png");
        Model jimmyModel = OBJLoader.loadOBJ(memoryManager,"res/models/jimmy.obj");


        //Create a window, renderer and character for each player
        for(int i = 0;i<players.size();i++) {

            //framebuffer
            Framebuffer fbo = createPlayerWindow(windowWidth, windowHeight);
            fbo.setBackgroundColor(new Vector3f((float)Math.random(),(float)Math.random(),(float)Math.random()));
            masterRenderer.addFramebuffer(fbo);

            //renderer
            Renderer renderer = fbo.createRenderer(testShader);
            renderers.add(renderer);

            //rectangle on screen as the splitscreen window
            Object2D sceneObject = new Object2D(memoryManager,
                    players.size()>2 && i%2==1 ? DisplaySettings.WIDTH/2 : 0,
                    (players.size()==2 && i==1) || i>1 ? DisplaySettings.HEIGHT/2 : 0,
                    fbo.getTexture(0));
            sceneObject.addToRenderer(renderer2D);

            //the player object
            Beast beast = new Beast(jimmyModel,jimmyTexture);
            Player player = new Player(inputHandler,i+1,players.get(i),beast);
            player.linkToRenderer(renderer);

            this.players.add(player);
        }

        //link all beast's to all renderers
        for(Player player : this.players) {
            for(Renderer renderer : renderers) {
                player.getBeast().linkToRenderer(renderer);
            }
        }

        //test object in 1 player mode
        if(players.size()==1) {
            Matrix4f testObjectMatrix = new Matrix4f();
            testObjectMatrix.translate(2.0f,0,0.5f);
            testObjectMatrix.scale(0.1f,0.1f,0.1f);

            for(Renderer renderer : renderers) {
                ShaderObject testObject = renderer.createObject(jimmyModel);
                testObject.addTexture(0,jimmyTexture);
                testObject.addUniform(new UniformMatrix4(testShader,"modelMatrix",testObjectMatrix));
            }
        }

    }

    /**
     * Create a player window framebuffer
     */
    private Framebuffer createPlayerWindow(int width, int height) {
        Framebuffer fbo;
        if(DisplaySettings.ANTIALIASING) {
            fbo = Framebuffer.createMultisampledFramebuffer3Dto2D(memoryManager,width, height);

        }else{
            fbo = Framebuffer.createFramebuffer3Dto2D(memoryManager,width,height);
        }
        return fbo;
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
        for(Player player : players) {
            player.update();
        }
    }

    /**
     * Render
     */
    @Override
    public void render(float frameInterpolation) {
        for(Player player : players) {
            player.interpolate(frameInterpolation);
        }
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
