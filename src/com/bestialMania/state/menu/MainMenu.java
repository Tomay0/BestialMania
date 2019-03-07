package com.bestialMania.state.menu;

import com.bestialMania.Settings;
import com.bestialMania.InputHandler;
import com.bestialMania.collision.CollisionHandler;
import com.bestialMania.collision.CollisionLoader;
import com.bestialMania.collision.Triangle;
import com.bestialMania.gui.Button;
import com.bestialMania.gui.ButtonListener;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.MemoryManager;
import com.bestialMania.rendering.model.loader.ModelConverter;
import com.bestialMania.state.State;
import com.bestialMania.state.game.map.MapData;
import org.joml.Vector3f;

import java.io.File;

public class MainMenu implements State, ButtonListener {
    //submenu stuff
    private Menu menu;
    private InputHandler inputHandler;
    private MasterRenderer masterRenderer;
    private MemoryManager memoryManager;

    //buttons
    private Button startButton,quitButton,collisionsButton;

    /**
     * Setup the main menu
     */
    public MainMenu(Menu menu, InputHandler inputHandler, MasterRenderer masterRenderer) {
        this.menu = menu;
        this.masterRenderer = masterRenderer;
        this.inputHandler = inputHandler;
        this.memoryManager = new MemoryManager();


        //buttons
        startButton = new Button(memoryManager,inputHandler,this, Settings.WIDTH/2-60, Settings.HEIGHT/2,"res/textures/ui/start.png","Start","start");
        quitButton = new Button(memoryManager,inputHandler,this, Settings.WIDTH/2-60, Settings.HEIGHT/2+100,"res/textures/ui/quit.png","Quit","quit");
        collisionsButton = new Button(memoryManager,inputHandler,this,Settings.WIDTH-180,Settings.HEIGHT-160,"res/textures/ui/convert.png","Convert Resources","rf");

        startButton.addToRenderer(menu.getGuiRender());
        quitButton.addToRenderer(menu.getGuiRender());
        collisionsButton.addToRenderer(menu.getGuiRender());

    }

    /**
     * Probably just does nothing
     */
    @Override
    public void update() {}

    /**
     * Render
     */
    @Override
    public void render(float frameInterpolation) {
        masterRenderer.render();
    }

    /**
     * Remove objects
     */
    @Override
    public void removeObjects() {
        //delete buttons from the renderer
        startButton.removeFromRenderer(menu.getGuiRender());
        quitButton.removeFromRenderer(menu.getGuiRender());
        collisionsButton.removeFromRenderer(menu.getGuiRender());

        //delete buttons from inputhandler
        startButton.removeListener();
        quitButton.removeListener();
        collisionsButton.removeListener();

        //delete buttons from memory
        memoryManager.cleanUp();
    }

    @Override
    public void cleanUp() {
        removeObjects();
        menu.cleanUp();
    }

    /**
     * Button actions
     */
    @Override
    public void press(String action) {
        if(action.equals("start")) {
            menu.setCurrentState(Menu.MenuState.PLAYER_SELECT);
        }else if(action.equals("quit")) {
            menu.quitGame();
        }else if(action.equals("rf")) {
            reloadAllFiles();
        }
    }

    public void reloadAllFiles() {
        try {
            //MODELS
            //look through all files in the "toConvert" folder
            File dir = new File("toConvert/models");
            for(File file : dir.listFiles()) {
                String name = file.getName();
                //OBJ
                if(name.endsWith(".obj")) {
                    String newName = name.replace(".obj",".bmm");
                    ModelConverter.convertOBJ("toConvert/models/" + name,"res/models/" + newName);
                }
                //DAE
                else if(name.endsWith(".dae")) {
                    String newName = name.replace(".dae",".bmma");
                    ModelConverter.convertDAE("toConvert/models/" + name, "res/models/" + newName);
                }
            }

            //refresh all collisions
            for(MapData mapData : Menu.MAPS) {
                String fileName = mapData.getCollisions();
                CollisionLoader loader = mapData.loadCollisions();
                loader.saveToFile(fileName);
            }

            System.out.println("Converted all files successfully!");
        }catch(Exception e) {
            System.err.println("Error when converting files");
            e.printStackTrace();
        }
    }
}
