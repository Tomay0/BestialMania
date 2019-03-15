package com.bestialMania.state.menu;

import com.bestialMania.InputHandler;
import com.bestialMania.MemoryManager;
import com.bestialMania.gui.Button;
import com.bestialMania.gui.ButtonListener;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.state.State;

import java.util.ArrayList;
import java.util.List;

public abstract class SubMenu implements State, ButtonListener {
    protected Menu menu;
    protected InputHandler inputHandler;
    protected MasterRenderer masterRenderer;
    protected MemoryManager memoryManager;
    protected List<Button> buttons = new ArrayList<>();

    /**
     * Create a new submenu
     */
    public SubMenu(Menu menu, InputHandler inputHandler, MasterRenderer masterRenderer) {
        this.menu = menu;
        this.masterRenderer = masterRenderer;
        this.inputHandler = inputHandler;
        this.memoryManager = new MemoryManager();
    }

    /**
     * Link all buttons to the text renderer
     */
    public void linkButtonsToRenderers() {
        for(Button button : buttons) button.addToRenderer(menu.getTextRender());
    }

    /**
     * Updates buttons on the screen
     */
    @Override
    public void update() {
        //update buttons
        for(Button button : buttons) {
            button.update();
        }
        //another update method for different menus
        menuUpdate();
    }
    public abstract void menuUpdate();

    /**
     * Render
     */
    @Override
    public void render(float frameInterpolation) {
        //main render method
        masterRenderer.render();
    }

    /**
     * Remove objects
     */
    @Override
    public void removeObjects() {
        menuRemoveObjects();
        //remove buttons
        for(Button button : buttons) {
            button.removeFromRenderer(menu.getTextRender());
            button.removeListener();
        }

        //delete submenu memory stuff
        memoryManager.cleanUp();
    }
    public abstract void menuRemoveObjects();

    /**
     * Clear memory
     */
    @Override
    public void cleanUp() {
        removeObjects();
        menu.cleanUp();
    }
}
