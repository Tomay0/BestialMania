package com.bestialMania.state.menu.online;

import com.bestialMania.InputHandler;
import com.bestialMania.Settings;
import com.bestialMania.gui.Button;
import com.bestialMania.gui.text.Text;
import com.bestialMania.network.Client;
import com.bestialMania.network.message.inbound.InboundMessage;
import com.bestialMania.network.message.inbound.InboundReadyMessage;
import com.bestialMania.network.message.inbound.InfoMessage;
import com.bestialMania.network.message.inbound.JoinMessage;
import com.bestialMania.network.message.outbound.ReadyMessage;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.state.menu.Menu;
import com.bestialMania.state.menu.SubMenu;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class OnlineWaitingMenu extends SubMenu {
    //client connection to the server
    private Client client;

    //maximum players as given by the server you join
    private int maxPlayers = 1;

    //connected players not including yourself
    private List<WaitingRoomPlayer> waitingRoomPlayers = new ArrayList<>();

    //gui elements
    private Text loadingText, playerCountText, playerNameText, readyText;
    private Button retryButton, readyButton, notReadyButton;
    /**
     * Setup the main menu
     */
    public OnlineWaitingMenu(Menu menu, InputHandler inputHandler, MasterRenderer masterRenderer) {
        super(menu,inputHandler,masterRenderer);

        //back button (on bottom)
        Button backButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH/2, Settings.HEIGHT-100, 200, 60,
                "BACK","back");
        addButton(backButton);

        //loading text
        loadingText = new Text(memoryManager,"Connecting to server...",menu.getFont(),40,Settings.WIDTH/2-400,100, Text.TextAlign.LEFT,new Vector3f(1,0,0));
        loadingText.addToRenderer(menu.getTextRender());
        //retry button
        retryButton = new Button(memoryManager,inputHandler,this,menu.getFont(),
                Settings.WIDTH/2+450,100,200,40,
                "RETRY","retry");
        //text to show player count
        playerCountText = new Text(memoryManager,"0/0",menu.getFont(),40,Settings.WIDTH/2+300,100,Text.TextAlign.RIGHT,new Vector3f(1,0,0));
        playerCountText.addToRenderer(menu.getTextRender());

        //player name
        playerNameText = new Text(memoryManager,"<Insert username here>",menu.getFont(),40,Settings.WIDTH/2-400,160,Text.TextAlign.LEFT,new Vector3f(1,0.5f,0));
        playerNameText.addToRenderer(menu.getTextRender());
        readyText = new Text(memoryManager,"Waiting",menu.getFont(),40,Settings.WIDTH/2+300,160,Text.TextAlign.RIGHT,new Vector3f(1,0.5f,0));
        readyText.addToRenderer(menu.getTextRender());

        //everyone is ready/cancel button
        readyButton = new Button(memoryManager,inputHandler,this,menu.getFont(),
                Settings.WIDTH/2+450,100,300,40,
                "Everyone is ready","ready");
        notReadyButton = new Button(memoryManager,inputHandler,this,menu.getFont(),
                Settings.WIDTH/2+450,100,300,40,
                "Cancel","nready");

        connect();
    }

    /**
     * Set up the client and try to connect
     */
    private void connect() {
        client = new Client("localhost",6969);
        loadingText.setText("Connecting to server...");
    }

    /**
     * Update method
     * - mostly polls messages from the client's message queue
     * done in the main thread.
     */
    @Override
    public void menuUpdate() {
        if(client==null) return;
        //poll inbound messages from the server
        while(client.getQueue().hasMessages()) {
            InboundMessage message = client.getQueue().readMessage();
            //join message
            if(message.getCode()=='J') {
                JoinMessage joinMessage = (JoinMessage) message;
                if(joinMessage.getSelf()) {
                    //connected to the server
                    if(joinMessage.getJoined()) {
                        loadingText.setText("Connected to the server. Waiting for players..");
                        addButton(readyButton);
                    }
                    //disconnected from the server
                    else {
                        loadingText.setText("Could not connect to the server.");
                        playerCountText.setText("0/0");
                        removeButton(readyButton);
                        removeButton(notReadyButton);
                        readyText.setText("Waiting");
                        addButton(retryButton);
                        for(WaitingRoomPlayer waitingRoomPlayer : waitingRoomPlayers) {
                            waitingRoomPlayer.cleanUp();
                        }
                        waitingRoomPlayers.clear();
                    }
                }
                else {
                    //player joined the waiting room
                    if(joinMessage.getJoined()) {
                        waitingRoomPlayers.add(new WaitingRoomPlayer(joinMessage.getId(),menu.getFont(),220 +waitingRoomPlayers.size()*60,menu.getTextRender()));
                    }
                    //player left the waiting room
                    else {
                        //find row on screen that has the player
                        int index = 0;
                        while(waitingRoomPlayers.get(index).getId()!=joinMessage.getId()) {
                            index++;
                            if(index>=waitingRoomPlayers.size()) break;
                        }

                        if(index<waitingRoomPlayers.size()) {
                            //remove the player's row
                            waitingRoomPlayers.get(index).cleanUp();
                            waitingRoomPlayers.remove(index);

                            //shift all players afterwards up
                            for(int i = index;i<waitingRoomPlayers.size();i++) {
                                waitingRoomPlayers.get(i).setY(220+i*60);
                            }
                        }
                    }
                    updatePlayerCount();
                }

            }
            //info message
            else if(message.getCode()=='i') {
                InfoMessage infoMessage = (InfoMessage) message;
                //set player name TODO implement actual usernames
                int id = infoMessage.getId();
                playerNameText.setText("Player"+id);


                //set max players
                this.maxPlayers = infoMessage.getMaxPlayers();
                updatePlayerCount();
            }
            //ready message
            else if(message.getCode()=='r') {
                InboundReadyMessage readyMessage = (InboundReadyMessage) message;
                int id = readyMessage.getId();
                boolean ready = readyMessage.isReady();
                for(WaitingRoomPlayer waitingRoomPlayer : waitingRoomPlayers) {
                    if(waitingRoomPlayer.getId()==id) {
                        waitingRoomPlayer.setReady(ready);
                        break;
                    }
                }
            }

        }
    }

    /**
     * Update the player count in the top right
     */
    private void updatePlayerCount() {
        playerCountText.setText((1+waitingRoomPlayers.size()) + "/" + maxPlayers);
    }

    /**
     * Remove objects
     */
    @Override
    public void menuRemoveObjects() {
       loadingText.removeFromRenderer(menu.getTextRender());
       playerCountText.removeFromRenderer(menu.getTextRender());
       playerNameText.removeFromRenderer(menu.getTextRender());
       readyText.removeFromRenderer(menu.getTextRender());
       for(WaitingRoomPlayer waitingRoomPlayer : waitingRoomPlayers) {
           waitingRoomPlayer.cleanUp();
       }
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
            removeButton(retryButton);
            connect();
        }
        else if(action.equals("back")) {
            menu.setCurrentState(Menu.MenuState.MAIN_MENU);
        }
        else if(action.equals("ready")) {
            removeButton(readyButton);
            addButton(notReadyButton);
            readyText.setText("Ready");
            client.sendMessage(new ReadyMessage(true));
        }
        else if(action.equals("nready")) {
            removeButton(notReadyButton);
            addButton(readyButton);
            readyText.setText("Waiting");
            client.sendMessage(new ReadyMessage(false));
        }
    }

}
