package com.bestialMania.state;

import com.bestialMania.*;
import com.bestialMania.object.animation.AnimatedObject;
import com.bestialMania.object.beast.Beast;
import com.bestialMania.object.beast.Player;
import com.bestialMania.object.gui.Object2D;
import com.bestialMania.rendering.*;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.model.loader.Loader;
import com.bestialMania.rendering.model.Skybox;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.rendering.shader.UniformFloat;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.rendering.shadow.ShadowBox;
import com.bestialMania.sound.Sound;
import com.bestialMania.sound.SoundSource;
import com.bestialMania.state.menu.Menu;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL30.*;

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

    private static final float FAR_DIST = 80.0f;


    //Shaders
    private Shader normalmapShader, testShader, skyboxShader, shader2D, depthShader, animatedDepthShader, animatedShader;

    //Sound Sources
    private SoundSource musicSource;

    //Renderers
    private Renderer renderer2D;


    private List<Renderer> testRenderers = new ArrayList<>();
    private List<Renderer> animatedRenderers = new ArrayList<>();
    private List<Renderer> normalmapRenderers = new ArrayList<>();
    private List<Renderer> skyboxRenderers = new ArrayList<>();

    //players in the game
    private List<Player> players = new ArrayList<>();

    //shadow boxes
    private List<Float> shadowDistanceValues;
    private Renderer[][] shadowRenderers;
    private Renderer[][] shadowAnimatedRenderers;
    private Texture[][] shadowTextures;
    private ShadowBox[][] shadowBoxes;

    //some variables
    private boolean normalMapping;
    private Vector3f lightDir, lightColor;
    private Matrix4f projection;
    private int windowWidth,windowHeight;
    private int shadowResolution, shadowPCFCount;
    private float shadowPCFSpread;

    /**
     * Initialize a game
     */
    public Game(Main main, InputHandler inputHandler, List<Integer> controllers) {
        this.main = main;
        this.inputHandler = inputHandler;
        masterRenderer = new MasterRenderer();
        memoryManager = new MemoryManager();

        inputHandler.addListener(this);

        inputHandler.setCursorDisabled();

        windowWidth = controllers.size()<3 ? Settings.WIDTH : Settings.WIDTH/2;
        windowHeight =  controllers.size()==1 ? Settings.HEIGHT : Settings.HEIGHT/2;


        lightDir = new Vector3f(-1.4f, -0.5f, 2.5f).normalize();
        lightColor = new Vector3f(1.0f, 1.0f, 1.0f);
        projection = new Matrix4f();
        projection.perspective(Settings.FOV,(float)windowWidth/(float)windowHeight,0.1f,FAR_DIST);

        normalMapping = Settings.TEXTURE_DETAIL!= Settings.GraphicsSetting.LOW;

        //shadow mapping stuff determined by the quality you choose
        //TODO: experiment with these settings later on in development, adding further enhancements and optimizations
        switch(Settings.SHADOW_QUALITY) {
            case MEDIUM:
                shadowResolution = 2048;
                shadowPCFCount = 2;
                shadowPCFSpread = 0.65f;
                break;
            case HIGH:
                shadowResolution = 2048;
                shadowPCFCount = 4;
                shadowPCFSpread = 0.45f;
                break;
            case ULTRA:
                shadowResolution = 4096;
                shadowPCFCount = 5;
                shadowPCFSpread = 0.45f;
                break;
            default:
                shadowResolution = 1024;
                shadowPCFCount = 1;
                shadowPCFSpread = 0.65f;
                break;
        }

        shadowDistanceValues = Arrays.asList(0.1f,FAR_DIST * 0.125f,FAR_DIST * 0.45f,FAR_DIST);//Currently works for triple shadow boxes, but potentially try 2 or 4 for different settings

        shadowRenderers = new Renderer[controllers.size()][shadowDistanceValues.size()-1];
        shadowAnimatedRenderers = new Renderer[controllers.size()][shadowDistanceValues.size()-1];
        shadowTextures = new Texture[controllers.size()][shadowDistanceValues.size()-1];
        shadowBoxes = new ShadowBox[controllers.size()][shadowDistanceValues.size()-1];

        loadShaders();
        loadRenderersAndPlayers(controllers);
        loadShadowboxes();
        loadSkybox();
        loadObjects();
        loadMusic();

    }

    /**
     * Load the "music"
     *
     */
    private void loadMusic() {
        Sound sound = new Sound(memoryManager,"res/sound/pumped_up_kicks.wav");
        musicSource = new SoundSource(sound,true);
        musicSource.play();
    }

    /**
     * Load the shaders
     */
    private void loadShaders() {
        //regular shader
        testShader = new Shader("res/shaders/3d/test_v.glsl","res/shaders/3d/test_f.glsl");
        testShader.bindTextureUnits(Arrays.asList("textureSampler"));
        testShader.setUniformVector3f(testShader.getUniformLocation("lightDirection"),lightDir);
        testShader.setUniformVector3f(testShader.getUniformLocation("lightColor"),lightColor);
        testShader.setUniformMatrix4(testShader.getUniformLocation("projectionMatrix"),projection);

        //animated shader
        animatedShader = new Shader("res/shaders/3d/animated_v.glsl","res/shaders/3d/test_f.glsl");
        animatedShader.bindTextureUnits(Arrays.asList("textureSampler"));
        animatedShader.setUniformVector3f(animatedShader.getUniformLocation("lightDirection"),lightDir);
        animatedShader.setUniformVector3f(animatedShader.getUniformLocation("lightColor"),lightColor);
        animatedShader.setUniformMatrix4(animatedShader.getUniformLocation("projectionMatrix"),projection);

        //normalmap shader
        normalmapShader = null;
        if(normalMapping) {
            normalmapShader = new Shader("res/shaders/3d/normalmap_v.glsl","res/shaders/3d/normalmap_f.glsl");
            normalmapShader.bindTextureUnits(Arrays.asList("textureSampler","normalSampler","shadowSampler[0]","shadowSampler[1]","shadowSampler[2]"));
            normalmapShader.setUniformVector3f(normalmapShader.getUniformLocation("lightDirection"),lightDir);
            normalmapShader.setUniformVector3f(normalmapShader.getUniformLocation("lightColor"),lightColor);
            normalmapShader.setUniformMatrix4(normalmapShader.getUniformLocation("projectionMatrix"),projection);
            normalmapShader.setUniformFloat(normalmapShader.getUniformLocation("pxSize"),1.0f/(float)shadowResolution);
            normalmapShader.setUniformInt(normalmapShader.getUniformLocation("pcfCount"),shadowPCFCount);
            normalmapShader.setUniformFloat(normalmapShader.getUniformLocation("pcfSpread"),shadowPCFSpread);
            for(int i = 0;i<shadowDistanceValues.size()-1;i++) {
                normalmapShader.setUniformFloat(normalmapShader.getUniformLocation("shadowDist[" + i + "]"),shadowDistanceValues.get(i+1));
            }
        }
        //skybox shader
        skyboxShader = new Shader("res/shaders/3d/skybox_v.glsl","res/shaders/3d/skybox_f.glsl");
        skyboxShader.bindTextureUnits(Arrays.asList("samplerCube"));
        skyboxShader.setUniformMatrix4(skyboxShader.getUniformLocation("projectionMatrix"),projection);

        //shader for 2d elements
        shader2D = new Shader("res/shaders/gui_v.glsl","res/shaders/gui_f.glsl");
        shader2D.bindTextureUnits(Arrays.asList("textureSampler"));

        //depth shader for shadow mapping
        depthShader = new Shader("res/shaders/depth_v.glsl","res/shaders/depth_f.glsl");
        animatedDepthShader = new Shader("res/shaders/animated_depth_v.glsl","res/shaders/depth_f.glsl");
    }


    /**
     * Load each player, for each player, have their own framebuffer with some renderers, link the camera's view matrix to the renderer
     */
    private void loadRenderersAndPlayers(List<Integer> controllers) {

        //2d renderer
        renderer2D = masterRenderer.getWindowFramebuffer().createRenderer(shader2D);

        //create the beast you play as (JIMMY)
        Texture jimmyTexture = Texture.loadImageTexture3D(memoryManager,"res/textures/jimmy_tex.png");
        AnimatedObject jimmy = Loader.loadAnimatedModel(memoryManager,"res/models/dae/jimmy.dae");

        //Create a window, renderer and character for each player
        for(int i = 0;i<controllers.size();i++) {

            //create shadow box framebuffers
            for(int j = 0;j<shadowDistanceValues.size()-1;j++) {
                Framebuffer shadowFbo = Framebuffer.createDepthFramebuffer2D(memoryManager,shadowResolution,shadowResolution);
                masterRenderer.addFramebuffer(shadowFbo);

                //shadow map renderer
                shadowRenderers[i][j] = shadowFbo.createRenderer(depthShader);
                shadowRenderers[i][j].setCull(GL_FRONT);
                shadowAnimatedRenderers[i][j] = shadowFbo.createRenderer(animatedDepthShader);
                shadowAnimatedRenderers[i][j].setCull(GL_FRONT);
                shadowTextures[i][j] = shadowFbo.getTexture(0);
            }

            //framebuffer
            Framebuffer fbo = createPlayerWindow();
            masterRenderer.addFramebuffer(fbo);

            //renderer
            Renderer renderer = fbo.createRenderer(testShader);
            testRenderers.add(renderer);

            //rectangle on screen as the splitscreen window
            Object2D sceneObject = new Object2D(memoryManager,
                    controllers.size()>2 && i%2==1 ? Settings.WIDTH/2 : 0,
                    (controllers.size()==2 && i==1) || i>1 ? Settings.HEIGHT/2 : 0,
                    fbo.getTexture(0));
            sceneObject.addToRenderer(renderer2D);

            //the player object
            Beast beast = new Beast(i==0 ? jimmy : new AnimatedObject(jimmy),jimmyTexture);
            Player player = new Player(inputHandler,i+1,controllers.get(i),beast);

            player.linkCameraToRenderer(renderer);
            //normal mapping renderer
            if(normalMapping) {
                Renderer nmRenderer = fbo.createRenderer(normalmapShader);
                normalmapRenderers.add(nmRenderer);
                player.linkCameraToRenderer(nmRenderer);
            }

            //skybox renderer
            Renderer skyboxRenderer = fbo.createRenderer(skyboxShader);
            skyboxRenderers.add(skyboxRenderer);
            player.linkCameraDirectionToRenderer(skyboxRenderer);

            //animated renderer
            Renderer animatedRenderer = fbo.createRenderer(animatedShader);
            animatedRenderers.add(animatedRenderer);
            player.linkCameraToRenderer(animatedRenderer);


            this.players.add(player);
        }

        //link all beast's to renderers
        for(Player player : this.players) {
            for(Renderer renderer : animatedRenderers) {
                player.getBeast().linkToRenderer(renderer);
            }
            createShadowCastingAnimatedObject(player.getBeast().getAnimatedObject(),player.getBeast().getMatrix());
        }
    }


    /**
     * Load the shadow boxes
     */
    private void loadShadowboxes() {
        float seamRatio = 0.3f;
        for(int i = 0;i<players.size();i++) {
            for(int j = 0;j<shadowDistanceValues.size()-1;j++) {
                //calculate positions
                float front = shadowDistanceValues.get(j);
                if (j>0) front-=(shadowDistanceValues.get(j)-shadowDistanceValues.get(j-1)) * seamRatio;
                float back = shadowDistanceValues.get(j+1);
                //add to depth renderer
                shadowBoxes[i][j] = new ShadowBox((float)windowWidth/(float)windowHeight,players.get(i).getViewMatrix(),lightDir,front,back);
                shadowBoxes[i][j].linkToDepthRenderer(shadowRenderers[i][j]);
                shadowBoxes[i][j].linkToDepthRenderer(shadowAnimatedRenderers[i][j]);

                if(normalMapping) {
                    //Shadow matrices to renderer
                    shadowBoxes[i][j].linkToRenderer(normalmapRenderers.get(i),j);
                    //Shadow textures to renderer
                    normalmapRenderers.get(i).addTexture(2+j,shadowTextures[i][j]);
                }
            }
        }
    }


    /**
     * Load the skybox
     */
    private void loadSkybox() {
        Texture skyboxTexture = Texture.loadCubemapTexture(memoryManager,"res/textures/skyboxes/test/desertsky","png");
        Model skyboxModel = new Skybox(memoryManager);
        for(Renderer renderer : skyboxRenderers) {
            ShaderObject object = renderer.createObject(skyboxModel);
            object.addTexture(0,skyboxTexture);
            object.disableDepth();
        }
    }

    /**
     * Create the ingame objects
     */
    private void loadObjects() {
        //SOME POLE OBJECT
        Model poleModel = Loader.loadOBJ(memoryManager,"res/models/pole.obj");
        Texture poleTexture = Texture.loadImageTexture3D(memoryManager,"res/textures/concrete.png");

        Texture poleNormalmap = null;
        if(normalMapping) {
            poleNormalmap = Texture.loadImageTexture3D(memoryManager, "res/textures/concrete_normal.png");
        }

        Matrix4f testObjectMatrix = new Matrix4f();
        testObjectMatrix.translate(2.0f,0,0.5f);
        testObjectMatrix.scale(3.0f,3.0f,3.0f);

        for(Renderer renderer : (normalMapping ? normalmapRenderers : testRenderers)) {//use regular shader on low texture detail
            ShaderObject testObject = renderer.createObject(poleModel);
            testObject.addTexture(0,poleTexture);
            if(normalMapping) testObject.addTexture(1,poleNormalmap);
            testObject.addUniform(new UniformMatrix4(normalMapping ? normalmapShader : testShader,"modelMatrix",testObjectMatrix));
            testObject.addUniform(new UniformFloat(normalMapping ? normalmapShader : testShader,"reflectivity",0.5f));
            testObject.addUniform(new UniformFloat(normalMapping ? normalmapShader : testShader,"shineDamper",10.0f));
        }
        createShadowCastingObject(poleModel,testObjectMatrix);

        //SOME FLOOR OBJECT
        Model planeModel = Loader.loadOBJ(memoryManager,"res/models/plane.obj");
        Matrix4f planeMatrix = new Matrix4f();
        Texture planeTexture = Texture.loadImageTexture3D(memoryManager,"res/textures/rocky.png");
        Texture planeNormalmap = null;
        if(normalMapping) {
            planeNormalmap = Texture.loadImageTexture3D(memoryManager, "res/textures/rocky_normal.png");
        }
        for(Renderer renderer : (normalMapping ? normalmapRenderers : testRenderers)) {//use regular shader on low texture detail
            ShaderObject testObject = renderer.createObject(planeModel);
            testObject.addTexture(0,planeTexture);
            if(normalMapping) testObject.addTexture(1,planeNormalmap);
            testObject.addUniform(new UniformMatrix4(normalMapping ? normalmapShader : testShader,"modelMatrix",planeMatrix));
            testObject.addUniform(new UniformFloat(normalMapping ? normalmapShader : testShader,"reflectivity",0.1f));
            testObject.addUniform(new UniformFloat(normalMapping ? normalmapShader : testShader,"shineDamper",4.0f));
        }
        createShadowCastingObject(planeModel,planeMatrix);

        //TEST ANIMATED MODEL
        //Model m = DAELoader.loadDAEModel(memoryManager,"res/models/dae/jimmy.dae");
    }

    /**
     * Create a player window framebuffer
     */
    private Framebuffer createPlayerWindow() {
        Framebuffer fbo;
        if(Settings.ANTIALIASING) {
            fbo = Framebuffer.createMultisampledFramebuffer3Dto2D(memoryManager,windowWidth, windowHeight);

        }else{
            fbo = Framebuffer.createFramebuffer3Dto2D(memoryManager,windowWidth,windowHeight);
        }
        return fbo;
    }

    /**
     * Add an object to a shadow renderer
     */
    private void createShadowCastingObject(Model model, Matrix4f modelMatrix) {
        for(Renderer[] rendererArray : shadowRenderers) {
            for(Renderer renderer : rendererArray) {
                ShaderObject object = renderer.createObject(model);
                object.addUniform(new UniformMatrix4(renderer.getShader(), "modelMatrix", modelMatrix));
            }
        }
    }

    /**
     * Add an animated object to a shadow renderer
     */
    private void createShadowCastingAnimatedObject(AnimatedObject object, Matrix4f modelMatrix) {
        for(Renderer[] rendererArray : shadowAnimatedRenderers) {
            for(Renderer renderer : rendererArray) {
                ShaderObject so = object.createObject(renderer);
                so.addUniform(new UniformMatrix4(renderer.getShader(), "modelMatrix", modelMatrix));
            }
        }
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
        //update players
        for(Player player : players) {
            player.interpolate(frameInterpolation);
        }
        //update shadow boxes
        for(ShadowBox[] shadowBoxArray : shadowBoxes) {
            for(ShadowBox shadowBox : shadowBoxArray) {
                shadowBox.update();
            }
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
        musicSource.stop();
        musicSource.cleanUp();
    }

    //unneeded most likely as all remove will be in cleanUp
    @Override
    public void removeObjects() { }

    @Override
    public void mouseEvent(boolean pressed, int button) {}

    @Override
    public void controllerEvent(int controller, boolean pressed, int button) {}
}
