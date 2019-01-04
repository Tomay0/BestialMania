package com.bestialMania.state.menu;

import com.bestialMania.DisplaySettings;
import com.bestialMania.InputHandler;
import com.bestialMania.object.gui.text.Text;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.MemoryManager;
import com.bestialMania.state.State;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Player select screen.
 * Players can join by pressing Enter (Person playing with keyboard/mouse) or by pressing A/X
 *
 * TODO:
 * - follow with character select or integrate a character select into this screen.
 * - Add in some sort of way of giving a name (At the moment just have Player1, Player2, etc)
 */
public class PlayerSelect implements State {
    private Menu menu;
    private InputHandler inputHandler;
    private MasterRenderer renderer;
    private MemoryManager memoryManager;

    private List<Integer> connectedPlayers = new ArrayList<>();//list of connected control schemes (4 max). In order of who joined

    //text objects
    private Text[] texts = new Text[8];


    /**
     * Initialize the player select screen
     */
    public PlayerSelect(Menu menu, InputHandler inputHandler, MasterRenderer renderer) {
        this.menu = menu;
        this.inputHandler = inputHandler;
        this.renderer = renderer;
        memoryManager = new MemoryManager();

        int x = DisplaySettings.WIDTH/4;
        int y = DisplaySettings.HEIGHT/4;
        Vector3f c = new Vector3f(255,255,0);
        texts[0] = new Text(memoryManager,"Player 1",menu.getFont(),40, x,y-30, Text.TextAlign.CENTER,c);
        texts[1] = new Text(memoryManager,"Press <Start> button",menu.getFont(),40, x,y+30, Text.TextAlign.CENTER,c);

        texts[2] = new Text(memoryManager,"Player 2",menu.getFont(),40, 3*x,y-30, Text.TextAlign.CENTER,c);
        texts[3] = new Text(memoryManager,"Press <Start> button",menu.getFont(),40, 3*x,y+30, Text.TextAlign.CENTER,c);

        texts[4] = new Text(memoryManager,"Player 3",menu.getFont(),40, x,3*y-30, Text.TextAlign.CENTER,c);
        texts[5] = new Text(memoryManager,"Press <Start> button",menu.getFont(),40, x,3*y+30, Text.TextAlign.CENTER,c);

        texts[6] = new Text(memoryManager,"Player 4",menu.getFont(),40, 3*x,3*y-30, Text.TextAlign.CENTER,c);
        texts[7] = new Text(memoryManager,"Press <Start> button",menu.getFont(),40, 3*x,3*y+30, Text.TextAlign.CENTER,c);

        for(int i = 0;i<8;i++) {
            texts[i].addToRenderer(menu.getTextRender());
        }
    }

    /**
     * Update the game
     */
    @Override
    public void update() {
        //Enter pressed - add keyboard/mouse
        if(inputHandler.isKeyPressed(GLFW_KEY_ENTER)) {
            addPlayer(-1);
        }
        //backspace to remove keyboard/mouse TODO change to ESCAPE once have a suitable way to exit game other than ESCAPE
        if(inputHandler.isKeyPressed(GLFW_KEY_BACKSPACE)) {
            removePlayer(-1);
        }
        //Look for controllers that have the start key pressed
        for(int controller : inputHandler.getActiveControllers()) {
            //A/X add
            if(inputHandler.isGamepadButtonPressed(controller,0)) {
                addPlayer(controller);
            }
            //Y/Triangle remove
            if(inputHandler.isGamepadButtonPressed(controller,3)) {
                removePlayer(controller);
            }
        }

        //check if any of the controllers have disconnected randomly
        for(int controller : connectedPlayers) {
            if(controller!=-1 && !inputHandler.isControllerActive(controller)) {
                removePlayer(controller);
            }
        }
    }

    /**
     * Add a player to the list of connected players if not added already
     */
    private void addPlayer(int controller) {
        if(!connectedPlayers.contains(controller) && connectedPlayers.size()<4) {
            connectedPlayers.add(controller);
        }
    }

    /**
     * Remove a player from the list if they exist
     */
    private void removePlayer(int controller) {
        if(connectedPlayers.contains(controller)) {
            connectedPlayers.remove((Object)controller);
        }
    }

    /**
     * Render to the screen
     */
    @Override
    public void render() {
        renderer.render();

        //TEST PRINT
        for(int playerNum = 1;playerNum<=connectedPlayers.size();playerNum++) {
            System.out.println("Player " + playerNum + ": " + connectedPlayers.get(playerNum-1));
        }
    }

    /**
     * Remove all text and stuff
     */
    @Override
    public void removeObjects() {
        for(int i = 0;i<8;i++) {
            texts[i].removeFromRenderer(menu.getTextRender());
        }
        memoryManager.cleanUp();
    }

    /**
     * Free memory
     */
    @Override
    public void cleanUp() {
        removeObjects();
        menu.cleanUp();
    }
}
