package com.bestialMania.state.menu;

import com.bestialMania.Main;
import com.bestialMania.Settings;
import com.bestialMania.InputHandler;
import com.bestialMania.gui.Button;
import com.bestialMania.rendering.MasterRenderer;

public class MainMenu extends SubMenu {
    /**
     * Setup the main menu
     */
    public MainMenu(Menu menu, InputHandler inputHandler, MasterRenderer masterRenderer) {
        super(menu,inputHandler,masterRenderer);

        //ADD SEVERAL BUTTONS TO THE SCREEN
        int y = Settings.HEIGHT/2-50;
        Button tutorialButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH/2, y, 200, 60,
                "TUTORIAL","tutorial");
        buttons.add(tutorialButton);

        y+=80;
        Button localButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH/2, y, 200, 60,
                "LOCAL","local");
        buttons.add(localButton);

        y+=80;
        Button onlineButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH/2, y, 200, 60,
                "ONLINE","online");
        buttons.add(onlineButton);

        y+=80;
        Button quitButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH/2, y, 200, 60,
                "QUIT","quit");
        buttons.add(quitButton);


        //collision update button - development purposes only
        Button collisionsButton = new Button(memoryManager,inputHandler,this, menu.getFont(),
                Settings.WIDTH-200,Settings.HEIGHT-160, 200, 30,
                "Convert Resources","rf");
        buttons.add(collisionsButton);

        linkButtonsToRenderers();
    }

    @Override
    public void menuUpdate() {}
    @Override
    public void menuRemoveObjects() {}

    /**
     * Button actions
     */
    @Override
    public void press(String action) {
        if(action.equals("tutorial")) {
            //TUTORIAL - START A GAME WITH ONLY THE KEYBOARD/MOUSE PLAYER
            menu.getConnectedPlayers().add(-1);
            menu.startGame();
        }
        else if(action.equals("local")) {
            menu.setCurrentState(Menu.MenuState.PLAYER_SELECT);
        }
        else if(action.equals("online")) {
            menu.setCurrentState(Menu.MenuState.ONLINE_MENU);
        }
        else if(action.equals("quit")) {
            menu.quitGame();
        }else if(action.equals("rf")) {
            Main.reloadAllFiles();
        }
    }
}
