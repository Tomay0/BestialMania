package com.bestialMania.rendering.model.loader;

import java.util.ArrayList;
import java.util.List;

public class VertexWeightData {
    private List<Float> weights = new ArrayList<>();
    private List<Integer> jointIds = new ArrayList<>();

    public void addWeight(float weight) {
        weights.add(weight);
    }
    public void addJoint(int joint) {
        jointIds.add(joint);
    }

    /**
     * Reduce or increase the number of joints so there are always 3.
     */
    public void limitJointNumber() {
        //increase if necessary
        while(weights.size()<3) {
            weights.add(0.0f);
            jointIds.add(0);
        }
        //decrease if necessary
        if(weights.size()>3) {
            while(jointIds.size()>3) {
                jointIds.remove(jointIds.size()-1);
            }
            float weight0 = weights.get(0);
            float weight1 = weights.get(1);
            float weight2 = weights.get(2);
            float total = weight0+weight1+weight2;
            weight0/=total;
            weight1/=total;
            weight2/=total;
            weights.clear();
            weights.add(weight0);
            weights.add(weight1);
            weights.add(weight2);
        }
    }


    /**
     * Get the joint id by the given index
     */
    public int getJointId(int index) {
        return jointIds.get(index);
    }

    /**
     * Get the joint's weight for the given index
     */
    public float getWeight(int index) {
        return weights.get(index);
    }
}
