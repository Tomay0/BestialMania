package com.bestialMania.state.game;

import com.bestialMania.*;
import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.collision.CollisionHandler;
import com.bestialMania.collision.CollisionLoader;
import com.bestialMania.gui.Object2D;
import com.bestialMania.object.AnimatedObject;
import com.bestialMania.object.Object3D;
import com.bestialMania.object.beast.Beast;
import com.bestialMania.object.beast.Player;
import com.bestialMania.rendering.*;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.model.Skybox;
import com.bestialMania.rendering.model.loader.ModelLoader;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.rendering.shadow.ShadowRenderer;
import com.bestialMania.rendering.texture.Texture;
import com.bestialMania.sound.Sound;
import com.bestialMania.sound.SoundSource;
import com.bestialMania.state.State;
import com.bestialMania.state.game.map.MapData;
import com.bestialMania.state.menu.Menu;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private static final float MIN_DIST = 0.01f;
    private static final float FAR_DIST = 120.0f;


    //Shaders
    private Shader shader2D, depthShader, animatedDepthShader;

    //Sound Sources
    private SoundSource musicSource;

    /*
    each of these shaders have 1 renderer per framebuffer.
    0 = skybox renderer
    1 = animated renderer (for characters)
    2+ = reserved for map dependent shaders
     */
    private List<RendererList> rendererLists = new ArrayList<>();

    //Renderers
    private Renderer renderer2D;

    //All objects that update
    List<Object3D> objects = new ArrayList<>();
    List<Object3D> objectsCopy = new ArrayList<>();//avoid concurrent modifications exception

    //players in the game
    private List<Player> players = new ArrayList<>();
    private List<Framebuffer> playerFbos = new ArrayList<>();//player framebuffers

    //shadow boxes
    private List<Float> shadowDistanceValues;
    private ShadowRenderer shadowRenderer;

    //collisions
    private CollisionHandler collisionHandler;

    //some variables
    private boolean normalMapping;
    private Vector3f lightDir, lightColor, ambient;
    private Matrix4f projection;
    private int windowWidth,windowHeight;
    private int shadowResolution, shadowPCFCount;
    private float shadowPCFSpread;

    /**
     * Initialize a game
     */
    public Game(Main main, InputHandler inputHandler, List<Integer> controllers, MapData map) {
        this.main = main;
        this.inputHandler = inputHandler;
        masterRenderer = new MasterRenderer();
        memoryManager = new MemoryManager();

        inputHandler.addListener(this);

        inputHandler.setCursorDisabled();

        windowWidth = controllers.size()<3 ? Settings.WIDTH : Settings.WIDTH/2;
        windowHeight =  controllers.size()==1 ? Settings.HEIGHT : Settings.HEIGHT/2;


        lightDir = map.getLightDirection();
        lightColor = map.getLightColor();
        ambient = map.getAmbientLight();
        projection = new Matrix4f();
        projection.perspective(Settings.FOV,(float)windowWidth/(float)windowHeight,MIN_DIST,FAR_DIST);

        normalMapping = Settings.TEXTURE_DETAIL!= Settings.GraphicsSetting.LOW;

        //shadow mapping stuff determined by the quality you choose
        //TODO: experiment with these settings later on in development, adding further enhancements and optimizations
        switch(Settings.SHADOW_RESOLUTION) {
            case LOW:
                shadowResolution = 1024;
                break;
            case MEDIUM:
                shadowResolution = 2048;
                break;
            default:
                shadowResolution = 4096;
                break;
        }
        float spreadDist = 1;
        switch(Settings.SHADOW_SOFTENING) {
            case LOW:
                shadowPCFCount = 1;
                spreadDist = 0.7f;
                break;
            case MEDIUM:
                shadowPCFCount = 2;
                spreadDist = 0.9f;
                break;
            case HIGH:
                shadowPCFCount = 4;
                spreadDist = 1.2f;
                break;
            case ULTRA:
                shadowPCFCount = 6;
                spreadDist = 1.5f;
                break;
        }
        shadowPCFSpread = spreadDist/shadowPCFCount;

        //initialize shadow box stuff
        float mid = 8.5f;
        float far = 24;
        shadowDistanceValues = Arrays.asList(mid*0.9f, mid, mid+0.8f*(far-mid), far);//Currently works for triple shadow boxes, but potentially try 2 or 4 for different settings


        //load floor
        collisionHandler = CollisionLoader.loadCollisionHandler(map.getCollisions());

        //load all game related shaders
        loadShaders();
        //load all map related shaders
        map.loadShaders(this);
        //your own beast's shader - do after everything else
        Shader animatedShader2 = new Shader("res/shaders/3d/animated_v.glsl","res/shaders/3d/beast_f.glsl");
        animatedShader2.bindTextureUnits(Arrays.asList("textureSampler","textureSampler2","shadowSampler0","shadowSampler1","shadowSampler2"));
        loadShader(animatedShader2,true,true,true,false);

        //load the shadow renderer
        shadowRenderer = new ShadowRenderer(memoryManager,masterRenderer,depthShader,animatedDepthShader,lightDir,map.getBoundingBox(),controllers.size(),shadowResolution);

        //load all player windows and renderers
        loadRenderersAndPlayers(controllers);

        //load the shadow boxes
        loadShadowboxes();

        //load the sky box
        loadSkybox(map.getSkyboxTexture());

        //load objects from the map
        map.loadObjects(this);

        //link your own beast's to its own renderer
        for(int i = 0;i<players.size();i++) {
            Player player = players.get(i);
            Renderer renderer = rendererLists.get(rendererLists.size()-1).getRenderer(i);
            player.linkToPlayerRenderer(renderer);
        }

        //music
        Sound sound = new Sound(memoryManager,map.getMusic());
        musicSource = new SoundSource(sound,true);
        musicSource.play();
    }

    /**
     * Load the major shaders
     */
    private void loadShaders() {
        //shader for 2d elements
        shader2D = new Shader("res/shaders/gui_v.glsl","res/shaders/gui_f.glsl");
        shader2D.bindTextureUnits(Arrays.asList("textureSampler"));

        //depth shaders for shadow mapping
        depthShader = new Shader("res/shaders/depth_v.glsl","res/shaders/depth_f.glsl");
        animatedDepthShader = new Shader("res/shaders/animated_depth_v.glsl","res/shaders/depth_f.glsl");

        //skybox shader
        Shader skyboxShader = new Shader("res/shaders/3d/skybox_v.glsl","res/shaders/3d/skybox_f.glsl");
        skyboxShader.bindTextureUnits(Arrays.asList("samplerCube"));
        loadShader(skyboxShader,false,true,false,true);

        //animated shader
        Shader animatedShader = new Shader("res/shaders/3d/animated_v.glsl","res/shaders/3d/general_shadow_f.glsl");
        animatedShader.bindTextureUnits(Arrays.asList("textureSampler","textureSampler2","shadowSampler0","shadowSampler1","shadowSampler2"));
        loadShader(animatedShader,true,true,true,false);
    }

    /**
     * Load a shader and create a renderer list
     */
    public void loadShader(Shader shader, boolean lighting, boolean projection, boolean shadow, boolean viewMatrixDirOnly) {
        if(lighting) {
            shader.setUniformVector3f(shader.getUniformLocation("lightDirection"),lightDir);
            shader.setUniformVector3f(shader.getUniformLocation("lightColor"),lightColor);
            shader.setUniformVector3f(shader.getUniformLocation("ambient"),ambient);
        }
        if(projection) {
            shader.setUniformMatrix4(shader.getUniformLocation("projectionMatrix"),this.projection);
        }
        if(shadow) {
            //shader.setUniformFloat(shader.getUniformLocation("pxSize"),1.0f/(float)shadowResolution);
            shader.setUniformInt(shader.getUniformLocation("pcfCount"),shadowPCFCount);
            shader.setUniformFloat(shader.getUniformLocation("pcfIncrAmount"), 1.0f/(4*shadowPCFCount*shadowPCFCount + 4*shadowPCFCount + 1));
            shader.setUniformFloat(shader.getUniformLocation("pcfSpread"),shadowPCFSpread);
            for(int i = 0;i<4;i++) {
                shader.setUniformFloat(shader.getUniformLocation("shadowDist[" + i + "]"),shadowDistanceValues.get(i));
            }
        }
        RendererList rendererList = new RendererList(shader,shadow, viewMatrixDirOnly);
        rendererLists.add(rendererList);
    }


    /**
     * Load each player, for each player, have their own framebuffer with some renderers, link the camera's view matrix to the renderer
     * TODO spawnpoints of each player
     * TODO character selection
     */
    private void loadRenderersAndPlayers(List<Integer> controllers) {
        //2d renderer
        renderer2D = masterRenderer.getWindowFramebuffer().createRenderer(shader2D);

        //create the beast you play as (JIMMY)
        Texture jimmyTexture = Texture.loadImageTexture3D(memoryManager,"res/textures/jimmy_tex.bmt");
        AnimatedModel jimmy = ModelLoader.loadAnimatedModel(memoryManager,"res/models/jimmy.bmma");

        //Create a window, renderer and character for each player
        for(int i = 0;i<controllers.size();i++) {

            //create shadow box renderers
            shadowRenderer.createRenderers(i);

            //framebuffer
            Framebuffer fbo = createPlayerWindow();
            masterRenderer.addFramebuffer(fbo);

            //rectangle on screen as the splitscreen window
            Object2D sceneObject = new Object2D(memoryManager,
                    controllers.size()>2 && i%2==1 ? Settings.WIDTH/2 : 0,
                    (controllers.size()==2 && i==1) || i>1 ? Settings.HEIGHT/2 : 0,
                    fbo.getTexture(0));
            sceneObject.addToRenderer(renderer2D);

            //the player object
            Beast beast = new Beast(this, i==0 ? jimmy : jimmy,jimmyTexture);
            Player player = new Player(inputHandler,i+1,controllers.get(i),beast);

            //create all renderers from the shaders
            for(RendererList rendererList : rendererLists) {
                rendererList.createRenderer(fbo,player);
            }
            this.players.add(player);
            this.playerFbos.add(fbo);
        }

        //link all beast's to the renderers of the other players
        for(int j = 0;j<controllers.size();j++) {
            Beast b = players.get(j).getBeast();
            for(int i = 0;i<controllers.size();i++) {
                if(i!=j)b.linkToRenderer(rendererLists.get(1).getRenderer(i));
            }
            shadowRenderer.createShadowCastingAnimatedObject(b, ShadowRenderer.ShadowDistance.MIDDLE);
            objects.add(b);
        }
    }

    /**
     * Create a player window framebuffer
     */
    private Framebuffer createPlayerWindow() {
        Framebuffer fbo;
        if(Settings.ANTIALIASING>0) {
            fbo = Framebuffer.createMultisampledFramebuffer3Dto2D(memoryManager,windowWidth, windowHeight);

        }else{
            fbo = Framebuffer.createFramebuffer3Dto2D(memoryManager,windowWidth,windowHeight);
        }
        return fbo;
    }


    /**
     * Load the shadow boxes
     * TODO larger shadow box should cover the entire map
     */
    private void loadShadowboxes() {

        float aspectRatio = (float)windowWidth/(float)windowHeight;
        for(int i = 0;i<players.size();i++) {
            shadowRenderer.loadShadowBoxes(i,players.get(i).getViewMatrix(),0,shadowDistanceValues.get(1),shadowDistanceValues.get(0),shadowDistanceValues.get(3),aspectRatio);

            //Shadow matrices to renderer
            for(RendererList rendererList : rendererLists) {
                if(rendererList.receivesShadows()) {
                    shadowRenderer.linkToRenderer(rendererList.getRenderer(i),i);
                }
            }
        }
    }


    /**
     * Load the skybox
     */
    private void loadSkybox(String fileName) {
        Texture skyboxTexture = Texture.loadCubemapTexture(memoryManager,fileName);
        Model skyboxModel = new Skybox(memoryManager);
        for(Renderer renderer : rendererLists.get(0)) {
            ShaderObject object = renderer.createObject(skyboxModel);
            object.addTexture(0,skyboxTexture);
            object.disableDepth();
        }
    }

    /**
     * Create an object
     */
    public void createObject(Object3D object, int rendererId, boolean castsShadows) {
        //link to major renderer
        for(Renderer renderer : rendererLists.get(rendererId)) {
            object.linkToRenderer(renderer);
        }

        //add to shadow casting renderers
        if(castsShadows) {
            if(object instanceof AnimatedObject) {
                shadowRenderer.createShadowCastingAnimatedObject((AnimatedObject) object, ShadowRenderer.ShadowDistance.MIDDLE);
            }
            else {
                shadowRenderer.createShadowCastingObject(object);
            }
        }
        objects.add(object);
    }

    /**
     * Remove an object. Executed via object.removeObject();
     */
    public void removeObject(Object3D object) {
        objects.remove(object);
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
        objectsCopy.clear();
        objectsCopy.addAll(objects);
        //update objects
        for(Object3D object : objectsCopy) {
            object.update();
        }
        //update players
        for(Player player : players) {
            player.update();
        }
    }

    /**
     * Render
     */
    @Override
    public void render(float frameInterpolation) {
        objectsCopy.clear();
        objectsCopy.addAll(objects);
        //update objects
        for(Object3D object : objectsCopy) {
            object.interpolate(frameInterpolation);
        }
        //update players
        for(Player player : players) {
            player.interpolate(frameInterpolation);
        }
        //update shadow boxes
        shadowRenderer.update();
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
        musicSource.stop();
        musicSource.cleanUp();
        for(Player player :players) {
            player.cleanUp();
        }
    }

    /**
     * Returns if normal mapping is used
     */
    public boolean usesNormalMapping() {
        return normalMapping;
    }

    /**
     * Returns the memory manager
     */
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    /**
     * Get the floor collisions
     */
    public CollisionHandler getCollisionHandler() {return collisionHandler;}


    //unneeded most likely as all remove will be in cleanUp
    @Override
    public void removeObjects() { }

    @Override
    public void mouseEvent(boolean pressed, int button) {}

    @Override
    public void controllerEvent(int controller, boolean pressed, int button) {}
}
