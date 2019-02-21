package com.bestialMania.object.beast;

import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.animation.Animation;
import com.bestialMania.animation.Pose;
import com.bestialMania.object.AnimatedObject;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformFloat;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.state.game.Floor;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Beast extends AnimatedObject {
    //TEST STUFF
    private static final float songBPM = 128;//PUMPED UP KICKS = 128. Running in the 90s = 159. Spaceghostpurp = 150 Change to make jimmy sync up to the song you pick
    private int t = 0;

    //constants
    private static final float GRAVITY = 0.006f;//gravity acceleration
    private static final float ACCELERATION = 0.01f;//lateral movement acceleration
    private static final float MIDAIR_ACCELERATION_MODIFIER = 0.05f;//multiply your acceleration by this number when midair
    private static final float MIDAIR_TURN_MODIFIER = 0.2f;//multiply your turning acceleration by this number when midair
    private static final float TURN_SPEED = 0.1f;//speed your model turns at
    private static final float FAST_TURN_SPEED = 0.2f;//speed you turn when "fast turning"
    private static final float FAST_TURN_ANGLE = (float)Math.PI*0.6f;//you must turn at least this angle amount to do a "fast turn"
    private static final float SPEED_JUMP_MULTIPLIER = 2.0f;//increasing this makes running increase your jump height much more.
    private static final float RUN_MODIFIER = 1.25f;//running speed modifier

    //character constants
    private float characterSpeed = 0.1f;
    private float characterJump = 0.08f;

    //PHYSICS
    private Vector3f position, positionInterpolate;//position vector(s)
    private Vector2f movementVector;//current movement vector
    private Vector2f intendedMovementVector;//intended movement vector to accelerate towards
    private Vector2f movementDirection;//intended direction to move towards
    private float angleTarget;//intended angle to face towards
    private float angle;//angle the model is facing TODO possibly animate turning around better
    private float speed;//current intended speed
    private float turnSpeed = TURN_SPEED;//turning speed
    private float yspeed;
    private float floorY;
    private boolean onGround;
    private boolean midairTurn = false;

    //collisions
    private Floor floor;

    //texture
    private Texture texture;

    //animations
    private Animation animation,animation2;

    /**
     * Create a beast
     */
    public Beast(Game game, AnimatedModel animatedModel, Texture texture){
        super(game, animatedModel, new Matrix4f());
        this.floor = game.getFloor();
        this.texture = texture;
        position = new Vector3f(0,0,0);//TODO have some sort of spawn point
        angle = 0;

        positionInterpolate = new Vector3f(0,0,0);
        movementDirection = new Vector2f((float)Math.sin(angle),(float)Math.cos(angle));
        movementVector = new Vector2f(0,0);
        intendedMovementVector = new Vector2f();
        angleTarget = angle;
        speed = 0;
        yspeed = 0;
        floorY = floor.getHeightAtLocation(position);
        onGround = position.y<=floorY;
        if(onGround) position.y=floorY;

        //TEST ANIMATION (trying to animation 2 parts separately)
        Set<String> testAffectedJoints = new HashSet<>(Arrays.asList("Dummy003","Dummy057","Dummy058","Dummy062","Dummy059","Dummy063","Dummy060","Dummy064","Dummy061"));//legs only
        Set<String> testAffectedJoints2 = animatedModel.getArmature().getJointsExcluding(testAffectedJoints);//rest of the body

        animation = new Animation(animatedModel.getArmature(),animatedModel.getPose(0),testAffectedJoints,true);
        animation2 = new Animation(animatedModel.getArmature(),animatedModel.getPose(0),testAffectedJoints2,true);
        int i = 0;
        float time = 0;


        float beatDuration = 60.0f/songBPM;


        while(i<12) {
            i++;
            time+=0.85*beatDuration;
            animation.addKeyFrame(time,animatedModel.getPose(i));
            animation2.addKeyFrame(time,animatedModel.getPose(i));
            i++;
            time+=0.15*beatDuration;
            animation.addKeyFrame(time,animatedModel.getPose(i));
            animation2.addKeyFrame(time,animatedModel.getPose(i));

        }
        animation.setCurrentTime(time+beatDuration*0.1f);
        animation2.setCurrentTime(time+beatDuration*0.1f);
        applyAnimation(animation);
        applyAnimation(animation2);

    }

    /**
     * Set a new direction
     */
    public void setDirection(Vector2f direction) {
        angleTarget = (float)Math.atan2(direction.x,direction.y);

        this.movementDirection = direction;
    }

    /**
     * Set a new speed
     */
    public void setSpeed(float speed,boolean running) {
        this.speed = speed*characterSpeed;
        if(running) this.speed*=RUN_MODIFIER;
    }

    /**
     * Jump
     */
    public void jump() {
        if(onGround) {
            float speedMultiplier = 1;
            speedMultiplier+=movementVector.length()*SPEED_JUMP_MULTIPLIER;

            yspeed = characterJump*speedMultiplier;


            //if(turnSpeed==FAST_TURN_SPEED) yspeed = 0.3f;//SUPER MARIO BACKFLIP
        }
    }

    /**
     * Get position
     */
    public Vector3f getPosition() {return position;}

    /**
     * Update position and orientation
     */
    @Override
    public void update() {
        //Please update the position BEFORE you change the speed and direction
        position.x+=movementVector.x;
        position.z+=movementVector.y;
        position.y+=yspeed;
        onGround = position.y<=floorY;
        //land on the ground
        if(onGround) {
            position.y = floorY;
            yspeed = 0;
            midairTurn = false;
        }


        //acceleration towards intended movement
        intendedMovementVector.x = movementDirection.x*speed;
        intendedMovementVector.y = movementDirection.y*speed;

        float accel = ACCELERATION;
        if(!onGround) accel*=MIDAIR_ACCELERATION_MODIFIER;

        if(movementVector.x<intendedMovementVector.x) {
            movementVector.x+=accel;
            if(movementVector.x>intendedMovementVector.x) movementVector.x = intendedMovementVector.x;
        }else if(movementVector.x>intendedMovementVector.x) {
            movementVector.x-=accel;
            if(movementVector.x<intendedMovementVector.x) movementVector.x = intendedMovementVector.x;
        }

        if(movementVector.y<intendedMovementVector.y) {
            movementVector.y+=accel;
            if(movementVector.y>intendedMovementVector.y) movementVector.y = intendedMovementVector.y;
        }else if(movementVector.y>intendedMovementVector.y) {
            movementVector.y-=accel;
            if(movementVector.y<intendedMovementVector.y) movementVector.y = intendedMovementVector.y;
        }

        //Get the height at which the next
        if(!onGround) yspeed-=GRAVITY;
        positionInterpolate.x = position.x+movementVector.x;
        positionInterpolate.z = position.z+movementVector.y;
        positionInterpolate.y = position.y+yspeed;
        floorY = floor.getHeightAtLocation(positionInterpolate);



        //FAST TURN AROUND
        if(getAngleDifference(angle,angleTarget)>FAST_TURN_ANGLE) {
            turnSpeed = FAST_TURN_SPEED;
        }

        float turnAmount = turnSpeed;
        if(midairTurn) turnAmount *=MIDAIR_TURN_MODIFIER;

        //turn to face direction of movement
        if(getAngleDifference(angle,angleTarget)<turnAmount) {
            angle = angleTarget;
            turnSpeed = TURN_SPEED;
            if(!onGround) midairTurn = true;
        }
        else if(angle>angleTarget) {
            if(angle-angleTarget>Math.PI) angle+=turnAmount;
            else angle-=turnAmount;
        }
        else {
            if(angleTarget-angle>Math.PI) angle-=turnAmount;
            else angle+=turnAmount;
        }

        if(angle>Math.PI) angle-=2*Math.PI;
        if(angle<-Math.PI) angle+=2*Math.PI;

        /*t++;
        if(t>200) {
            Pose pose = getCurrentPose();
            setPose(pose);
            cancelAnimation(animation2);
        }*/

        updateAnimation();
    }

    /**
     * Update matrices using the frame interpolation amount
     *
     * return the interpolated position
     */
    @Override
    public void interpolate(float frameInterpolation) {
        //interpolate position
        positionInterpolate.x = position.x+movementVector.x*frameInterpolation;
        positionInterpolate.z = position.z+movementVector.y*frameInterpolation;
        positionInterpolate.y = position.y+yspeed*frameInterpolation;
        if(positionInterpolate.y<floorY) {//landing on the ground
            positionInterpolate.y = floorY;
        }

        //interpolate direction
        float angleInterpolate = angle;
        float turnAmount = turnSpeed*frameInterpolation;
        if(midairTurn) turnAmount *=MIDAIR_TURN_MODIFIER;
        if(getAngleDifference(angle,angleTarget)<turnAmount) angleInterpolate = angleTarget;
        else if(angle>angleTarget) {
            if(angle-angleTarget>Math.PI) angleInterpolate+=turnAmount;
            else angleInterpolate-=turnAmount;
        }
        else {
            if(angleTarget-angle>Math.PI) angleInterpolate-=turnAmount;
            else angleInterpolate+=turnAmount;
        }

        //recalculate matrix
        modelMatrix.identity();

        //translation
        modelMatrix.translate(positionInterpolate);
        //rotation
        modelMatrix.rotateY(angleInterpolate);

        //scale
        modelMatrix.scale(0.1f,0.1f,0.1f);
        interpolateAnimation(frameInterpolation);

    }

    public Vector3f getPositionInterpolate() {
        return positionInterpolate;
    }

    /**
     * Add the model to the renderer
     */
    @Override
    public void linkToRenderer(Renderer renderer) {
        ShaderObject shaderObject = createShaderObject(renderer);
        linkTransformsToShaderObject(shaderObject);
        shaderObject.addTexture(0,texture);
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"reflectivity",0.2f));
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"shineDamper",5.0f));
    }


    public Model getModel() {return animatedModel.getModel();}
    public Matrix4f getMatrix() {
        return modelMatrix;
    }

    /**
     * Get the difference between 2 angles
     */
    private static float getAngleDifference(float angle1, float angle2) {
        float dif = Math.abs(angle1-angle2);
        if(dif<=Math.PI) return dif;
        else return ((float)Math.PI * 2.0f) - dif;
    }
}
