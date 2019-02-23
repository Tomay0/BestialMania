package com.bestialMania.state.menu;

import com.bestialMania.InputHandler;
import com.bestialMania.Main;
import com.bestialMania.state.game.map.EmeraldValley;
import com.bestialMania.state.game.map.MapData;
import com.bestialMania.gui.text.Font;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.MemoryManager;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.state.game.Game;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Not a state itself but handles everything in the menu and switching between each individual menu state.
 * 1 master renderer is shared between all states
 */
public class Menu {

    //Menu state enum, used for easy switching between menus.
    public enum MenuState {PLAYER_SELECT,MAIN_MENU};

    private static final MapData[] MAPS = {new EmeraldValley()};

    private MasterRenderer renderer;//the renderer
    private MemoryManager memoryManager;//memory manager
    private InputHandler inputHandler;
    private Main main;//for swapping states

    //rendering objects to be shared by submenus
    private Renderer guiRender, textRender;
    private Shader guiShader, textShader;
    private Font font;


    //OPTIONS TODO: map selection, character selection, other game settings
    private List<Integer> connectedPlayers = new ArrayList<>();//list of connected control schemes (4 max). In order of who joined

    //-1, 1, 2, 3
    private int mapIndex;//currently selected map corresponding the MAPS array

    /**
     * Initialize the menu
     *
     */
    public Menu(Main main, InputHandler inputHandler) {
        this.main = main;
        this.inputHandler = inputHandler;
        //load all menu textures and fonts
        renderer = new MasterRenderer();
        renderer.getWindowFramebuffer().setBackgroundColor(new Vector3f(0.2f,0.2f,0.2f));
        memoryManager = new MemoryManager();
        mapIndex = 0;

        //shaders
        guiShader = new Shader("res/shaders/gui_v.glsl","res/shaders/gui_f.glsl");
        textShader = new Shader("res/shaders/gui_v.glsl", "res/shaders/text_f.glsl");

        guiRender = renderer.getWindowFramebuffer().createRenderer(guiShader);
        textRender = renderer.getWindowFramebuffer().createRenderer(textShader);

        font = new Font(memoryManager,"res/fonts/test.fnt","res/fonts/test.png");
    }

    /**
     * Start the game with the settings selected
     * This is separate to setCurrentState because the game is completely separate to the menu.
     */
    public void startGame() {
        if(connectedPlayers.size()==0) {
            System.out.println("Cannot start now, need at least one player");
        }else{
            main.getCurrentState().cleanUp();//delete all memories associated with the menu
            main.setCurrentState(new Game(main,inputHandler,connectedPlayers, MAPS[mapIndex]));
        }
    }

    /**
     * Quit the game
     */
    public void quitGame() {
        main.quit();
    }

    /**
     * Change the menu state
     */
    public void setCurrentState(MenuState state) {
        if(main.getCurrentState()!=null) main.getCurrentState().removeObjects();//remove objects in previous state
        switch(state) {
            //Player selection menu
            case PLAYER_SELECT: {
                main.setCurrentState(new PlayerSelect(this,inputHandler,renderer));
                break;
            }
            //Main menu
            case MAIN_MENU: {
                main.setCurrentState(new MainMenu(this,inputHandler,renderer));
                break;
            }
            /*
            TODO add other menus as necessary. You can use the MenuSkeletonCode class for help on how to setup the class for this.
            Addition of other menus to this method should be pretty much the same as the above methods
             */
            default: break;
        }
    }

    /**
     * Clean up memory method
     */
    public void cleanUp() {
        memoryManager.cleanUp();
    }

    /*


        GETTERS


     */

    public Renderer getGuiRender() {
        return guiRender;
    }

    public Renderer getTextRender() {
        return textRender;
    }

    public Shader getGuiShader() {
        return guiShader;
    }

    public Shader getTextShader() {
        return textShader;
    }

    public List<Integer> getConnectedPlayers() {
        return connectedPlayers;
    }

    public Font getFont() {
        return font;
    }
}
