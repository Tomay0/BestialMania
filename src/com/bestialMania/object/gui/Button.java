package com.bestialMania.object.gui;
//TODO: Implement text on button

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.Main;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class Button extends Object2D{
    private String textOnButton;
    private String actionOnClick;
    private Main main = Main.main;

    /**
     *  Constructor for width and height being relative to texture
     */
    public Button(int x, int y, String textureFileName, String textOnButton, String actionOnClick) {
        super(x,y,textureFileName);
        this.textOnButton = textOnButton;
        this.actionOnClick = actionOnClick;
    }

    /**
     *  Constructor for set width and height
     */
    public Button(int x, int y, int width, int height, String textureFileName, String textOnButton, String actionOnClick){
        super(x,y,width,height,textureFileName);
        this.textOnButton = textOnButton;
        this.actionOnClick = actionOnClick;
    }

    /**
     * Method that controls the action that each button will undertake upon being clicked
     */
    public void doAction(){
        if(actionOnClick.equals("Quit")) main.quit();
        else if(actionOnClick.equals("Play")) {
            System.out.println("Play state");
            main.setupGameState();
        }

    }

    /**
     * Method that checks if the mouse position is on a button
     */
    public boolean mouseOn(Vector2f mousePos){
        return (mousePos.x >= x && mousePos.x <= x + width  && mousePos.y >= y && mousePos.y <= y + height );
    }
}
