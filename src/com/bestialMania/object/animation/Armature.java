package com.bestialMania.object.animation;

import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.Map;

public class Armature implements Iterable<Joint>{
    private Joint rootJoint;
    private Map<String, Joint> joints;
    private Joint[] jointArray;
    /**
     * Armature is a list of joints arranged in a hierarchy. This object is useful for storing joints in a variety of collections
     * Array: in order by id
     * HashMap: arranged by joint name
     * Tree/Hierarchy: Start at rootJoint
     */
    public Armature(Map<String, Joint> joints, Joint rootJoint) {
        this.rootJoint = rootJoint;
        this.joints = joints;
        jointArray = new Joint[joints.size()];
        for(Joint j : joints.values()) {
            jointArray[j.getId()] = j;
        }
    }

    /**
     * Get a joint by its name
     */
    public Joint getJoint(String name) {
        if(!joints.containsKey(name)) return null;
        return joints.get(name);

    }

    /**
     * Get a joint by its id
     */
    public Joint getJoint(int id) {
        return jointArray[id];
    }

    /**
     * Calculate the inverse bind transforms of all joints by their local bind transforms
     */
    public void calculateInverseBindTransforms() {
        Matrix4f parent = new Matrix4f();
        parent.identity();
        rootJoint.calculateInverseBindTransform(parent);
    }

    /**
     * Number of joints
     */
    public int size() {
        return jointArray.length;
    }

    /**
     * Get the root joint
     */
    public Joint getRootJoint() {
        return rootJoint;
    }

    /**
     * Iterator all joints in order of id
     */
    @Override
    public Iterator<Joint> iterator() {
        return joints.values().iterator();
    }
}
