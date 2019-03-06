package com.bestialMania.animation;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Joint implements Iterable<Joint> {
    private String name;
    private int id;
    private List<Joint> children = new ArrayList<>();
    private Matrix4f localBindTransform, inverseBindTransform = new Matrix4f();

    /**
     * A "Joint" controls a particular set of vertices on a model.
     * Joints are arranged in a hierarchy and are affected by each other's transformations
     * Each has a unique name and id. Both can be used for different collection types
     */
    public Joint(String name, int id) {
        this.name = name;
        this.id = id;
    }
    public Joint(String name, int id,Matrix4f inverseBindTransform) {
        this.name = name;
        this.id = id;
        this.inverseBindTransform = inverseBindTransform;
    }


    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }

    public void addChild(Joint joint) {
        children.add(joint);
    }

    @Override
    public Iterator<Joint> iterator() {return children.iterator();}

    /**
     * Set the local bind transform
     * This is the transform required to get from the parent's transform to this joint's transform in the bind position (T-POSE)
     */
    public void setLocalBindTransform(Matrix4f localBindTransform) {
        this.localBindTransform = localBindTransform;
    }

    /**
     * Calculate the inverse bind transform by every local bind transform in the hierarchy
     * The inverse bind transform will transform each joint to the identity matrix
     */
    public void calculateInverseBindTransform(Matrix4f parentTransform) {
        parentTransform.mul(localBindTransform,inverseBindTransform);
        for(Joint child : children) {
            child.calculateInverseBindTransform(inverseBindTransform);
        }
        inverseBindTransform.invert();
    }

    public Matrix4f getInverseBindTransform() {
        return inverseBindTransform;
    }
}
