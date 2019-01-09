package com.bestialMania.object.beast;

import com.bestialMania.DisplaySettings;
import com.bestialMania.InputHandler;
import com.bestialMania.rendering.Framebuffer;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player {
    private static Vector2f SCREEN_CENTER = new Vector2f(DisplaySettings.WIDTH/2,DisplaySettings.HEIGHT/2);
    private static final float MIN_PITCH = -(float)Math.PI*0.1f;
    private static final float MAX_PITCH = (float)Math.PI*0.49f;
    private InputHandler inputHandler;
    private int playerNum;//player number from 1-4 showing the location on the screen
    private int controller;//controller id
    private Beast beast;

    /*
        CAMERA POSITION STUFF
     */
    private float cameraYaw, cameraPitch;//yaw = Y-axis rotation. pitch = X-axis rotation
    private float cameraDist, cameraHeight;//distance from the camera to the beast

    private Vector2f cameraMoveVec;

    private Vector3f cameraLocation,lookLocation,upVector;
    private Matrix4f viewMatrix;

    /*
     * RENDERING STUFF
     */
    //private Framebuffer playerWindow;
    //private Renderer renderer;


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
        cameraPitch = 0.3f;
        cameraDist = 3;
        cameraHeight = 0.7f;
        cameraLocation = new Vector3f();
        lookLocation = new Vector3f();
        cameraMoveVec = new Vector2f();
        upVector = new Vector3f(0,1,0);
        //test view matrix TODO
        viewMatrix = new Matrix4f();
        viewMatrix.lookAt(new Vector3f(0,0.5f,2),new Vector3f(0,0,0),new Vector3f(0,1,0));
    }

    /**
     * Get the beast object
     */
    public Beast getBeast() {
        return beast;
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
        //update to correct camera direction
        cameraYaw+=cameraMoveVec.x;
        cameraPitch+=cameraMoveVec.y;
        if(cameraPitch<MIN_PITCH) cameraPitch = MIN_PITCH;
        if(cameraPitch>MAX_PITCH) cameraPitch = MAX_PITCH;

        /*

            CHANGE CAMERA ANGLES

         */
        //mouse camera controls
        if(controller==-1) {
            Vector2f mousePos = inputHandler.getMousePosition();
            cameraMoveVec.x = (float)(mousePos.x- SCREEN_CENTER.x)*DisplaySettings.HORIZONTAL_MOUSE_SENSITIVITY;
            cameraMoveVec.y = (float)(mousePos.y- SCREEN_CENTER.y)*DisplaySettings.VERTICAL_MOUSE_SENSITIVITY;
            inputHandler.setMousePosition(SCREEN_CENTER);
        }else{
            //right analog camera controls
            Vector2f rightAnalog = inputHandler.gamepadRightJoystickPosition(controller);

            cameraMoveVec.x = rightAnalog.x * DisplaySettings.HORIZONTAL_CONTROLLER_CAMERA_SENSITIVITY;
            cameraMoveVec.y = rightAnalog.y * DisplaySettings.VERTICAL_CONTROLLER_CAMERA_SENSITIVITY;
            if(Math.abs(cameraMoveVec.x)<DisplaySettings.HORIZONTAL_CONTROLLER_CAMERA_SENSITIVITY*0.4f) cameraMoveVec.x = 0;
            if(Math.abs(cameraMoveVec.y)<DisplaySettings.VERTICAL_CONTROLLER_CAMERA_SENSITIVITY*0.4f) cameraMoveVec.y = 0;
        }

        /*

            MOVE BEAST

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
        if(speed>0.4f) {
            if(speed>1) speed=1;
            beast.setSpeed(speed*0.1f);
            dir.normalize();

            Vector2f rotatedDir = new Vector2f();
            float yawSinus = (float)Math.sin(cameraYaw);
            float yawCosinus = (float)Math.cos(cameraYaw);
            rotatedDir.x = dir.x*yawCosinus-dir.y*yawSinus;
            rotatedDir.y = dir.x*yawSinus+dir.y*yawCosinus;
            beast.setDirection(rotatedDir);
        }else beast.setSpeed(0);

        //update the beast
        beast.update();
    }

    /**
     * Update matrices using the frame interpolation amount
     */
    public void interpolate(float frameInterpolation) {
        //calculate interpolated yaw/pitch
        float yaw = cameraYaw + cameraMoveVec.x*frameInterpolation;
        float pitch = cameraPitch + cameraMoveVec.y*frameInterpolation;
        if(pitch<MIN_PITCH) pitch = MIN_PITCH;
        if(pitch>MAX_PITCH) pitch = MAX_PITCH;

        //sinus/cosinus for useful calculations
        float yawSinus = (float)Math.sin(yaw);
        float yawCosinus = (float)Math.cos(yaw);
        float pitchSinus = (float)Math.sin(pitch);
        float pitchCosinus = (float)Math.cos(pitch);

        //get camera location
        Vector3f beastLocation = beast.interpolate(frameInterpolation);
        cameraLocation.x = beastLocation.x - cameraDist*yawSinus*pitchCosinus;
        cameraLocation.z = beastLocation.z + cameraDist*yawCosinus*pitchCosinus;
        cameraLocation.y = beastLocation.y + cameraDist*pitchSinus + cameraHeight;

        //get looking location
        lookLocation.x = beastLocation.x;
        lookLocation.y = beastLocation.y+cameraHeight;
        lookLocation.z = beastLocation.z;

        //matrix calculation
        viewMatrix.identity();
        viewMatrix.lookAt(cameraLocation,lookLocation,upVector);
    }
}
