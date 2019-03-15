package com.bestialMania.state.menu;

import com.bestialMania.InputHandler;
import com.bestialMania.Main;
import com.bestialMania.MemoryManager;
import com.bestialMania.Settings;
import com.bestialMania.gui.Button;
import com.bestialMania.gui.ButtonListener;
import com.bestialMania.gui.text.Text;
import com.bestialMania.network.Client;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.state.State;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class OnlineMenu extends SubMenu {
    private Text loadingText;
    private Client client;
    /**
     * Setup the main menu
     */
    public OnlineMenu(Menu menu, InputHandler inputHandler, MasterRenderer masterRenderer) {
        super(menu,inputHandler,masterRenderer);

        //loading text
        loadingText = new Text(memoryManager,"Connecting to server...",menu.getFont(),60,Settings.WIDTH/2,Settings.HEIGHT/2-50, Text.TextAlign.CENTER,new Vector3f(255,0,0));
        loadingText.addToRenderer(menu.getTextRender());

        Button localButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH/2, Settings.HEIGHT/2+50, 200, 60,
                "BACK","back");
        buttons.add(localButton);
        client = new Client();

        linkButtonsToRenderers();
    }

    @Override
    public void menuUpdate() {

    }

    @Override
    public void menuRemoveObjects() {
       loadingText.removeFromRenderer(menu.getTextRender());
    }

    /**
     * Button actions
     */
    @Override
    public void press(String action) {
        if(action.equals("start")) {
            //BEGIN GAME (IF START BUTTON APPEARS)
            menu.getConnectedPlayers().add(-1);
            menu.startGame();
        }
        else if(action.equals("back")) {
            menu.setCurrentState(Menu.MenuState.MAIN_MENU);
        }
    }
}
