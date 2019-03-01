package com.bestialMania.object.beast;

import com.bestialMania.Settings;
import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.animation.Animation;
import com.bestialMania.animation.AnimationListener;
import com.bestialMania.collision.CollisionHandler;
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

public class Beast extends AnimatedObject implements AnimationListener {
    //TEST STUFF
    //private static final float songBPM = 150;//PUMPED UP KICKS = 128. Running in the 90s = 159. Spaceghostpurp = 150 Change to make jimmy sync up to the song you pick
    private int t = 0;

    //constants
    private static final float GRAVITY = 0.01f;//gravity acceleration
    private static final float ACCELERATION = 0.007f;//lateral movement acceleration
    private static final float MIDAIR_ACCELERATION_MODIFIER = 0.2f;//multiply your acceleration by this number when midair
    private static final float MIDAIR_TURN_MODIFIER = 0.5f;//multiply your turning acceleration by this number when midair
    private static final float TURN_SPEED = 0.1f;//speed your model turns at
    private static final float FAST_TURN_SPEED = 0.2f;//speed you turn when "fast turning"
    private static final float FAST_TURN_ANGLE = (float)Math.PI*0.6f;//you must turn at least this angle amount to do a "fast turn"

    private static final float SPEED_JUMP_MULTIPLIER = 2.0f;//increasing this makes running increase your jump height much more.
    private static final float HIGH_JUMP_MODIFIER = 1.5f;//high jump modifier

    private static final float RUN_MODIFIER = 1.25f;//running speed modifier
    private static final float CROUCH_MODIFIER = 0.5f;//crouch speed modifier

    private static final float SLIDE_SPEED_MODIFIER = 2.0f;//slide speed modifier - higher than run modifier

    private static final float LONG_JUMP_HEIGHT_MODIFIER = 0.5f;//note that while long jumping, gravity is lower so you still go far
    private static final float LONG_JUMP_GRAVITY_MODIFIER = 0.5f;//lower gravity while long jumping
    private static final float LONG_JUMP_ACCELERATION_MODIFIER = 0.2f;//in addition to the midair acceleration modifier

    private static final float DIVE_SPEED = 0.04f;//your y speed will be set to this when you dive, it will make you move with no yspeed for a certain amount of time until the yspeed gets below 0 again
    private static final float DIVE_MAX_SPEED_MODIFIER = 2.05f;//slightly higher than slide speed
    private static final float DIVE_SPEED_MODIFIER = 0.9f;//percentage of your velocity that gets added to your current velocity when diving. Less than max dive speed - 1

    private static final float TERMINAL_VELOCITY = 0.5f;//fastest speed you can fall

    public static final float UPHILL_CLIMB_HEIGHT = 0.7f;//how step of an angle you can climb in one movement. Make this larger than terminal velocity to avoid falling through the floor. Note that you will not climb this height if there is a wall in the way
    public static final float DOWNHILL_CLIMB_HEIGHT = 0.3f;//how step of an angle you can descend in one movement
    public static final float WALL_CLIMB_BIAS = 0.1f;//your character can go over walls this high

    //character constants, these depend on what beast you pick
    private float characterSpeed = 0.085f;//lowest is ~0.8. Highest is ~0.09
    private float characterJump = 0.17f;//don't know about these values yet but make slower characters have lower jump

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
    private Vector2f slideVector = new Vector2f();
    private float angleTarget;//intended angle to face towards
    private float angleTarget2;//intended angle to face towards
    private float angle;//angle the model is facing TODO possibly animate turning around better
    private float speed;//current intended speed
    private float turnSpeed = TURN_SPEED;//turning speed
    private float yspeed;
    private float floorY;
    private float ceilY;
    private boolean running = false;
    private boolean crouching = false;
    private boolean diving = false;
    private boolean longJump = false;
    private int slidingFrames = 0;
    private boolean onGround;
    private boolean midairTurn = false;
    private Sound oof;
    private List<SoundSource> sources = new ArrayList<>();

    //collisions
    private CollisionHandler collisionHandler;

    //texture
    private Texture texture;

    //animations
    private int animationID;//ID to distinguish what animations you are currently transitioning into
    /*
    1 = still
    2 = walking
    3 = land
    4 = jump
    5 = crouch
    6 = slide
    7 = dive
     */
    private Animation currentAnimation;
    private Animation walkingAnimation, landingAnimation;

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
        angleTarget2 = angleTarget;
        speed = 0;
        yspeed = 0;
        floorY = collisionHandler.getFloorHeightAtLocation(position);
        ceilY = CollisionHandler.MAX_Y;
        onGround = position.y<=floorY;
        if(onGround) position.y=floorY;

        //TEST ANIMATION (trying to animation 2 parts separately)
        /*Set<String> testAffectedJoints = new HashSet<>(Arrays.asList("Dummy003","Dummy057","Dummy058","Dummy062","Dummy059","Dummy063","Dummy060","Dummy064","Dummy061"));//legs only
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
        applyAnimation(animation2);*/

        /*
        Test animation:
        0 = t pose
        1 = still
        2 = walk1
        3 = walk2
        4 = jump
        5 = crouch
        6 = slide
        7 = dive
         */

        walkingAnimation = new Animation(animatedModel.getArmature(), animatedModel.getPose(2),true);
        walkingAnimation.addKeyFrame(0.3f,animatedModel.getPose(3));
        walkingAnimation.addKeyFrame(0.6f,animatedModel.getPose(2));

        landingAnimation = new Animation(animatedModel.getArmature(),animatedModel.getPose(1),false);
        landingAnimation.addKeyFrame(1,animatedModel.getPose(4));
        landingAnimation.disableTimer();

        //currently in the still pose
        animationID = 1;
        currentAnimation = new Animation(animatedModel.getArmature(),animatedModel.getPose(1),false);
        applyAnimation(currentAnimation);

        oof = new Sound(game.getMemoryManager(),"res/sound/oof2.wav");

    }

    /**
     * Set a new direction
     */
    public void setDirection(Vector2f direction) {
        if(!onGround || slidingFrames>0) {
            angleTarget2 = (float)Math.atan2(direction.x,direction.y);
        }else {
            angleTarget = (float)Math.atan2(direction.x,direction.y);

            if (getAngleDifference(angle, angleTarget) > FAST_TURN_ANGLE) {
                turnSpeed = FAST_TURN_SPEED;
            }
        }

        this.movementDirection = direction;
    }

    /**
     * Set a new speed
     */
    public void setSpeed(float speed,boolean running) {
        this.speed = speed*characterSpeed;

        //work out if the character is running based if automatic running is turning on
        if(running) this.running = true;
        else if(!Settings.AUTOMATIC_RUNNING) this.running = false;
        if(speed==0) this.running = false;

        if(this.crouching) {
            this.speed*=CROUCH_MODIFIER;
            this.running = false;
        }
        else if(this.running) this.speed*=RUN_MODIFIER;

        //animation
        if(onGround && !crouching && slidingFrames<=0) {
            if(speed>0) {
                //begin walking animation
                if(animationID!=2) {
                    animationID = 2;
                    Animation beginWalk = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
                    beginWalk.setListener(this,"walk");
                    beginWalk.addKeyFrame(0.3f,animatedModel.getPose(2));
                    applyAnimation(beginWalk);
                    cancelAnimation(currentAnimation);
                    currentAnimation = beginWalk;
                }
            }
            else {
                //stop walking animation
                if(animationID!=1) {
                    animationID = 1;
                    Animation stopWalk = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
                    stopWalk.addKeyFrame(0.3f,animatedModel.getPose(1));
                    applyAnimation(stopWalk);
                    cancelAnimation(currentAnimation);
                    currentAnimation = stopWalk;
                }
            }
        }
    }

    /**
     * Jump.
     * Returns true if a jump took place
     */
    public boolean jump() {
        float speed = movementVector.length();
        if(!onGround) {
            if(diving) return false;
            //wall jump
            if(wallPushVector.x!=0 || wallPushVector.y!=0) {
                float x = wallPushVector.x;
                float y = wallPushVector.y;
                float len = (float)Math.sqrt(x*x+y*y);
                x/=len;
                y/=len;
                float dot = x*movementVector.x + y*movementVector.y;
                if(Math.abs(dot/speed)<0.3) return false;//don't wall jump if you are really close to being parallel to the wall
                dot*=-2;
                x*=dot;
                y*=dot;
                movementVector.x+=x;
                movementVector.y+=y;

                angleTarget = (float)Math.atan2(movementVector.x,movementVector.y);
                midairTurn = false;
                longJump = false;
            }else return false;//midair so can't jump
        }
        //jump
        float speedMultiplier = 1;
        speedMultiplier+=speed*speed*SPEED_JUMP_MULTIPLIER;
        if(slidingFrames>0) {
            //LONG JUMP
            speedMultiplier*=LONG_JUMP_HEIGHT_MODIFIER;
            slidingFrames = 0;//stop sliding
            longJump = true;
        }
        else if(crouching) speedMultiplier*=HIGH_JUMP_MODIFIER;//HIGH JUMP
        if(yspeed<0) yspeed=0;
        yspeed += characterJump*speedMultiplier;
        return true;
    }

    /**
     * Start/Stop crouching
     */
    public void crouch(boolean crouch) {
        //initialize the crouch
        if(!crouching && crouch) {
            if(speed>0) {
                if(onGround) {
                    if(slidingFrames<0) return;
                    float speed = movementVector.length();
                    //slide only if you're going fast enough or you quickly change direction
                    if((slidingFrames==0 && (speed>characterSpeed*0.9f || turnSpeed==FAST_TURN_SPEED)) || diving) {
                        slidingFrames = 15;//slide lasts for 15 frames, animation lasts an extra
                        diving = false;

                        //slide speed
                        slideVector.x = movementDirection.x*characterSpeed*SLIDE_SPEED_MODIFIER;
                        slideVector.y = movementDirection.y*characterSpeed*SLIDE_SPEED_MODIFIER;
                        turnSpeed = FAST_TURN_SPEED;

                        //begin sliding animation
                        animationID = 6;

                        Animation slidingAnimation = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
                        slidingAnimation.addKeyFrame(0.07f,animatedModel.getPose(6));
                        slidingAnimation.addKeyFrame(0.25f,animatedModel.getPose(6));
                        cancelAnimation(currentAnimation);
                        applyAnimation(slidingAnimation);
                        currentAnimation = slidingAnimation;
                    }

                }
                else {
                    if(!diving && slidingFrames<=0) {
                        diving = true;
                        slidingFrames = 15;

                        //speed up in the direction you are facing, at the minimum you will speed up to your characters normal speed
                        float diveSpeed = characterSpeed*DIVE_MAX_SPEED_MODIFIER;
                        float dx = movementDirection.x*diveSpeed - movementVector.x;
                        float dy = movementDirection.y*diveSpeed - movementVector.y;
                        float speedChange = (float)Math.sqrt(dx*dx+dy*dy);
                        diveSpeed-=speedChange;
                        diveSpeed+=DIVE_SPEED_MODIFIER*characterSpeed;//your dive accelerates you from full walking speed to the full diving speed
                        if(diveSpeed<characterSpeed*(1+DIVE_SPEED_MODIFIER)) diveSpeed = characterSpeed*(1+DIVE_SPEED_MODIFIER);


                        slideVector.x = movementDirection.x*diveSpeed;
                        slideVector.y = movementDirection.y*diveSpeed;
                        turnSpeed = FAST_TURN_SPEED;
                        angleTarget = angleTarget2;
                        midairTurn = false;
                        longJump = false;

                        yspeed=DIVE_SPEED;

                        //begin diving animation
                        animationID = 7;

                        Animation slidingAnimation = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
                        slidingAnimation.addKeyFrame(0.1f,animatedModel.getPose(7));
                        cancelAnimation(currentAnimation);
                        applyAnimation(slidingAnimation);
                        currentAnimation = slidingAnimation;

                    }else return;
                }
            }else if(!onGround) return;
        }
        this.crouching = crouch;
        //crouching on ground animation
        if(this.crouching && onGround && animationID!=5 && slidingFrames<=0) {
            animationID = 5;
            Animation crouchAnimation = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
            crouchAnimation.addKeyFrame(0.1f,animatedModel.getPose(5));
            cancelAnimation(currentAnimation);
            applyAnimation(crouchAnimation);
            currentAnimation = crouchAnimation;
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
        //UPDATE YOUR POSITION - DO THIS FIRST
        position.x+=movementVector.x+wallPushVector.x;
        position.z+=movementVector.y+wallPushVector.y;
        if(yspeed<=0 || !diving) position.y+=yspeed;


        //landing on ground
        boolean inAir = !onGround;
        onGround = position.y<floorY+0.0001f;//0.001f is a small bias to prevent floating point rounding errors
        if(onGround) {
            //previously in air, so play landing on ground sound effect
            if(inAir) {
                SoundSource source = new SoundSource(oof,false,-yspeed / TERMINAL_VELOCITY);
                source.play();
                sources.add(source);
                if(midairTurn) {
                    midairTurn = false;
                    angleTarget = angle;
                }
            }
            //stick to the floor
            position.y = floorY;
            yspeed = 0;
            longJump = false;
        }
        else {
            //jump animation (more like fall animation LMAO)
            if(animationID!=4 && slidingFrames<=0 && !diving && (yspeed>=0 || positionInterpolate.y-floorY>=DOWNHILL_CLIMB_HEIGHT)) {
                animationID = 4;
                Animation jumpAnimation = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
                jumpAnimation.addKeyFrame(0.15f,animatedModel.getPose(4));
                applyAnimation(jumpAnimation);
                cancelAnimation(currentAnimation);
                currentAnimation = jumpAnimation;
            }
        }

        //hitting the ceiling
        if(position.y+characterHeight>ceilY-0.0001f) {
            position.y = ceilY-characterHeight;
            if(yspeed>0) yspeed = 0;
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

        //SLIDING
        if(slidingFrames>0) {
            slidingFrames--;
            movementVector.x = slideVector.x;
            movementVector.y = slideVector.y;
            if(slidingFrames==0 && !diving) {
                slidingFrames = -10;//slight cooldown between slides
            }
        }
        //REGULAR MOVEMENT
        else {
            if(slidingFrames<0) slidingFrames++;
            intendedMovementVector.x = movementDirection.x*speed;
            intendedMovementVector.y = movementDirection.y*speed;

            float accel = ACCELERATION;
            if(!onGround) accel*=MIDAIR_ACCELERATION_MODIFIER;
            if(longJump) accel*=LONG_JUMP_ACCELERATION_MODIFIER;
            if(onGround && diving) {
                //landing from a dive will lower your speed
                diving = false;
                slidingFrames = 0;
                float currentSpeed = movementVector.length();
                if(currentSpeed>speed) accel = currentSpeed-speed;
            }

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
        }

        //GRAVITY
        if(!onGround) {
            float gravity = GRAVITY;
            if(longJump) gravity*=LONG_JUMP_GRAVITY_MODIFIER;
            yspeed-=gravity;
        }
        if(yspeed<-TERMINAL_VELOCITY) {
            yspeed=-TERMINAL_VELOCITY;
        }

        //FLOOR COLLISIONS
        positionInterpolate.x = position.x+movementVector.x;
        positionInterpolate.z = position.z+movementVector.y;
        if(yspeed<=0 || !diving) positionInterpolate.y = position.y+yspeed;
        floorY = collisionHandler.getFloorHeightAtLocation(positionInterpolate);
        //stay on top of a slope
        if(onGround) {
            float heightBelowFloor = floorY-position.y;
            if(heightBelowFloor > -DOWNHILL_CLIMB_HEIGHT && heightBelowFloor<UPHILL_CLIMB_HEIGHT) {//stick to the ground if the ground infront of you goes downhill
                yspeed = heightBelowFloor;
            }
            else{//this occurs if you fall off the edge of a floor triangle
                onGround = false;
                yspeed-=GRAVITY;
            }
        }

        //CEILING COLLISIONS
        positionInterpolate.y += characterHeight;
        ceilY = collisionHandler.getCeilingHeightAtLocation(positionInterpolate);

        //WALL COLLISION
        positionInterpolate.y = position.y+yspeed;
        if(positionInterpolate.y<floorY) positionInterpolate.y = floorY;
        positionInterpolate.y+=WALL_CLIMB_BIAS;//slight bias so you slide over the top of walls but not underneath
        collisionHandler.calculateWallPush(positionInterpolate,characterRadius,wallPushVector/*,1,0*/);//more tests seems to result in you getting pushed through walls sometimes

        //TURNING (don't turn while sliding)
        if(turnSpeed==FAST_TURN_SPEED || slidingFrames<=0 || !diving) {
            float turnAmount = turnSpeed;
            float targetAngle = angleTarget;
            if (midairTurn) {
                turnAmount *= MIDAIR_TURN_MODIFIER;
                targetAngle = angleTarget2;
            }


            //turn to face direction of movement
            if (getAngleDifference(angle, targetAngle) < turnAmount) {
                angle = targetAngle;
                turnSpeed = TURN_SPEED;
                if (!onGround) midairTurn = true;
            } else if (angle > targetAngle) {
                if (angle - targetAngle > Math.PI) angle += turnAmount;
                else angle -= turnAmount;
            } else {
                if (targetAngle - angle > Math.PI) angle -= turnAmount;
                else angle += turnAmount;
            }

            if (angle > Math.PI) angle -= 2 * Math.PI;
            if (angle < -Math.PI) angle += 2 * Math.PI;
        }

        //ANIMATION
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
        if(positionInterpolate.y+characterHeight>ceilY) {//hitting the ceiling
            positionInterpolate.y = ceilY-characterHeight;
        }
        //landing animation when you get close to the ground
        if(!onGround && yspeed<0 && !diving && slidingFrames<=0 && positionInterpolate.y-floorY<DOWNHILL_CLIMB_HEIGHT) {
            if(animationID!=3) {
                animationID = 3;
                cancelAnimation(currentAnimation);
                applyAnimation(landingAnimation);
                currentAnimation = landingAnimation;

            }

            float i = (positionInterpolate.y-floorY)/DOWNHILL_CLIMB_HEIGHT;
            landingAnimation.setCurrentTime(i);
        }

        //interpolate facing direction
        float angleInterpolate = angle;
        if(turnSpeed==FAST_TURN_SPEED || slidingFrames<=0 || !diving) {
            float turnAmount = turnSpeed*frameInterpolation;
            float targetAngle = angleTarget;
            if(midairTurn) targetAngle = angleTarget2;
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

    /**
     * When an animation transition completes
     */
    @Override
    public void animationOver(String action) {
        //begin walking animation
        if(action.equals("walk")) {
            walkingAnimation.setCurrentTime(0);
            cancelAnimation(currentAnimation);
            applyAnimation(walkingAnimation);
            currentAnimation = walkingAnimation;
        }
    }
}
