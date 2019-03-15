package com.bestialMania.gui;

import com.bestialMania.InputHandler;
import com.bestialMania.InputListener;
import com.bestialMania.MemoryManager;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class TestButton extends Object2D implements InputListener {
    private String textOnButton;
    private String actionOnClick;//multiple buttons could be onscreen and could use the same listener, so use an action to distinguish different buttons
    private ButtonListener listener;//something to catch the button's click event
    private InputHandler inputHandler;//to get the mouse position


    /**
     *  Constructor for width and height being relative to texture
     */
    public TestButton(MemoryManager mm, InputHandler inputHandler, ButtonListener listener, int x, int y, String textureFileName, String textOnButton, String actionOnClick) {
        super(mm,x,y,textureFileName);
        this.textOnButton = textOnButton;
        this.actionOnClick = actionOnClick;
        this.inputHandler = inputHandler;
        this.listener = listener;
        inputHandler.addListener(this);
    }

    /**
     *  Constructor for set width and height
     */
    public TestButton(MemoryManager mm, InputHandler inputHandler, ButtonListener listener, int x, int y, int width, int height, String textureFileName, String textOnButton, String actionOnClick){
        super(mm,x,y,width,height,textureFileName);
        this.textOnButton = textOnButton;
        this.actionOnClick = actionOnClick;
        this.inputHandler = inputHandler;
        this.listener = listener;
        inputHandler.addListener(this);
    }

    /**
     * Method that checks if the mouse position is on a button
     */
    public boolean mouseOn(Vector2f mousePos){
        return (mousePos.x >= x && mousePos.x <= x + width  && mousePos.y >= y && mousePos.y <= y + height );
    }

    /**
     * Remove the listener when this button is no longer in use
     */
    public void removeListener() {
        inputHandler.removeListener(this);
    }

    //Mouse selection
    @Override
    public void mouseEvent(boolean pressed, int button) {
        if(!pressed && button==GLFW_MOUSE_BUTTON_LEFT) {
            if(mouseOn(inputHandler.getMousePosition())) {
                listener.press(actionOnClick);
            }
        }
    }

    //TODO allow selecting with keyboard/controller?
    @Override
    public void keyEvent(boolean pressed, int key) {}
    @Override
    public void controllerEvent(int controller, boolean pressed, int button) {}
}
