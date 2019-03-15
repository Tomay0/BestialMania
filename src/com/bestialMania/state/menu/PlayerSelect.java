package com.bestialMania.state.menu;

import com.bestialMania.Settings;
import com.bestialMania.InputHandler;
import com.bestialMania.InputListener;
import com.bestialMania.gui.Button;
import com.bestialMania.gui.ButtonListener;
import com.bestialMania.gui.text.Text;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.MemoryManager;
import com.bestialMania.state.State;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Player select screen.
 * Players can join by pressing Enter (Person playing with keyboard/mouse) or by pressing A/X
 *
 * TODO:
 * - follow with character select or integrate a character select into this screen.
 * - Add in some sort of way of giving a name (At the moment just have Player1, Player2, etc)
 */
public class PlayerSelect extends SubMenu implements InputListener {

    //text objects
    private Text[] texts = new Text[12];

    /**
     * Initialize the player select screen
     */
    public PlayerSelect(Menu menu, InputHandler inputHandler, MasterRenderer renderer) {
        super(menu,inputHandler,renderer);
        inputHandler.addListener(this);

        int x = Settings.WIDTH/4;
        int y = Settings.HEIGHT/4;
        Vector3f c = new Vector3f(255,255,0);
        texts[0] = new Text(memoryManager,"Player 1",menu.getFont(),40, x,y-30, Text.TextAlign.CENTER,c);
        texts[1] = new Text(memoryManager,"Press A/X/Enter button",menu.getFont(),40, x,y+30, Text.TextAlign.CENTER,c);
        texts[2] = new Text(memoryManager,"Joined!",menu.getFont(),40, x,y+30, Text.TextAlign.CENTER,c);

        texts[3] = new Text(memoryManager,"Player 2",menu.getFont(),40, 3*x,y-30, Text.TextAlign.CENTER,c);
        texts[4] = new Text(memoryManager,"Press A/X/Enter button",menu.getFont(),40, 3*x,y+30, Text.TextAlign.CENTER,c);
        texts[5] = new Text(memoryManager,"Joined!",menu.getFont(),40, 3*x,y+30, Text.TextAlign.CENTER,c);

        texts[6] = new Text(memoryManager,"Player 3",menu.getFont(),40, x,3*y-30, Text.TextAlign.CENTER,c);
        texts[7] = new Text(memoryManager,"Press A/X/Enter button",menu.getFont(),40, x,3*y+30, Text.TextAlign.CENTER,c);
        texts[8] = new Text(memoryManager,"Joined!",menu.getFont(),40, x,3*y+30, Text.TextAlign.CENTER,c);

        texts[9] = new Text(memoryManager,"Player 4",menu.getFont(),40, 3*x,3*y-30, Text.TextAlign.CENTER,c);
        texts[10] = new Text(memoryManager,"Press A/X/Enter button",menu.getFont(),40, 3*x,3*y+30, Text.TextAlign.CENTER,c);
        texts[11] = new Text(memoryManager,"Joined!",menu.getFont(),40, 3*x,3*y+30, Text.TextAlign.CENTER,c);



        for(int i = 0;i<4;i++) {
            texts[i*3].addToRenderer(menu.getTextRender());
            texts[i*3+1].addToRenderer(menu.getTextRender());
        }

        Button startButton = new Button(memoryManager,inputHandler,this,menu.getFont(),x*2,y*2-50,200,60,"BEGIN","start");
        Button backButton = new Button(memoryManager,inputHandler,this,menu.getFont(), x*2,y*2+50,200,60,"BACK","back");
        buttons.add(startButton);
        buttons.add(backButton);

        for(Button button : buttons) {
            button.addToRenderer(menu.getTextRender());
        }
    }

    /**
     * Update the game
     */
    @Override
    public void menuUpdate() {
        //check if any of the controllers have disconnected randomly
        for(int controller : menu.getConnectedPlayers()) {
            if(controller!=-1 && !inputHandler.isControllerActive(controller)) {
                removePlayer(controller);
            }
        }
    }

    /**
     * Toggles to add/remove a player
     */
    private void togglePlayer(int controller) {
        //remove
        if(menu.getConnectedPlayers().contains(controller)) {
            removePlayer(controller);
        }
        //add if <4 available players
        else if(menu.getConnectedPlayers().size()<4){
            addPlayer(controller);
        }

        //TEST PRINT
        for(int playerNum = 1;playerNum<=menu.getConnectedPlayers().size();playerNum++) {
            System.out.println("Player " + playerNum + ": " + menu.getConnectedPlayers().get(playerNum-1));
        }
    }


    /**
     * Add a player to the list of connected players if not added already
     */
    private void addPlayer(int controller) {

        int pID = menu.getConnectedPlayers().size();

        texts[pID*3+2].addToRenderer(menu.getTextRender());
        texts[pID*3+1].removeFromRenderer(menu.getTextRender());

        menu.getConnectedPlayers().add(controller);
    }

    /**
     * Remove a player from the list if they exist
     */
    private void removePlayer(int controller) {
        int pID = menu.getConnectedPlayers().size()-1;

        texts[pID*3+1].addToRenderer(menu.getTextRender());
        texts[pID*3+2].removeFromRenderer(menu.getTextRender());

        menu.getConnectedPlayers().remove((Object)controller);
    }

    /**
     * Remove all text and stuff
     */
    @Override
    public void menuRemoveObjects() {
        for(int i = 0;i<12;i++) {
            texts[i].removeFromRenderer(menu.getTextRender());
        }

        //remove this listener
        inputHandler.removeListener(this);
    }

    @Override
    public void keyEvent(boolean pressed, int key) {
        if(pressed && key==GLFW_KEY_ENTER) {
            togglePlayer(-1);
        }
    }

    @Override
    public void controllerEvent(int controller, boolean pressed, int button) {
        if(pressed && button==0) {
            togglePlayer(controller);
        }
    }

    @Override
    public void mouseEvent(boolean pressed, int button) {}

    @Override
    public void press(String action) {
        if(action.equals("start")) {
            if(menu.getConnectedPlayers().size()<2) return;//only play local if there's at least 2 players
            menu.startGame();
        }else if(action.equals("back")) {
            menu.getConnectedPlayers().clear();
            menu.setCurrentState(Menu.MenuState.MAIN_MENU);
        }
    }
}
