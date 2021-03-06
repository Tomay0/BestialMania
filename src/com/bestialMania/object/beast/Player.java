package com.bestialMania.object.beast;

import com.bestialMania.InputListener;
import com.bestialMania.Settings;
import com.bestialMania.InputHandler;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.blur.BlurRenderer;
import com.bestialMania.rendering.shader.UniformFloat;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player implements InputListener {
    private static Vector2f SCREEN_CENTER = new Vector2f(Settings.WIDTH/2, Settings.HEIGHT/2);
    private static final float MIN_PITCH = -(float)Math.PI*0.4f;
    private static final float MAX_PITCH = (float)Math.PI*0.49f;
    private static final Vector3f ORIGIN = new Vector3f(0,0,0);
    private InputHandler inputHandler;
    private int playerNum;//player number from 1-4 showing the location on the screen
    private int controller;//controller id
    private int jumpFrames = 0;//you can jump 5 frames before you land
    private Beast beast;

    /*
        CAMERA POSITION STUFF
     */
    private float cameraYaw, cameraPitch;//yaw = Y-axis rotation. pitch = X-axis rotation
    private float cameraDist, cameraHeight;//distance from the camera to the beast

    private Vector2f cameraMoveVec;

    private Vector3f cameraLocation,lookLocation,upVector, dirVector;
    private Matrix4f viewMatrix, viewDirMatrix;

    private UniformFloat alpha;
    private BlurRenderer blurRenderer;

    /*
     * RENDERING STUFF
     */
    //private Framebuffer playerWindow;
    //private Renderer renderer;


    /**
     * Handles the controlling of the beast with a controller or keyboard/mouse.
     * Also handles the camera.
     */
    public Player(InputHandler inputHandler, int playerNum, int controller, Beast beast, BlurRenderer blurRenderer) {
        this.inputHandler = inputHandler;
        this.playerNum = playerNum;
        this.controller = controller;
        this.beast = beast;
        this.blurRenderer = blurRenderer;
        cameraYaw = 0;
        cameraPitch = 0.3f;
        cameraDist = 3;
        cameraHeight = 0.7f;
        cameraLocation = new Vector3f();
        lookLocation = new Vector3f();
        cameraMoveVec = new Vector2f();
        upVector = new Vector3f(0,1,0);
        dirVector = new Vector3f();

        viewMatrix = new Matrix4f();
        viewMatrix.lookAt(new Vector3f(0,0.5f,2),new Vector3f(0,0,0),new Vector3f(0,1,0));

        viewDirMatrix = new Matrix4f();

        inputHandler.addListener(this);
    }

    /**
     * Remove this object from the listeners
     */
    public void cleanUp() {
        inputHandler.removeListener(this);
        beast.cleanUp();
    }

    /**
     * Get the beast object
     */
    public Beast getBeast() {
        return beast;
    }

    /**
     * Get the view matrix
     */
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    /**
     * Link the camera's view matrix to the renderer
     */
    public void linkCameraToRenderer(Renderer renderer) {
        renderer.addUniform(new UniformMatrix4(renderer.getShader(),"viewMatrix",viewMatrix));
    }

    /**
     * Link the camera's direction view matrix to the renderer
     */
    public void linkCameraDirectionToRenderer(Renderer renderer) {
        renderer.addUniform(new UniformMatrix4(renderer.getShader(),"viewMatrix",viewDirMatrix));
    }

    /**
     * Update the beast
     */
    public void update() {
        if(controller!=-1) if(!inputHandler.isControllerActive(controller)) System.err.println("Controller ID " + controller + " is inactive!");
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
            cameraMoveVec.x = (float)(mousePos.x- SCREEN_CENTER.x)* Settings.HORIZONTAL_MOUSE_SENSITIVITY;
            cameraMoveVec.y = (float)(mousePos.y- SCREEN_CENTER.y)* Settings.VERTICAL_MOUSE_SENSITIVITY;
            inputHandler.setMousePosition(SCREEN_CENTER);
        }else if(inputHandler.isControllerActive(controller)){
            //right analog camera controls
            Vector2f rightAnalog = inputHandler.gamepadRightJoystickPosition(controller);

            cameraMoveVec.x = rightAnalog.x * Settings.HORIZONTAL_CONTROLLER_CAMERA_SENSITIVITY;
            cameraMoveVec.y = rightAnalog.y * Settings.VERTICAL_CONTROLLER_CAMERA_SENSITIVITY;
            if(Math.abs(cameraMoveVec.x)< Settings.HORIZONTAL_CONTROLLER_CAMERA_SENSITIVITY*0.4f) cameraMoveVec.x = 0;
            if(Math.abs(cameraMoveVec.y)< Settings.VERTICAL_CONTROLLER_CAMERA_SENSITIVITY*0.4f) cameraMoveVec.y = 0;
        }
        //motion blur
        if(Settings.MOTION_BLUR>0) {
            blurRenderer.setBlur(Math.abs(cameraMoveVec.x*Settings.MOTION_BLUR),Math.abs(cameraMoveVec.y*Settings.MOTION_BLUR));
        }

        /*

            MOVE BEAST

         */

        float speed;//"speed" of the controller (Eg: smaller if you lightly push the left analog stick up
        Vector2f dir;//direction your controller is pointing RIGHT = POSITIVE X. DOWN = POSITIVE Y
        boolean running=false;//if you are running
        //JUMP
        if(jumpFrames>0) {
            jumpFrames++;

            if(beast.jump() || jumpFrames>5) jumpFrames = 0;
        }

        //keyboard
        if(controller==-1) {
            speed = 0;
            dir = new Vector2f((inputHandler.isKeyPressed(GLFW_KEY_D) ? 1 : 0)-(inputHandler.isKeyPressed(GLFW_KEY_A) ? 1 : 0),
                    (inputHandler.isKeyPressed(GLFW_KEY_S) ? 1 : 0)-(inputHandler.isKeyPressed(GLFW_KEY_W) ? 1 : 0));
            if(dir.x==0&&dir.y==0) speed = 0;
            else speed = 1;

            //run
            if(inputHandler.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) running = true;
            //crouch
            beast.crouch(inputHandler.isKeyPressed(GLFW_KEY_LEFT_CONTROL));
        }
        //controller
        else if(inputHandler.isControllerActive(controller)){
            dir = inputHandler.gamepadLeftJoystickPosition(controller);
            speed = dir.length();
            speed-=0.4f;//remove noise
            speed/=0.6;

            //run
            if(inputHandler.isGamepadButtonPressed(controller,9)) running = true;
            //crouch
            beast.crouch(inputHandler.isGamepadButtonPressed(controller,1));
        }
        //no controller connected
        else{
            speed = 0;
            dir = new Vector2f(0,0);
        }

        //only move if the speed is high enough (as there may be noise in the analog stick)
        if(speed>0) {
            if(speed>1) speed=1;
            beast.setSpeed(speed,running);
            dir.normalize();

            Vector2f rotatedDir = new Vector2f();
            float yawSinus = (float)Math.sin(cameraYaw);
            float yawCosinus = (float)Math.cos(cameraYaw);
            rotatedDir.x = dir.x*yawCosinus-dir.y*yawSinus;
            rotatedDir.y = dir.x*yawSinus+dir.y*yawCosinus;
            beast.setDirection(rotatedDir);
        }else beast.setSpeed(0,false);


        //update the beast's physics
        beast.updatePhysics();
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
        Vector3f beastLocation = beast.getPositionInterpolate();
        cameraLocation.x = beastLocation.x - cameraDist*yawSinus*pitchCosinus;
        cameraLocation.z = beastLocation.z + cameraDist*yawCosinus*pitchCosinus;
        cameraLocation.y = beastLocation.y + cameraDist*pitchSinus + cameraHeight;

        //get looking location
        lookLocation.x = beastLocation.x;
        lookLocation.y = beastLocation.y+cameraHeight;
        lookLocation.z = beastLocation.z;

        //calculate if the camera will be behind a wall
        float collision = beast.getCollisionHandler().getTriangleIntersection(lookLocation,cameraLocation);
        float alpha = 1.0f;
        if(collision<1) {
            collision-=0.07f;
            if(collision<=0.001) collision = 0.001f;
            //camera is behind the wall, move it so you're infront
            cameraLocation.x = beastLocation.x - cameraDist*yawSinus*pitchCosinus*collision;
            cameraLocation.z = beastLocation.z + cameraDist*yawCosinus*pitchCosinus*collision;
            cameraLocation.y = beastLocation.y + cameraDist*pitchSinus*collision + cameraHeight;
            //
            // TODO add transparency when you get too close
            // float newDist = cameraDist*collision;
            if(collision<0.25f) {
                alpha = (collision-0.05f)*5f;
                if(alpha<0) alpha = 0;
            }
        }
        this.alpha.setValue(alpha);

        //change look to dir
        lookLocation.sub(cameraLocation,dirVector);

        //matrix calculation
        viewMatrix.identity();
        viewMatrix.lookAt(cameraLocation,lookLocation,upVector);

        viewDirMatrix.identity();
        viewDirMatrix.lookAt(ORIGIN,dirVector,upVector);

    }

    /**
     * Link to the renderer that is used to render the character in its own framebuffer
     * Separate because an additional alpha value
     */
    public void linkToPlayerRenderer(Renderer renderer) {
        ShaderObject shaderObject = beast.linkToRenderer(renderer);
        alpha = new UniformFloat(renderer.getShader(),"alpha",1.0f);
        shaderObject.addUniform(alpha);
    }



    /**
     * Controller button presses
     */
    @Override
    public void controllerEvent(int controller, boolean pressed, int button) {
        if(controller==this.controller) {
            if(pressed) {
                //jump
                if(button==0) {
                    jumpFrames++;
                }
            }
        }
    }

    /**
     * Keyboard button presses
     */
    @Override
    public void keyEvent(boolean pressed, int key) {
        if(controller==-1) {
            if(pressed) {
                //jump
                if(key==GLFW_KEY_SPACE) {
                    jumpFrames++;
                }
            }
        }
    }

    /**
     * Mouse button presses
     */
    @Override
    public void mouseEvent(boolean pressed, int button) {
        if(controller==-1) {
            if(pressed) {

            }
        }
    }
}
