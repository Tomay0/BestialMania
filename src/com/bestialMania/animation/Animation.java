package com.bestialMania.animation;

import com.bestialMania.Main;
import com.bestialMania.object.beast.Beast;
import org.joml.Matrix4f;

import java.util.*;

public class Animation {
    private static final float timeAdvance = 1.0f/ (float)Main.TICKS_PER_SECOND;

    private float currentTime;//current time based on
    private boolean loop;
    private float end;

    private Map<String,JointAnimation> jointAnimations = new HashMap<>();

    private AnimationListener listener = null;
    private String listenerAction = null;
    private boolean useTimer = true;//if the timer is automatic
    private boolean finished = false;//only for non-looping animations

    /**
     * Create an animation.
     * All joints are assigned to a default pose, more keyframes get added later
     */
    public Animation(Armature armature, Pose defaultPose, boolean loop) {
        currentTime = 0;
        end = 0;
        this.loop = loop;
        for(Joint joint : armature) {
            JointAnimation jointAnimation = new JointAnimation(joint);
            jointAnimation.addKeyframe(0,defaultPose);
            jointAnimations.put(joint.getName(),jointAnimation);
        }
    }

    /**
     * Create an animation.
     * All applied joints are assigned a default pose, more keyframes get added later
     */
    public Animation(Armature armature, Pose defaultPose, Set<String> appliedJoints, boolean loop) {
        currentTime = 0;
        end = 0;
        this.loop = loop;
        for(String jointName : appliedJoints) {
            Joint joint = armature.getJoint(jointName);
            JointAnimation jointAnimation = new JointAnimation(joint);
            jointAnimation.addKeyframe(0,defaultPose);
            jointAnimations.put(joint.getName(),jointAnimation);
        }
    }

    /**
     * Add a keyframe to the animation (must be done in order)
     * Only applies to the joints you provide
     */
    public void addKeyFrame(float timestamp, Pose pose, Set<String> appliedJoints) {
        for(String jointName : appliedJoints) {
            JointAnimation jointAnimation = jointAnimations.get(jointName);
            jointAnimation.addKeyframe(timestamp,pose);
        }
        end = timestamp;
    }

    /**
     * Add a keyframe to the animation (must be done in order)
     * Applies to all joints
     */
    public void addKeyFrame(float timestamp, Pose pose) {
        for(JointAnimation jointAnimation : jointAnimations.values()) {
            jointAnimation.addKeyframe(timestamp,pose);
        }
        end = timestamp;
    }

    /**
     * Update the animation timer
     * (Called every update())
     */
    public void update() {
        if(!useTimer) return;
        if(end==0) return;//don't do anything if there is no animation
        currentTime += timeAdvance;
        //loop
        if(loop) {
            while(currentTime > end) {
                currentTime-=end;
            }
        }
        else if(!finished){
            if(currentTime+timeAdvance>end) {
                finished = true;
                if(listener!=null) {
                    listener.animationOver(listenerAction);
                }
            }
        }
    }

    /**
     * Calculate the matrices and store in the localTransforms array
     * (Called every render())
     */
    public void calculateMatrices(float interpolation) {
        float actualTime = currentTime;
        if(end==0) actualTime = 0;
        else if(useTimer) {
            actualTime = currentTime + timeAdvance*interpolation;
            if(loop){
                while(actualTime > end) {
                    actualTime-=end;
                }
            }
        }


        for(JointAnimation jointAnimation : jointAnimations.values()) {
            jointAnimation.calculateMatrix(actualTime);
        }
    }

    /**
     * Get the local transform for a joint id
     */
    public Matrix4f getLocalTransform(String jointName) {
        return jointAnimations.get(jointName).getMatrix();
    }

    public void setCurrentTime(float timestamp) {
        this.currentTime = timestamp;
    }

    /**
     * Disable automatic timer
     */
    public void disableTimer() {this.useTimer = false;}

    /**
     * Set a listener that will call an event if the animation finishes
     */
    public void setListener(AnimationListener listener, String action) {
        this.listener = listener;
        this.listenerAction = action;
    }

    public Collection<String> getAffectedJoints() {
        return Collections.unmodifiableCollection(jointAnimations.keySet());
    }
}
