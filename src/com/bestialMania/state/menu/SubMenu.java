package com.bestialMania.state.menu;

import com.bestialMania.InputHandler;
import com.bestialMania.MemoryManager;
import com.bestialMania.gui.Button;
import com.bestialMania.gui.ButtonListener;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.state.State;

import java.util.HashSet;
import java.util.Set;

public abstract class SubMenu implements State, ButtonListener {
    protected Menu menu;
    protected InputHandler inputHandler;
    protected MasterRenderer masterRenderer;
    protected MemoryManager memoryManager;
    private Set<Button> buttons = new HashSet<>();

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
     * Add a button
     */
    public void addButton(Button button) {
        buttons.add(button);
        button.add(menu.getTextRender());
    }

    /**
     * Remove a button
     */
    public void removeButton(Button button) {
        buttons.remove(button);
        button.remove(menu.getTextRender());
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
            button.remove(menu.getTextRender());
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
