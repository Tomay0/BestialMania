package com.bestialMania.animation;

public class KeyFrame {
    private float timestamp;
    private JointTransform jointTransform;

    /**
     * A Keyframe is a joint's transform at a particular timestamp
     */
    public KeyFrame(float timestamp, Pose pose, Joint joint) {
        this.timestamp = timestamp;
        this.jointTransform = pose.getTransform(joint.getName());
    }


    public float getTimestamp() {
        return timestamp;
    }
    public JointTransform getJointTransform() {
        return jointTransform;
    }
}
