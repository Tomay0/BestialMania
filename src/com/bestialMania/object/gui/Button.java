package com.bestialMania.object.gui;
//TODO: Implement text on button

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.Main;
public class Button{

    private int x, y, width, height;
    private String textureFileName; //width and height can be relative to texture provided
    private Object2D buttonRepresentation;
    private String textOnButton;
    private String actionOnClick;
    private Main main = Main.main;

    /**
     *  Constructor for width and height being relative to texture
     */
    public Button(int x, int y, String textureFileName, String textOnButton, String actionOnClick) {
        this.x = x;
        this.y = y;
        this.textureFileName = textureFileName;
        this.textOnButton = textOnButton;
        this.actionOnClick = actionOnClick;
        buttonRepresentation = new Object2D(x, y, textureFileName);
        this.width = buttonRepresentation.getObject2DTexture().getWidth();
        this.height = buttonRepresentation.getObject2DTexture().getHeight();
    }

    /**
     *  Constructor for set width and height
     */
    public Button(int x, int y, int width, int height, String textureFileName, String textOnButton, String actionOnClick){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureFileName = textureFileName;
        this.textOnButton = textOnButton;
        this.actionOnClick = actionOnClick;
        buttonRepresentation = new Object2D(x, y, width, height, textureFileName);
    }

    /**
     * Method that controls the action that each button will undertake upon being clicked
     */
    public void doAction(){
        if(actionOnClick.equals("Quit")) main.terminate();
        else if(actionOnClick.equals("Play")) {
            System.out.println("Play state");
            main.setCurrentState(Main.State.IN_GAME);
        }

    }

    /**
     * Binds this object to a renderer
     */
    public void addToRenderer(Renderer renderer) {
        ShaderObject shaderObject  = renderer.createObject(buttonRepresentation.getObject2DModel());
        shaderObject.addTexture(0, buttonRepresentation.getObject2DTexture());
        shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix", buttonRepresentation.getMatrix()));
    }

    /*Getters*/
    public int getX(){return x;}
    public int getY(){return y;}
    public int getWidth(){return width;}
    public int getHeight(){return height;}
}
