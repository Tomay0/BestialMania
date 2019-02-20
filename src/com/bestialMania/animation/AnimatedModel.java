package com.bestialMania.animation;
import com.bestialMania.rendering.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnimatedModel {
    private Model model;
    private Armature armature;
    private List<Pose> poses = new ArrayList<>();//all poses loaded from a file

    /**
     * Object that can be animated
     * Has a model, armature and a list of poses to animate from.
     */
    public AnimatedModel(Model model, Armature armature) {
        this.model = model;
        this.armature = armature;
    }

    public void addPose(Pose pose) {poses.add(pose);}
    public Pose getPose(int id) {return poses.get(id);}
    public List<Pose> getPoses() {return Collections.unmodifiableList(poses);}

    public Armature getArmature() {return armature;}
    public Model getModel() {
        return model;
    }

}
