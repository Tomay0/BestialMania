package com.bestialMania.object.animation;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class JointAnimation {
    private List<KeyFrame> keyframes = new ArrayList<>();
    private Joint joint;//keyframe only affects 1 joint.
    private int currentIndex;
    private Matrix4f interpolatedMatrix;

    /**
     * A series of keyframes for a joint's transform.
     */
    public JointAnimation(Joint joint) {
        this.joint = joint;
        currentIndex = -1;
        interpolatedMatrix = new Matrix4f();
    }

    /**
     * Add a keyframe
     */
    public void addKeyframe(float timestamp, Pose pose) {
        keyframes.add(new KeyFrame(timestamp, pose, joint));
    }

    /**
     * Calculate the interpolated matrix based on the time through the animation
     */
    public Matrix4f calculateMatrix(float currentTime) {
        while(currentIndex < keyframes.size()-1) {
            if(currentTime >= keyframes.get(currentIndex+1).getTimestamp())
                currentIndex++;
            else break;
        }
        while(currentIndex > -1) {
            if(currentTime < keyframes.get(currentIndex).getTimestamp())
                currentIndex--;
            else break;
        }

        //last frame of the animation
        if(currentIndex==keyframes.size()-1) {
            JointTransform jointTransform = keyframes.get(keyframes.size()-1).getJointTransform();
            return jointTransform.getMatrix();
        }
        //first frame of the animation
        else if(currentIndex==-1) {
            JointTransform jointTransform = keyframes.get(0).getJointTransform();
            return jointTransform.getMatrix();
        }
        //interpolated between currentIndex and currentIndex+1
        else{
            KeyFrame k1 = keyframes.get(currentIndex);
            KeyFrame k2 = keyframes.get(currentIndex+1);
            JointTransform j1 = k1.getJointTransform();
            JointTransform j2 = k2.getJointTransform();

            float timespan = k2.getTimestamp()-k1.getTimestamp();
            float progression = currentTime-k1.getTimestamp();
            float interpolation = progression/timespan;

            JointTransform.interpolate(j1,j2,interpolatedMatrix,interpolation);
            return interpolatedMatrix;
        }

    }

    /**
     * Get the joint
     */
    public Joint getJoint() {
        return joint;
    }
}
