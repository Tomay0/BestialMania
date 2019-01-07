package com.bestialMania.state.menu;

import com.bestialMania.DisplaySettings;
import com.bestialMania.InputHandler;
import com.bestialMania.object.gui.Button;
import com.bestialMania.object.gui.ButtonListener;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.MemoryManager;
import com.bestialMania.state.State;

public class MainMenu implements State, ButtonListener {
    //submenu stuff
    private Menu menu;
    private InputHandler inputHandler;
    private MasterRenderer renderer;
    private MemoryManager memoryManager;

    //buttons
    private Button startButton,quitButton;

    /**
     * Setup the main menu
     */
    public MainMenu(Menu menu, InputHandler inputHandler, MasterRenderer renderer) {
        this.menu = menu;
        this.renderer = renderer;
        this.inputHandler = inputHandler;
        this.memoryManager = new MemoryManager();


        //buttons
        startButton = new Button(memoryManager,inputHandler,this, DisplaySettings.WIDTH/2-60,DisplaySettings.HEIGHT/2,"res/textures/ui/start.png","Start","start");
        quitButton = new Button(memoryManager,inputHandler,this, DisplaySettings.WIDTH/2-60,DisplaySettings.HEIGHT/2+100,"res/textures/ui/quit.png","Quit","quit");

        startButton.addToRenderer(menu.getGuiRender());
        quitButton.addToRenderer(menu.getGuiRender());
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
    public void render() {
        renderer.render();
    }

    /**
     * Remove objects
     */
    @Override
    public void removeObjects() {
        //delete buttons from the renderer
        startButton.removeFromRenderer(menu.getGuiRender());
        quitButton.removeFromRenderer(menu.getGuiRender());

        //delete buttons from inputhandler
        startButton.removeListener();
        quitButton.removeListener();

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
        }
    }
}