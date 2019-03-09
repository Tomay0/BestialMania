package com.bestialMania.object.beast;

import com.bestialMania.Settings;
import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.animation.Animation;
import com.bestialMania.animation.AnimationListener;
import com.bestialMania.collision.CollisionHandler;
import com.bestialMania.object.AnimatedObject;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.texture.Texture;
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

    private static final float RUN_MODIFIER = 1.5f;//running speed modifier
    private static final float CROUCH_MODIFIER = 0.5f;//crouch speed modifier

    private static final float SLIDE_SPEED_MODIFIER = 1.5f;//slide speed modifier - in addition to the run modifier

    private static final float LONG_JUMP_HEIGHT_MODIFIER = 0.5f;//note that while long jumping, gravity is lower so you still go far
    private static final float LONG_JUMP_GRAVITY_MODIFIER = 0.4f;//lower gravity while long jumping
    private static final float LONG_JUMP_ACCELERATION_MODIFIER = 0.1f;//in addition to the midair acceleration modifier, affects dive acceleration as well

    private static final float DIVE_SPEED = 0.04f;//your y speed will be set to this when you dive, it will make you move with no yspeed for a certain amount of time until the yspeed gets below 0 again
    private static final float DIVE_MAX_SPEED_MODIFIER = 1.55f;//in addition to the run modifier, is slightly more than the slide speed modifier
    private static final float DIVE_SPEED_MODIFIER = 0.9f;//percentage of your velocity that gets added to your current velocity when diving. Less than max dive speed - 1
    private static final float DIVE_MIN_HEIGHT = 0.2f;//you need to be at least this high above the ground to dive

    private static final float TERMINAL_VELOCITY = 0.5f;//fastest speed you can fall

    public static final float UPHILL_CLIMB_HEIGHT = 0.7f;//how step of an angle you can climb in one movement. Make this larger than terminal velocity to avoid falling through the floor. Note that you will not climb this height if there is a wall in the way
    public static final float DOWNHILL_CLIMB_HEIGHT = 0.3f;//how step of an angle you can descend in one movement
    public static final float WALL_CLIMB_BIAS = 0.1f;//your character can go over walls this high

    private static final float LEDGE_GRAB_CLIMB_SPEED = 0.05f;//how fast you climb up onto a ledge

    private static final float LANDING_ANIMATION_HEIGHT = 0.7f;//you begin to do a landing animation at this height above ground

    //character constants, these depend on what beast you pick
    private float characterSpeed = 0.075f;
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
    private float angleTarget;//intended angle to face towards
    private float angleTarget2;//intended angle to face towards
    private float angle;//angle the model is facing TODO possibly animate turning around better
    private float speed;//current intended speed
    private float turnSpeed = TURN_SPEED;//turning speed
    private float yspeed;
    private float floorY;
    private float ceilY;
    private float grabY;
    private boolean running = false;
    private boolean crouching = false;
    private boolean diving = false;
    private boolean longJump = false;
    private int ledgeGrabFrames = 0;
    private int slidingFrames = 0;
    private boolean onGround;
    private boolean midairTurn = false;
    private Sound oof,jump,slide,dive;
    private SoundSource walkingSound;
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
        grabY = CollisionHandler.MIN_Y;
        onGround = position.y<=floorY;
        if(onGround) position.y=floorY;

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

        //load some sounds
        oof = new Sound(game.getMemoryManager(),"res/sound/oof2.wav");
        jump = new Sound(game.getMemoryManager(), "res/sound/jump.wav");
        slide = new Sound(game.getMemoryManager(), "res/sound/slide.wav");
        dive = new Sound(game.getMemoryManager(), "res/sound/dive.wav");
        Sound walk = new Sound(game.getMemoryManager(), "res/sound/walk.wav");
        walkingSound = new SoundSource(walk,true);

    }

    /**
     * Set a new direction for you to turn towards
     */
    public void setDirection(Vector2f direction) {
        //sliding or being in midair will make you spin towards its direction quickly but then slowly turn after reaching that direction
        if(!onGround || slidingFrames>0 || ledgeGrabFrames>0) {
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
     * Change the speed of your character.
     * The "speed" refers to a value between 0 and 1 referring to how far the analog sticks are pressed (always 1 for keyboard)
     * Running is just if you have the run button held. If automatic running is enabled you may maintain running speed.
     */
    public void setSpeed(float speed,boolean running) {
        //multiply by your character's speed
        this.speed = speed*characterSpeed;

        //work out if the character is running based if automatic running is turning on
        if(running) this.running = true;
        else if(!Settings.AUTOMATIC_RUNNING) this.running = false;
        if(speed==0) this.running = false;

        //slow your speed if you are crouching (doesn't include sliding or diving or climbing a ledge)
        if(this.crouching && slidingFrames==0 && onGround && ledgeGrabFrames==0) {
            this.speed*=CROUCH_MODIFIER;
            this.running = false;
        }
        //if not crouching, increase your speed if you are running
        else if(this.running) this.speed*=RUN_MODIFIER;

        //walking/idle animations. Must be on ground for this to occur, however this does not occur while sliding or crouching.
        if(onGround && slidingFrames<=0 && ledgeGrabFrames==0) {
            if(crouching) {
                playTransitionAnimation(5,0.1f,null);

            }else {
                if(speed>0) playTransitionAnimation(2,0.3f,"walk");
                else playTransitionAnimation(1,0.3f,null);

            }
        }
    }

    /**
     * Method occurs whenever you hold the jump button
     * returns true if you have jumped on that specific frame
     */
    public boolean jump() {
        float speed = movementVector.length();
        if(!onGround) {
            if(diving) return false;//don't wall jump while diving
            //WALL JUMP
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
                turnSpeed = FAST_TURN_SPEED;
            }else return false;//midair so can't jump

        }
        //stop climbing the ledge if you jump while climbing
        else if(ledgeGrabFrames>0) {
            ledgeGrabFrames = 0;
            yspeed = 0;
            //jump forward if 3/4 of your body is over the ledge
            if(position.y+characterHeight/4.0f>grabY) {
                movementVector.x = movementDirection.x*characterSpeed;
                movementVector.y = movementDirection.y*characterSpeed;

                angleTarget = (float)Math.atan2(movementVector.x,movementVector.y);
            }
            //otherwise, wall jump away
            else {
                movementVector.x = -intendedMovementVector.x*characterSpeed/LEDGE_GRAB_CLIMB_SPEED;
                movementVector.y = -intendedMovementVector.y*characterSpeed/LEDGE_GRAB_CLIMB_SPEED;
                angleTarget = (float)Math.atan2(movementVector.x,movementVector.y);
            }
        }

        //jump
        float speedMultiplier = 1;
        speedMultiplier+=speed*speed*SPEED_JUMP_MULTIPLIER;

        //LONG JUMP - you keep the speed you have from the slide - you will longjump if you jump during the slide cooldown
        if(slidingFrames!=0) {
            speedMultiplier*=LONG_JUMP_HEIGHT_MODIFIER;
            slidingFrames = 0;//stop sliding
            longJump = true;
        }
        else if(crouching && slidingFrames==0) speedMultiplier*=HIGH_JUMP_MODIFIER;//HIGH JUMP
        if(yspeed<0) yspeed=0;
        yspeed += characterJump*speedMultiplier;
        onGround = false;
        playSound(jump);

        return true;
    }


    /**
     * Start/Stop crouching
     */
    public void crouch(boolean crouch) {
        //initialize either a slide or a dive (can't do this while grabbing on a ledge)
        if(!crouching && crouch && ledgeGrabFrames==0) {
            if(speed>0) {
                if(onGround) {
                    //SLIDE
                    if(slidingFrames<0) return;//if you press the crouch button while in the cooldown stage of the slide, the next frame that is not a cooldown and the crouch button is still pressed will initialize another slide
                    float speed = movementVector.length();
                    //slide only if you're going fast enough or you quickly change direction
                    if((slidingFrames==0 && (speed>characterSpeed*0.9f || turnSpeed==FAST_TURN_SPEED)) || diving) {
                        slidingFrames = 15;//slide lasts for 15 frames, animation lasts an extra
                        diving = false;

                        //slide speed
                        movementVector.x = movementDirection.x*characterSpeed*RUN_MODIFIER*SLIDE_SPEED_MODIFIER;
                        movementVector.y = movementDirection.y*characterSpeed*RUN_MODIFIER*SLIDE_SPEED_MODIFIER;
                        turnSpeed = FAST_TURN_SPEED;

                        //begin sliding animation
                        animationID = 6;

                        Animation slidingAnimation = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
                        slidingAnimation.addKeyFrame(0.07f,animatedModel.getPose(6));
                        slidingAnimation.addKeyFrame(0.25f,animatedModel.getPose(6));
                        playAnimation(slidingAnimation);
                        playSound(slide);
                    }

                }
                else {
                    //DIVE
                    if(!diving && slidingFrames<=0 && position.y-floorY>DIVE_MIN_HEIGHT) {
                        diving = true;
                        slidingFrames = 15;

                        //speed up in the direction you are facing, at the minimum you will speed up to your characters normal speed
                        float diveSpeed = characterSpeed*RUN_MODIFIER*DIVE_MAX_SPEED_MODIFIER;
                        float dx = movementDirection.x*diveSpeed - movementVector.x;
                        float dy = movementDirection.y*diveSpeed - movementVector.y;
                        float speedChange = (float)Math.sqrt(dx*dx+dy*dy);
                        diveSpeed-=speedChange;
                        diveSpeed+=DIVE_SPEED_MODIFIER*characterSpeed*RUN_MODIFIER;//your dive accelerates you from full running speed to the full diving speed
                        if(diveSpeed<characterSpeed*RUN_MODIFIER*(1+DIVE_SPEED_MODIFIER)) diveSpeed = characterSpeed*RUN_MODIFIER*(1+DIVE_SPEED_MODIFIER);


                        movementVector.x = movementDirection.x*diveSpeed;
                        movementVector.y = movementDirection.y*diveSpeed;
                        turnSpeed = FAST_TURN_SPEED;
                        angleTarget = angleTarget2;
                        midairTurn = false;
                        longJump = false;

                        yspeed=DIVE_SPEED;

                        //begin diving animation
                        playTransitionAnimation(7,0.1f,null);

                        playSound(dive);

                    }else return;
                }
            }else if(!onGround && diving) return;
        }
        //stop grabbing onto a ledge if you press crouch
        else if(ledgeGrabFrames>0 && crouch) ledgeGrabFrames = 0;

        this.crouching = crouch;
    }

    /**
     * Update method 1 - this update method is used by all beast objects and simply updates their position each frame based on the previously determined vectors
     * This method also determines if you are on the ground for the use in physics, updates the animation and deletes sound memory
     */
    @Override
    public void update() {
        //UPDATE YOUR POSITION - DO THIS FIRST
        position.x+=movementVector.x+wallPushVector.x;
        position.z+=movementVector.y+wallPushVector.y;
        if(yspeed<=0 || !diving) position.y+=yspeed;//while diving your yspeed will remain at 0 for a certain amount of time
        //respawn if you fall off the map
        if(position.y <-100) {
            position.x = 0;
            position.y = 0;
            position.z = 0;
        }
        if(ledgeGrabFrames==0) {
            //landing on ground
            boolean inAir = !onGround;
            onGround = position.y<floorY+0.0001f;//0.001f is a small bias to prevent floating point rounding errors
            if(onGround) {
                //previously in air, so play landing on ground sound effect
                if(inAir) {
                    playSound(oof);
                    if(midairTurn) {
                        midairTurn = false;
                        angleTarget = angle;
                    }
                }
                //stick to the floor
                position.y = floorY;
                yspeed = 0;
                //landing on the ground from a longjump/dive
                if(diving || longJump) {
                    diving = false;
                    longJump = false;
                    slidingFrames = 0;
                    float currentSpeed = movementVector.length();
                    if (currentSpeed > speed && speed>0) {
                        movementVector.mul(speed/currentSpeed);
                    }
                }
            }
            //hitting the ceiling
            if(position.y+characterHeight>ceilY-0.0001f) {
                position.y = ceilY-characterHeight;
                if(yspeed>0) yspeed = 0;
            }
        }
        else if(position.y>grabY) position.y = grabY;


        //clear memory for sounds that are not playing
        for(SoundSource source : new ArrayList<>(sources)) {
            if(!source.isPlaying()) {
                source.stop();
                source.cleanUp();
                sources.remove(source);
            }
        }

        //ANIMATION
        updateAnimation();
    }

    /**
     * Update which is only called by the player object after all
     */
    public void updatePhysics() {
        //grabbing and climbing onto a ledge - skips all code after this
        if (ledgeGrabFrames > 0) {
            if (position.y < grabY) {
                movementVector.x = 0;
                movementVector.y = 0;
                wallPushVector.x = 0;
                wallPushVector.y = 0;
                yspeed = LEDGE_GRAB_CLIMB_SPEED;
            } else {
                movementVector.x = intendedMovementVector.x;
                movementVector.y = intendedMovementVector.y;
                ledgeGrabFrames++;
                if (ledgeGrabFrames > characterRadius / LEDGE_GRAB_CLIMB_SPEED * 2) {
                    //finish climbing the ledge
                    ledgeGrabFrames = 0;
                }
            }
        }

        if (ledgeGrabFrames == 0) {

            /*

            ACCELERATION - LATERAL

             */

            //SLIDING MOVEMENT - your speed is locked to one direction and there is no friction
            if (slidingFrames > 0) {
                slidingFrames--;
                //do a 10 frame cooldown after the slide has completed
                if (slidingFrames == 0 && !diving) {
                    slidingFrames = -10;
                }
            }
            //REGULAR MOVEMENT
            else {
                //countdown the slide cooldown
                if (slidingFrames < 0) slidingFrames++;

                //get your intended movement vector
                intendedMovementVector.x = movementDirection.x * speed;
                intendedMovementVector.y = movementDirection.y * speed;

                //work out how fast you will accelerate towards the intended movement. Being in midair will slow your acceleration/deceleration
                float accel = ACCELERATION;
                if (!onGround) {
                    accel *= MIDAIR_ACCELERATION_MODIFIER;
                    //diving or long jumping will slow your acceleration/deceleration even more
                    if (longJump || diving) accel *= LONG_JUMP_ACCELERATION_MODIFIER;
                }

                //change your xspeed
                if (movementVector.x < intendedMovementVector.x) {
                    movementVector.x += accel;
                    if (movementVector.x > intendedMovementVector.x) movementVector.x = intendedMovementVector.x;
                } else if (movementVector.x > intendedMovementVector.x) {
                    movementVector.x -= accel;
                    if (movementVector.x < intendedMovementVector.x) movementVector.x = intendedMovementVector.x;
                }

                //change your yspeed
                if (movementVector.y < intendedMovementVector.y) {
                    movementVector.y += accel;
                    if (movementVector.y > intendedMovementVector.y) movementVector.y = intendedMovementVector.y;
                } else if (movementVector.y > intendedMovementVector.y) {
                    movementVector.y -= accel;
                    if (movementVector.y < intendedMovementVector.y) movementVector.y = intendedMovementVector.y;
                }
            }
            //your new movementSpeed
            float movementSpeed = movementVector.length();
                /*

                ACCELERATION - VERTICAL

                 */

            //GRAVITY - only occurs if you're above ground. Long jumping will
            if (!onGround) {
                float gravity = GRAVITY;
                if (longJump) gravity *= LONG_JUMP_GRAVITY_MODIFIER;
                yspeed -= gravity;
                //stop the walking sound effect while midair
                walkingSound.pause();
                //animation for jumping/moving in midair. Does not occur when sliding, diving.
                //This animation swaps to a landing animation when you get close to the ground, so it doesn't occur there either.
                if (slidingFrames <= 0 && !diving && (yspeed >= 0 || positionInterpolate.y - floorY >= LANDING_ANIMATION_HEIGHT)) {
                    playTransitionAnimation(4, 0.15f, null);
                }
            }
            //When you're on the ground - play the walking sound effect while moving
            else {
                if (movementSpeed > 0) {
                    if (!walkingSound.isPlaying()) {
                        walkingSound.play();
                    }
                } else {
                    walkingSound.pause();
                }
            }

            //Lock your y speed below terminal velocity
            if (yspeed < -TERMINAL_VELOCITY) {
                yspeed = -TERMINAL_VELOCITY;
            }

                /*

                COLLISIONS

                 */

            //FLOOR COLLISIONS
            positionInterpolate.x = position.x + movementVector.x;
            positionInterpolate.z = position.z + movementVector.y;

            //remember your y doesn't increase for a little bit while diving
            positionInterpolate.y = position.y;
            if (yspeed <= 0 || !diving) positionInterpolate.y += yspeed;

            //Where the floor will be at the next frame given your calculated movement vector
            floorY = collisionHandler.getFloorHeightAtLocation(positionInterpolate);

            //If you're on the ground, your y will lock to the floor's location
            if (onGround) {
                float heightBelowFloor = floorY - position.y;
                //stick to the floor if the distance to the floor is close enough
                if (heightBelowFloor > -DOWNHILL_CLIMB_HEIGHT && heightBelowFloor < UPHILL_CLIMB_HEIGHT) {
                    yspeed = heightBelowFloor;
                }
                //distance to the ground is too large, you will simply fall.
                else {
                    onGround = false;
                    yspeed -= GRAVITY;
                }
            }

            //CEILING COLLISIONS
            positionInterpolate.y += characterHeight;
            ceilY = collisionHandler.getCeilingHeightAtLocation(positionInterpolate);

            //WALL COLLISION
            positionInterpolate.y = position.y + yspeed;
            if (positionInterpolate.y < floorY) positionInterpolate.y = floorY;
            positionInterpolate.y += WALL_CLIMB_BIAS;//slight bias so you slide over the top of walls but not underneath
            collisionHandler.calculateWallPush(positionInterpolate, characterRadius, wallPushVector/*,1,0*/);//more tests seems to result in you getting pushed through walls sometimes

            //Check if you can grab onto a ledge. Holding the crouch button will cancel it
            if ((wallPushVector.x != 0 || wallPushVector.y != 0) && yspeed <= 0 && !onGround && !crouching) {
                float dx = movementVector.x / movementSpeed;
                float dz = movementVector.y / movementSpeed;
                //check if in front of you there is a floor
                positionInterpolate.x += dx * characterRadius * 2;
                positionInterpolate.z += dz * characterRadius * 2;
                positionInterpolate.y += characterHeight;
                float frontY = collisionHandler.getFloorHeightAtLocation(positionInterpolate);
                float dy = positionInterpolate.y - frontY;
                if (dy > 0 && dy < characterHeight / 2.0f) {
                    grabY = frontY;
                    angleTarget = angleTarget2 = (float) Math.atan2(dx, dz);
                    turnSpeed = FAST_TURN_SPEED;
                    midairTurn = false;
                    longJump = false;
                    diving = false;
                    onGround = true;
                    ledgeGrabFrames = 1;
                    intendedMovementVector.x = dx * LEDGE_GRAB_CLIMB_SPEED;
                    intendedMovementVector.y = dz * LEDGE_GRAB_CLIMB_SPEED;
                }
            }
        }

        //TURNING (don't turn if you are sliding)
        if(turnSpeed==FAST_TURN_SPEED || (slidingFrames<=0 && ledgeGrabFrames==0)) {
            float turnAmount = turnSpeed;
            float targetAngle = angleTarget;

            //turn slowly while in midair
            if (midairTurn) {
                turnAmount *= MIDAIR_TURN_MODIFIER;
                targetAngle = angleTarget2;
            }


            //turn to face direction of movement
            if (getAngleDifference(angle, targetAngle) < turnAmount) {
                angle = targetAngle;
                turnSpeed = TURN_SPEED;
                if (!onGround) midairTurn = true;//if you're in midair and you reach the turning angle you desire, your turning speed will decrease
            } else if (angle > targetAngle) {
                if (angle - targetAngle > Math.PI) angle += turnAmount;
                else angle -= turnAmount;
            } else {
                if (targetAngle - angle > Math.PI) angle -= turnAmount;
                else angle += turnAmount;
            }
            //keep the angle between +-pi
            if (angle > Math.PI) angle -= 2 * Math.PI;
            if (angle < -Math.PI) angle += 2 * Math.PI;
        }
    }

    /**
     * Update matrices using the frame interpolation amount
     *
     * return the interpolated position
     */
    @Override
    public void interpolate(float frameInterpolation) {
        //CALCULATE YOUR POSITION
        positionInterpolate.x = position.x+(wallPushVector.x+movementVector.x)*frameInterpolation;
        positionInterpolate.z = position.z+(wallPushVector.y+movementVector.y)*frameInterpolation;
        positionInterpolate.y = position.y+yspeed*frameInterpolation;

        if(ledgeGrabFrames==0) {
            if(positionInterpolate.y<floorY && yspeed<0) {//landing on the ground
                positionInterpolate.y = floorY;
            }
            if(positionInterpolate.y+characterHeight>ceilY) {//hitting the ceiling
                positionInterpolate.y = ceilY-characterHeight;
            }


            //LANDING ANIMATION - when you get close to the ground.
            // Only occurs if you are:
            // - above the ground, moving downwards, not diving or sliding and you're below a certain distance above the ground
            if(!onGround && yspeed<0 && !diving && slidingFrames<=0 && positionInterpolate.y-floorY<LANDING_ANIMATION_HEIGHT) {
                if(animationID!=3) {
                    animationID = 3;
                    playAnimation(landingAnimation);
                }

                float i = (positionInterpolate.y-floorY)/LANDING_ANIMATION_HEIGHT;
                landingAnimation.setCurrentTime(i);
            }
        }
        else {
            if(positionInterpolate.y>grabY) positionInterpolate.y = grabY;
        }

        //ROTATE THE CHARACTER
        float angleInterpolate = angle;

        if(turnSpeed==FAST_TURN_SPEED || (slidingFrames<=0 && ledgeGrabFrames==0)) {
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
    /**
     * Play a sound
     */
    private void playSound(Sound sound) {
        SoundSource source = new SoundSource(sound,false);
        source.play();
        sources.add(source);
    }

    /**
     * Play a transition animation
     */
    private void playTransitionAnimation(int animationID, float duration, String actionWhenComplete) {
        if(this.animationID==animationID) return;
        this.animationID = animationID;
        Animation animation = createTransitionAnimation(animationID,duration);
        if(actionWhenComplete!=null) animation.setListener(this,actionWhenComplete);
        playAnimation(animation);
    }

    /**
     * Create and play a simple transition animation
     */
    private Animation createTransitionAnimation(int animationID, float duration) {
        Animation animation = new Animation(animatedModel.getArmature(),getCurrentPose(),false);
        animation.addKeyFrame(duration,animatedModel.getPose(animationID));
        return animation;
    }

    /**
     * Swap the current animation with a new one
     */
    private void playAnimation(Animation animation) {
        cancelAnimation(currentAnimation);
        applyAnimation(animation);
        currentAnimation = animation;
    }

    /**
     * When an animation transition completes
     */
    @Override
    public void animationOver(String action) {
        //begin walking animation
        if(action.equals("walk")) {
            walkingAnimation.setCurrentTime(0);
            playAnimation(walkingAnimation);
        }
    }

    /**
     * Add the model to the renderer
     */
    @Override
    public ShaderObject linkToRenderer(Renderer renderer) {
        ShaderObject shaderObject = createShaderObject(renderer);
        linkTransformsToShaderObject(shaderObject);
        shaderObject.addTexture(0,texture);
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"reflectivity",0.2f));
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"shineDamper",5.0f));
        return shaderObject;
    }

    /**
     * Delete all sounds
     */
    public void cleanUp() {
        for(SoundSource source : sources) {
            source.stop();
            source.cleanUp();
        }
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
     * Get position
     */
    public Vector3f getPosition() {return position;}

    public Vector3f getPositionInterpolate() {
        return positionInterpolate;
    }
    public Model getModel() {return animatedModel.getModel();}
    public Matrix4f getMatrix() {
        return modelMatrix;
    }
    public CollisionHandler getCollisionHandler() {
        return collisionHandler;
    }
}
