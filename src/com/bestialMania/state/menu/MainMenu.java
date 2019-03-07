package com.bestialMania.state.menu;

import com.bestialMania.Main;
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
import com.bestialMania.rendering.texture.TextureImage;
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
        startButton = new Button(memoryManager,inputHandler,this, Settings.WIDTH/2-60, Settings.HEIGHT/2,"res/textures/ui/start.bmt","Start","start");
        quitButton = new Button(memoryManager,inputHandler,this, Settings.WIDTH/2-60, Settings.HEIGHT/2+100,"res/textures/ui/quit.bmt","Quit","quit");
        collisionsButton = new Button(memoryManager,inputHandler,this,Settings.WIDTH-180,Settings.HEIGHT-160,"res/textures/ui/convert.bmt","Convert Resources","rf");

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
            Main.reloadAllFiles();
        }
    }
}
