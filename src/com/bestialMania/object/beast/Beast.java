package com.bestialMania.object.beast;

import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.animation.Animation;
import com.bestialMania.collision.CollisionHandler;
import com.bestialMania.collision.TriangleEdge;
import com.bestialMania.object.AnimatedObject;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformFloat;
import com.bestialMania.sound.Sound;
import com.bestialMania.sound.SoundSource;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

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
    private static final float TERMINAL_VELOCITY = 0.5f;//fastest speed you can fall

    public static final float UPHILL_CLIMB_HEIGHT = 0.7f;//how step of an angle you can climb in one movement. Make this larger than terminal velocity to avoid falling through the floor. Note that you will not climb this height if there is a wall in the way
    public static final float DOWNHILL_CLIMB_HEIGHT = 0.2f;//how step of an angle you can descend in one movement
    public static final float WALL_CLIMB_BIAS = 0.1f;//your character can go over walls this high, note that if thi

    //character constants, these depend on what beast you pick
    private float characterSpeed = 0.1f;
    private float characterJump = 0.08f;

    /*
    Collision detection:
    Your position is located at the bottom centre.
    Collision bounds is a cylinder of specified radius and height below.
     */
    private float characterRadius = 0.28f;
    private float characterHeight = 1.2f;

    //PHYSICS
    private Vector3f position, positionInterpolate;//position vector(s)
    private Vector2f movementVector;//current movement vector
    private Vector2f intendedMovementVector;//intended movement vector to accelerate towards
    private Vector2f movementDirection;//intended direction to move towards
    private Vector2f wallPushVector = new Vector2f();//the vector that a wall pushes you out with
    private float angleTarget;//intended angle to face towards
    private float angleTargetMidair;//intended angle to face towards
    private float angle;//angle the model is facing TODO possibly animate turning around better
    private float speed;//current intended speed
    private float turnSpeed = TURN_SPEED;//turning speed
    private float yspeed;
    private float floorY;
    //private float wallIntersect = 1;//If not 1, you will collide into a wall this ratio between ticks. Eg: wallIntersect = 0.5 will stop you at a wall halfway through the frame
    private boolean onGround;
    private boolean midairTurn = false;
    private Sound oof;
    private List<SoundSource> sources = new ArrayList<>();

    //collisions
    private CollisionHandler collisionHandler;

    //texture
    private Texture texture;

    //animations
    private Animation animation,animation2;

    /**
     * Create a beast
     */
    public Beast(Game game, AnimatedModel animatedModel, Texture texture){
        super(game, animatedModel, new Matrix4f());
        this.collisionHandler = game.getCollisionHandler();
        this.texture = texture;
        position = new Vector3f(0,0,0);//TODO have some sort of spawn point
        angle = 0;

        positionInterpolate = new Vector3f(0,0,0);
        movementDirection = new Vector2f((float)Math.sin(angle),(float)Math.cos(angle));
        movementVector = new Vector2f(0,0);
        intendedMovementVector = new Vector2f();
        angleTarget = angle;
        angleTargetMidair = angleTarget;
        speed = 0;
        yspeed = 0;
        floorY = collisionHandler.getHeightAtLocation(position);
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

        oof = new Sound(game.getMemoryManager(),"res/sound/oof2.wav");

    }

    /**
     * Set a new direction
     */
    public void setDirection(Vector2f direction) {
        if(!onGround) {
            angleTargetMidair = (float)Math.atan2(direction.x,direction.y);
        }else {
            angleTarget = (float)Math.atan2(direction.x,direction.y);
        }

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
    public boolean jump() {
        //if(turnSpeed==FAST_TURN_SPEED) yspeed += characterJump*speedMultiplier;//SUPER MARIO BACKFLIP
        if(!onGround) {
            //wall jump
            if(wallPushVector.x!=0 || wallPushVector.y!=0) {
                float x = wallPushVector.x;
                float y = wallPushVector.y;
                float len = (float)Math.sqrt(x*x+y*y);
                x/=len;
                y/=len;
                float dot = x*movementVector.x + y*movementVector.y;
                dot*=-2;
                x*=dot;
                y*=dot;
                movementVector.x+=x;
                movementVector.y+=y;

                angleTarget = (float)Math.atan2(movementVector.x,movementVector.y);
                midairTurn = false;
            }else return false;//midair so can't jump
        }
        //jump
        float speedMultiplier = 1;
        speedMultiplier+=movementVector.length()*SPEED_JUMP_MULTIPLIER;
        if(yspeed<0) yspeed=0;
        yspeed += characterJump*speedMultiplier;
        return true;
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
        position.x+=movementVector.x+wallPushVector.x;
        position.z+=movementVector.y+wallPushVector.y;
        position.y+=yspeed;
        boolean inAir = !onGround;
        onGround = position.y<floorY+0.0001f;//0.001f is a small bias to prevent floating point rounding errors
        //land on the ground
        if(onGround) {
            if(inAir) {
                //LAND ON GROUND
                SoundSource source = new SoundSource(oof,false,-yspeed / TERMINAL_VELOCITY);
                source.play();
                sources.add(source);
            }
            position.y = floorY;
            yspeed = 0;
            if(midairTurn) {
                midairTurn = false;
                angleTarget = angleTargetMidair;
            }
        }
        //crash into a wall
        /*if(wallIntersect!=1) {
            movementVector.x = 0;
            movementVector.y = 0;
            SoundSource source = new SoundSource(oof,false);
            source.play();
            sources.add(source);
        }*/

        for(SoundSource source : new ArrayList<>(sources)) {
            if(!source.isPlaying()) {
                source.stop();
                source.cleanUp();
                sources.remove(source);
            }
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
        if(yspeed<-TERMINAL_VELOCITY) {
            yspeed=-TERMINAL_VELOCITY;
        }
        positionInterpolate.x = position.x+movementVector.x;
        positionInterpolate.z = position.z+movementVector.y;
        positionInterpolate.y = position.y+yspeed;

        floorY = collisionHandler.getHeightAtLocation(positionInterpolate);
        //change your yspeed when on the ground to be the same as that of the slope
        if(onGround) {
            float heightBelowFloor = floorY-position.y;
            if(heightBelowFloor > -DOWNHILL_CLIMB_HEIGHT && heightBelowFloor<UPHILL_CLIMB_HEIGHT) {//stick to the ground if the ground infront of you goes downhill
                yspeed = heightBelowFloor;
            }
            else{//this occurs if you fall off the edge of a floor triangle
                onGround = false;
                yspeed-=GRAVITY;
                //System.err.println("Fell off floor? Potential collision problems");
                //floor.printHeightAtLocation(positionInterpolate);
            }
        }
        if(positionInterpolate.y<floorY) positionInterpolate.y = floorY;
        positionInterpolate.y+=WALL_CLIMB_BIAS;//slight bias so you slide over the top of walls but not underneath
        collisionHandler.calculateWallPush(positionInterpolate,characterRadius,wallPushVector/*,1,0*/);//more tests seems to result in you getting pushed through walls sometimes

        //FAST TURN AROUND
        if(getAngleDifference(angle,angleTarget)>FAST_TURN_ANGLE) {
            turnSpeed = FAST_TURN_SPEED;
        }

        float turnAmount = turnSpeed;
        if(midairTurn) turnAmount *=MIDAIR_TURN_MODIFIER;
        float targetAngle = angleTarget;
        if(midairTurn) targetAngle = angleTargetMidair;

        //turn to face direction of movement
        if(getAngleDifference(angle,targetAngle)<turnAmount) {
            angle = targetAngle;
            turnSpeed = TURN_SPEED;
            if(!onGround) midairTurn = true;
        }
        else if(angle>targetAngle) {
            if(angle-targetAngle>Math.PI) angle+=turnAmount;
            else angle-=turnAmount;
        }
        else {
            if(targetAngle-angle>Math.PI) angle-=turnAmount;
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
        float positionInterpolation = frameInterpolation;
        //if(frameInterpolation > wallIntersect) positionInterpolation = wallIntersect;
        //interpolate position
        positionInterpolate.x = position.x+(wallPushVector.x+movementVector.x)*positionInterpolation;
        positionInterpolate.z = position.z+(wallPushVector.y+movementVector.y)*positionInterpolation;
        positionInterpolate.y = position.y+yspeed*frameInterpolation;
        if(positionInterpolate.y<floorY && yspeed<0) {//landing on the ground
            positionInterpolate.y = floorY;
        }

        //interpolate direction
        float angleInterpolate = angle;
        float turnAmount = turnSpeed*frameInterpolation;
        float targetAngle = angleTarget;
        if(midairTurn) targetAngle = angleTargetMidair;
        if(midairTurn) turnAmount *=MIDAIR_TURN_MODIFIER;
        if(getAngleDifference(angle,targetAngle)<turnAmount) angleInterpolate = targetAngle;
        else if(angle>targetAngle) {
            if(angle-targetAngle>Math.PI) angleInterpolate+=turnAmount;
            else angleInterpolate-=turnAmount;
        }
        else {
            if(targetAngle-angle>Math.PI) angleInterpolate-=turnAmount;
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
    public CollisionHandler getCollisionHandler() {
        return collisionHandler;
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
