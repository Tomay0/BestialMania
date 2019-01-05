package com.bestialMania.state.menu;

import com.bestialMania.InputHandler;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.MemoryManager;
import com.bestialMania.state.State;


/**
 * THIS CODE HERE IS ONLY FOR THE PURPOSES OF COPY+PASTE,
 * ALL COMMENTS HERE ARE FOR LEARNING PURPOSES AND CAN BE REMOVED/SIMPLIFIED AFTER COPY PASTING
 */
public class MenuSkeletonCode implements State {//Implement state so can use the render() method and other important methods, can also implement InputListener and ButtonListener if necessary
    /*
    The menu.
    Options you pick will be saved to this object until you start the game, such as map selected, characters selected, etc.
    You can also load shared textures/models into this menu object rather than in here so the game doesn't need to keep loading the same textures/models every submenu.
     */
    private Menu menu;

    //InputHandler
    private InputHandler inputHandler;

    //Uses the same renderer as in the menu class, just call the removeFromRenderer methods within removeObjects() to make transitions seamless.
    private MasterRenderer renderer;

    //Required for creating anything on the graphics card, gets deleted in the removeObjects() method.
    private MemoryManager memoryManager;

    /**
     * Constructor - build all objects like buttons and text here.
     * You can also build some objects
     */
    public MenuSkeletonCode(Menu menu, InputHandler inputHandler, MasterRenderer renderer) {
        this.menu = menu;
        this.renderer = renderer;
        this.inputHandler = inputHandler;
        this.memoryManager = new MemoryManager();//memory manager needs to be created within this particular submenu

        //create text/buttons here.
        //see PlayerSelect or MainMenu classes for ideas on how to do this.
    }

    /**
     * Anything that needs to be updated each frame, such as checking if a button is held or similar.
     */
    @Override
    public void update() {

    }

    /**
     * Nothing else really needs to be added to this method other than renderer.render(); as everything is taken care of within this method call
     */
    @Override
    public void render() {
        renderer.render();
    }

    /**
     * - Delete buttons,etc from a renderer that will not be used in the next menu
     * - Removes listeners for buttons that are no longer in use
     * - Also call memoryManager.cleanUp() to remove any extra memory loaded from the buttons/text you have added onscreen
     */
    @Override
    public void removeObjects() {
        //text.removeFromRenderer(someRendererHere);
        //button.removeListener();

        memoryManager.cleanUp();
    }


    /**
     * Called when you exit the menu entirely (either by quitting or entering the game)
     * thus the menu needs to be cleaned up as well as the objects on this particular screen
     */
    @Override
    public void cleanUp() {
        removeObjects();
        menu.cleanUp();
    }
}
