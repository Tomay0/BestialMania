package com.bestialMania.state.menu;

import com.bestialMania.InputHandler;
import com.bestialMania.Main;
import com.bestialMania.MemoryManager;
import com.bestialMania.Settings;
import com.bestialMania.gui.Button;
import com.bestialMania.gui.ButtonListener;
import com.bestialMania.gui.text.Text;
import com.bestialMania.network.Client;
import com.bestialMania.network.ServerListener;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.state.State;
import org.joml.Vector3f;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class OnlineMenu extends SubMenu implements ServerListener {
    private Text loadingText,loadingText2,failText;
    private Client client;
    private int serverMessage = 0;
    private boolean serverUpdate = false;//sets to true when the server sends a message that needs to be processed in the main thread
    private Button retryButton;
    /**
     * Setup the main menu
     */
    public OnlineMenu(Menu menu, InputHandler inputHandler, MasterRenderer masterRenderer) {
        super(menu,inputHandler,masterRenderer);

        //loading text
        loadingText = new Text(memoryManager,"Connecting to server...",menu.getFont(),60,Settings.WIDTH/2,Settings.HEIGHT/2-150, Text.TextAlign.CENTER,new Vector3f(255,0,0));
        failText = new Text(memoryManager,"Failed to connect to server.",menu.getFont(),60,Settings.WIDTH/2,Settings.HEIGHT/2-150, Text.TextAlign.CENTER,new Vector3f(255,0,0));
        loadingText2 = new Text(memoryManager,"Joining game...",menu.getFont(),60,Settings.WIDTH/2,Settings.HEIGHT/2-150, Text.TextAlign.CENTER,new Vector3f(255,0,0));

        Button backButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH/2, Settings.HEIGHT/2+50, 200, 60,
                "BACK","back");
        buttons.add(backButton);
        retryButton = new Button(memoryManager,inputHandler,this,menu.getFont(),
                Settings.WIDTH/2,Settings.HEIGHT/2-50,200,60,
                "RETRY","retry");

        connect();
        linkButtonsToRenderers();
    }

    /**
     * Set up the client and try to connect
     */
    private void connect() {
        client = new Client("localhost",6969,this);
        loadingText.addToRenderer(menu.getTextRender());
    }

    @Override
    public void menuUpdate() {
        //send mouse coordinates to the server for some reason
        if(client.isConnected()) {
            //client.sendData(inputHandler.getMousePosition().x + "," + inputHandler.getMousePosition().y);
        }

        //update something based on what is sent by the server
        if(serverUpdate) {
            if(serverMessage==1) {
                //established connection
                loadingText.removeFromRenderer(menu.getTextRender());
                loadingText2.addToRenderer(menu.getTextRender());
            }
            else if(serverMessage==2) {
                //failed connection
                loadingText.removeFromRenderer(menu.getTextRender());
                failText.addToRenderer(menu.getTextRender());
                buttons.add(retryButton);
                retryButton.addToRenderer(menu.getTextRender());
            }

            serverUpdate = false;
        }
    }

    @Override
    public void menuRemoveObjects() {
       loadingText.removeFromRenderer(menu.getTextRender());
       failText.removeFromRenderer(menu.getTextRender());
       loadingText2.removeFromRenderer(menu.getTextRender());
       if(!buttons.contains(retryButton)) retryButton.removeListener();
       client.disconnect();
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
        else if(action.equals("retry")) {
            failText.removeFromRenderer(menu.getTextRender());
            buttons.remove(retryButton);
            retryButton.removeFromRenderer(menu.getTextRender());
            connect();
        }
        else if(action.equals("back")) {
            menu.setCurrentState(Menu.MenuState.MAIN_MENU);
        }
    }

    /**
     * Established connection with the server
     * ASYNC WITH EVERYTHING ELSE - DON'T DIRECTLY UPDATE GRAPHICS HERE
     */
    @Override
    public void establishedConnection() {
        serverMessage = 1;
        serverUpdate = true;
    }

    /**
     * Lose connection with the server
     */
    @Override
    public void lostConnection() {
        serverMessage = 2;
        serverUpdate = true;
    }
}
