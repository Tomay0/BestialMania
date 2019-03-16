package com.bestialMania.gui;

import com.bestialMania.InputHandler;
import com.bestialMania.InputListener;
import com.bestialMania.MemoryManager;
import com.bestialMania.gui.text.Font;
import com.bestialMania.gui.text.Text;
import com.bestialMania.rendering.Renderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Button implements InputListener {
    private static final Vector3f DEFAULT_COLOR = new Vector3f(0,255,0);
    private static final Vector3f HIGHLIGHT_COLOR = new Vector3f(255,255,255);

    private String action;
    private ButtonListener listener;
    private InputHandler inputHandler;
    private int x, y, width, height;
    private Text text;
    private Vector3f color;

    /**
     * Create a Button that is only text
     * @param mm Memory manager
     * @param inputHandler Input handler
     * @param listener Listener for when the button is clicked
     * @param font Font of the button
     * @param x Centre x position
     * @param y Centre y position
     * @param width Width of the button's bounding box
     * @param text Text on the button
     * @param action Action to call when the button is pressed
     */
    public Button(MemoryManager mm, InputHandler inputHandler, ButtonListener listener, Font font, int x, int y, int width, int fontSize, String text, String action) {
        this.color = new Vector3f(DEFAULT_COLOR.x,DEFAULT_COLOR.y,DEFAULT_COLOR.z);
        this.text = new Text(mm,text,font,fontSize,x,y-fontSize/4, Text.TextAlign.CENTER,color);
        this.x = x-width/2;
        this.y = y-fontSize/2;
        this.width = width;
        this.height = fontSize;
        this.action = action;
        this.listener = listener;
        this.inputHandler = inputHandler;
    }

    /**
     * Method that checks if the mouse position is on a button
     */
    public boolean mouseOn(Vector2f mousePos){
        return (mousePos.x >= x && mousePos.x <= x + width  && mousePos.y >= y && mousePos.y <= y + height );
    }

    /**
     * Add text to the renderer
     */
    public void add(Renderer renderer) {
        inputHandler.addListener(this);
        text.addToRenderer(renderer);
    }
    /**
     * Remove text from the renderer
     */
    public void remove(Renderer renderer) {
        inputHandler.removeListener(this);
        text.removeFromRenderer(renderer);
    }

    /**
     * Update if the mouse is hovering over the text
     */
    public void update() {
        if(mouseOn(inputHandler.getMousePosition())) {
            color.x = HIGHLIGHT_COLOR.x;
            color.y = HIGHLIGHT_COLOR.y;
            color.z = HIGHLIGHT_COLOR.z;
        }
        else {
            color.x = DEFAULT_COLOR.x;
            color.y = DEFAULT_COLOR.y;
            color.z = DEFAULT_COLOR.z;
        }
    }

    /**
     * Release left mouse button
     */
    @Override
    public void mouseEvent(boolean pressed, int button) {
        if(!pressed && button==GLFW_MOUSE_BUTTON_LEFT) {
            if(mouseOn(inputHandler.getMousePosition())) {
                listener.press(action);
            }
        }
    }

    @Override
    public void controllerEvent(int controller, boolean pressed, int button) {}
    @Override
    public void keyEvent(boolean pressed, int key) { }
}
