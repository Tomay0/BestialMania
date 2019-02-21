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
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Beast extends AnimatedObject {

    private static final float songBPM = 128;//PUMPED UP KICKS = 128. Running in the 90s = 159. Spaceghostpurp = 150 Change to make jimmy sync up to the song you pick
    private static final float ACCELERATION = 0.05f;
    private static final float TURN_SPEED = 0.1f;
    private static final float FAST_TURN_SPEED = 0.25f;
    private static final float FAST_TURN_ANGLE = (float)Math.PI/3.0f;

    //PHYSICS
    private Vector3f position, positionInterpolate;//position vector(s)
    private Vector2f movementVector;//current movement vector
    private Vector2f intendedMovementVector;//intended movement vector to accelerate towards
    private Vector2f movementDirection;//intended direction to move towards
    private float angleTarget;//intended angle to face towards
    private float angle;//angle the model is facing TODO possibly animate turning around better
    private float speed;//current intended speed
    private float turnSpeed = TURN_SPEED;//turning speed

    private Texture texture;
    private Animation animation,animation2;
    private int t = 0;

    /**
     * Create a beast
     */
    public Beast(Game game, AnimatedModel animatedModel, Texture texture){
        super(game, animatedModel, new Matrix4f());
        position = new Vector3f(0,0,0);//TODO have some sort of spawn point
        angle = 0;

        positionInterpolate = new Vector3f(0,0,0);
        movementDirection = new Vector2f((float)Math.sin(angle),(float)Math.cos(angle));
        movementVector = new Vector2f(0,0);
        intendedMovementVector = new Vector2f();
        angleTarget = angle;
        speed = 0;

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

        this.texture = texture;
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
    public void setSpeed(float speed) {
        this.speed = speed;
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


        //acceleration towards intended movement
        intendedMovementVector.x = movementDirection.x*speed;
        intendedMovementVector.y = movementDirection.y*speed;

        if(movementVector.x<intendedMovementVector.x) {
            movementVector.x+=ACCELERATION;
            if(movementVector.x>intendedMovementVector.x) movementVector.x = intendedMovementVector.x;
        }else if(movementVector.x>intendedMovementVector.x) {
            movementVector.x-=ACCELERATION;
            if(movementVector.x<intendedMovementVector.x) movementVector.x = intendedMovementVector.x;
        }

        if(movementVector.y<intendedMovementVector.y) {
            movementVector.y+=ACCELERATION;
            if(movementVector.y>intendedMovementVector.y) movementVector.y = intendedMovementVector.y;
        }else if(movementVector.y>intendedMovementVector.y) {
            movementVector.y-=ACCELERATION;
            if(movementVector.y<intendedMovementVector.y) movementVector.y = intendedMovementVector.y;
        }

        //FAST TURN AROUND
        if(Math.abs(angle-angleTarget)>FAST_TURN_ANGLE && Math.abs(angle-angleTarget)<2*Math.PI-FAST_TURN_ANGLE) {
            turnSpeed = FAST_TURN_SPEED;
        }

        //turn to face direction of movement
        if(Math.abs(angle-angleTarget)<turnSpeed || Math.abs(angle-angleTarget)>2*Math.PI-turnSpeed) {
            angle = angleTarget;
            turnSpeed = TURN_SPEED;
        }
        else if(angle>angleTarget) {
            if(angle-angleTarget>Math.PI) angle+=turnSpeed;
            else angle-=turnSpeed;
        }
        else {
            if(angleTarget-angle>Math.PI) angle-=turnSpeed;
            else angle+=turnSpeed;
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

        //interpolate direction
        float angleInterpolate = angle;
        float turnAmount = turnSpeed*frameInterpolation;
        if(Math.abs(angle-angleTarget)<turnAmount || Math.abs(angle-angleTarget)>2*Math.PI-turnAmount) angleInterpolate = angleTarget;
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
}
