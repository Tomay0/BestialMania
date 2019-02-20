package com.bestialMania.object;

import com.bestialMania.animation.*;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;

import java.util.*;

public abstract class AnimatedObject extends Object3D{
    protected AnimatedModel animatedModel;
    private Map<String,Animation> animationJointMap = new HashMap<>();
    private Set<Animation> currentAnimations = new HashSet<>();

    //pose to use if no animation is applied to specific joints
    private Pose defaultPose;
    //joint transforms to be used in the shader
    private Matrix4f[] jointTransforms;

    //parent transforms
    private Matrix4f[] parentTransforms;

    /**
     * Initialize an animation object
     */
    public AnimatedObject(Game game, AnimatedModel animatedModel, Matrix4f modelMatrix) {
        super(game,animatedModel.getModel(),modelMatrix);
        this.animatedModel = animatedModel;
        jointTransforms = new Matrix4f[animatedModel.getArmature().size()];
        parentTransforms = new Matrix4f[animatedModel.getArmature().size()];

        for(int i = 0;i<animatedModel.getArmature().size();i++) {
            jointTransforms[i] = new Matrix4f();
            parentTransforms[i] = new Matrix4f();
        }

        defaultPose = animatedModel.getPose(0);
    }

    /**
     * Link joint transforms to a shader object
     */
    public void linkTransformsToShaderObject(ShaderObject shaderObject) {
        for(int i = 0;i<animatedModel.getArmature().size();i++) {
            shaderObject.addUniform(new UniformMatrix4(shaderObject.getShader(),"jointTransforms[" + i + "]",jointTransforms[i]));
        }
    }


    /**
     * Set the transforms to be of the given pose.
     */
    public void setPose(Pose pose) {
        this.defaultPose = pose;
    }

    /**
     * Apply an animation
     */
    public void applyAnimation(Animation animation) {
        for(String jointName : animation.getAffectedJoints()) {
            animationJointMap.put(jointName,animation);
        }
        currentAnimations.add(animation);
    }


    /**
     * Stop a specified animation
     */
    public void cancelAnimation(Animation animation) {
        for(String jointName : new HashSet<>(animationJointMap.keySet())) {
            if(animationJointMap.get(jointName)==animation) {
                animationJointMap.remove(jointName);
            }
        }
        currentAnimations.remove(animation);
    }
    /**
     * Stop all animations
     */
    public void cancelAllAnimations() {
        animationJointMap.clear();
        currentAnimations.clear();
    }

    /**
     * Recursively calculate all transforms for the current animation (Assumed not null)
     */
    private void recalculateTransforms(Joint currentJoint, Matrix4f transform) {
        //calculate local transform at the given pose based on the parent transform
        Matrix4f localTransform = getLocalTransform(currentJoint);
        transform.mul(localTransform);
        for(Joint child : currentJoint) {
            //pass the parent transform down, but copy to another memory location so the matrix doesn't get mixed up
            transform.get(parentTransforms[child.getId()]);
            recalculateTransforms(child,parentTransforms[child.getId()]);
        }
        //multiply by inverse bind transform and store in the array
        Matrix4f invBindTransform = currentJoint.getInverseBindTransform();
        transform.mul(invBindTransform,jointTransforms[currentJoint.getId()]);
    }

    /**
     * Get the local transform of a joint based on its current animation
     */
    private Matrix4f getLocalTransform(Joint joint) {
        if(animationJointMap.containsKey(joint.getName())) {
            //return the pose from the animation
            return animationJointMap.get(joint.getName()).getLocalTransform(joint.getName());
        }
        else{
            //return the transform from the default pose
            return defaultPose.getTransform(joint.getName()).getMatrix();
        }
    }


    /**
     * update() call
     */
    protected void updateAnimation() {
        for(Animation animation : currentAnimations) {
            animation.update();
        }
    }

    /**
     * render() call
     */
    protected void interpolateAnimation(float interpolation) {
        for(Animation animation : currentAnimations) {
            animation.calculateMatrices(interpolation);
        }
        Joint root = animatedModel.getArmature().getRootJoint();
        Matrix4f parentTransform = parentTransforms[root.getId()];
        parentTransform.identity();
        recalculateTransforms(root,parentTransform);
    }


    /**
     * Create a new pose which is equivalent to the current interpolation transforms
     */
    public Pose getCurrentPose() {
        Pose pose = new Pose(-1);
        for(Joint joint : animatedModel.getArmature()) {
            Matrix4f matrix = new Matrix4f();
            getLocalTransform(joint).get(matrix);
            pose.addTransform(joint,matrix);
        }
        return pose;
    }
}
