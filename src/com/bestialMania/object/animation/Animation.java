package com.bestialMania.object.animation;

import com.bestialMania.Main;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Animation {
    private static final float timeAdvance = 1.0f/ (float)Main.TICKS_PER_SECOND;

    private float currentTime;//current time based on
    private boolean loop;
    private float end;

    private Map<String,JointAnimation> jointAnimations = new HashMap<>();
    private Matrix4f[] localTransforms;

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
        localTransforms = new Matrix4f[armature.size()];
        for(int i = 0;i<armature.size();i++) {
            localTransforms[i] = new Matrix4f();
        }
    }

    /**
     * Add a keyframe to the animation (must be done in order)
     * Only applies to the joints you provide
     */
    public void addKeyFrame(float timestamp, Pose pose, Set<String> joints) {
        for(String jointName : joints) {
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
        if(end==0) return;//don't do anything if there is no animation
        currentTime += timeAdvance;
        //loop
        if(loop) {
            while(currentTime > end) {
                currentTime-=end;
            }
        }
    }

    /**
     * Calculate the matrices and store in the localTransforms array
     * (Called every render())
     */
    public void calculateMatrices(float interpolation) {

        float actualTime = currentTime + timeAdvance*interpolation;
        if(end==0) actualTime = 0;
        else if(loop){
            while(actualTime > end) {
                actualTime-=end;
            }
        }

        for(JointAnimation jointAnimation : jointAnimations.values()) {
            Matrix4f matrix = jointAnimation.calculateMatrix(actualTime);
            matrix.get(localTransforms[jointAnimation.getJoint().getId()]);
        }
    }

    /**
     * Get the local transform for a joint id
     */
    public Matrix4f getMatrix(int jointId) {
        return localTransforms[jointId];
    }

    public void setCurrentTime(float timestamp) {
        this.currentTime = timestamp;
    }
}
