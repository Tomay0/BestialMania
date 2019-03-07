package com.bestialMania.animation;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Pose {
    private int id;

    private Map<String,JointTransform> jointTransforms = new HashMap<>();

    /**
     * A "Pose" is a list of transforms that make up a keyframe or pose in an animation.
     * Each pose is stored in an animated object with a specific id and are loaded when the model is loaded.
     */
    public Pose(int id) {
        this.id = id;
    }

    /**
    Add a transform to the pose
     */
    public void addTransform(Joint joint, Matrix4f matrix) {
        JointTransform transform = new JointTransform(joint,matrix);
        jointTransforms.put(joint.getName(), transform);
    }

    /**
     * Gets the transform for the given joint
     */
    public JointTransform getTransform(String jointName) {
        return jointTransforms.get(jointName);
    }

    public void addTransform(Joint joint, Vector3f position, Vector4f rotation, Matrix4f matrix) {
        JointTransform transform = new JointTransform(joint,position,rotation,matrix);
        jointTransforms.put(joint.getName(),transform);
    }

    public Collection<JointTransform> getJointTransforms() {return jointTransforms.values();}
}
