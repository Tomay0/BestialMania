package com.bestialMania.object.beast;

import com.bestialMania.InputHandler;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player {
    private InputHandler inputHandler;
    private int playerNum;//player number from 1-4 showing the location on the screen
    private int controller;//controller id
    private Beast beast;

    /*
        CAMERA POSITION STUFF
     */
    private float cameraYaw, cameraPitch;//yaw = Y-axis rotation. pitch = X-axis rotation
    private float cameraDist;//distance from the camera to the beast
    private Matrix4f viewMatrix;

    /**
     * Handles the controlling of the beast with a controller or keyboard/mouse.
     * Also handles the camera.
     */
    public Player(InputHandler inputHandler, int playerNum, int controller, Beast beast) {
        this.inputHandler = inputHandler;
        this.playerNum = playerNum;
        this.controller = controller;
        this.beast = beast;
        cameraYaw = 0;
        cameraPitch = 0;
        cameraDist = 3;
        //test view matrix TODO
        viewMatrix = new Matrix4f();
        viewMatrix.lookAt(new Vector3f(0,0.5f,2),new Vector3f(0,0,0),new Vector3f(0,1,0));
    }

    /**
     * Link the camera's view matrix to the renderer
     */
    public void linkToRenderer(Renderer renderer) {
        renderer.addUniform(new UniformMatrix4(renderer.getShader(),"viewMatrix",viewMatrix));
    }

    /**
     * Update the beast
     */
    public void update() {
        /*
            BEAST MOVEMENT
         */

        float speed;//"speed" of the controller (Eg: smaller if you lightly push the left analog stick up
        Vector2f dir;//direction your controller is pointing RIGHT = POSITIVE X. DOWN = POSITIVE Y
        //keyboard
        if(controller==-1) {
            speed = 0;
            dir = new Vector2f((inputHandler.isKeyPressed(GLFW_KEY_D) ? 1 : 0)-(inputHandler.isKeyPressed(GLFW_KEY_A) ? 1 : 0),
                    (inputHandler.isKeyPressed(GLFW_KEY_S) ? 1 : 0)-(inputHandler.isKeyPressed(GLFW_KEY_W) ? 1 : 0));
            if(dir.x==0&&dir.y==0) speed = 0;
            else speed = 1;
        }
        //controller
        else {
            dir = inputHandler.gamepadLeftJoystickPosition(controller);
            speed = dir.length();
        }
        //only move if the speed is high enough (as there may be noise in the analog stick)
        if(speed>0.1f) {
            beast.setSpeed(speed*0.1f);
            dir.normalize();
            beast.setDirection(dir);
        }else beast.setSpeed(0);
        beast.update();
    }
}
