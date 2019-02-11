package com.bestialMania.rendering.model.armature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Armature {
    private Joint rootJoint;
    private Map<String, Joint> joints = new HashMap<>();
    private Joint[] jointArray;
    /**
     * Create
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
        return joints.get(name);
    }

    /**
     * Get a joint by its id
     */
    public Joint getJoint(int id) {
        return jointArray[id];
    }

    /**
     * TEST:
     * print a list of all the joints
     */
    public void printJoints() {
        printJoints(rootJoint,"");
    }

    private void printJoints(Joint joint, String indent) {
        System.out.println(indent + joint.getName());

        for(Joint child : joint) {
            printJoints(child,indent + " ");
        }
    }
}
