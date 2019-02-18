package com.bestialMania.object.animation;

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnimatedObject {
    private Model model;
    private Armature armature;
    private List<Pose> poses = new ArrayList<>();//all poses loaded from a file

    //joint transforms to be used in the shader
    private Matrix4f[] jointTransforms;

    //parent transforms
    private Matrix4f[] parentTransforms;

    private Animation currentAnimation = null;

    /*
    Potential TODO:
    - Allow multiple animations at once, one may control the legs and one may control hand movement (eg: walking while punching or some shit)
    - Allow swapping between different animations in a smoother way. Eg: swap between walking and jumping animations

     */

    /**
     * Object that can be animated
     * Has a model, armature and a list of poses to animate from.
     */
    public AnimatedObject(Model model, Armature armature) {
        this.model = model;
        this.armature = armature;
        jointTransforms = new Matrix4f[armature.size()];
        parentTransforms = new Matrix4f[armature.size()];

        for(int i = 0;i<armature.size();i++) {
            jointTransforms[i] = new Matrix4f();
            parentTransforms[i] = new Matrix4f();
        }
    }


    /**
     * Copy an animated object
     */
    public AnimatedObject(AnimatedObject copy) {
        this.model = copy.model;
        this.armature = copy.armature;
        for(Pose pose : copy.poses) {
            addPose(pose);
        }
        jointTransforms = new Matrix4f[armature.size()];
        parentTransforms = new Matrix4f[armature.size()];

        for(int i = 0;i<armature.size();i++) {
            jointTransforms[i] = new Matrix4f();
            parentTransforms[i] = new Matrix4f();
        }
    }

    public void addPose(Pose pose) {poses.add(pose);}
    public Pose getPose(int id) {return poses.get(id);}
    public List<Pose> getPoses() {return Collections.unmodifiableList(poses);}

    public Armature getArmature() {return armature;}
    public Model getModel() {
        return model;
    }

    /**
     * Set the transforms to be of the given pose.
     */
    public void setPose(Pose pose) {
        Joint root = armature.getRootJoint();
        Matrix4f parentTransform = parentTransforms[root.getId()];
        parentTransform.identity();
        recalculateTransforms(root, pose, parentTransform);
    }

    /**
     * Set the animation
     */
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    /**
     * Stop the animation
     */
    public void cancelAnimation() {
        currentAnimation = null;
    }

    /**
     * update() call
     */
    public void update() {
        if(currentAnimation!=null) {
            currentAnimation.update();
        }
    }

    /**
     * render() call
     */
    public void interpolate(float interpolation) {
        if(currentAnimation!=null) {
            currentAnimation.calculateMatrices(interpolation);
            Joint root = armature.getRootJoint();
            Matrix4f parentTransform = parentTransforms[root.getId()];
            parentTransform.identity();
            recalculateTransforms(root,parentTransform);
        }
    }


    /**
     * Recursively calculate all transforms for the given pose.
     */
    private void recalculateTransforms(Joint currentJoint, Pose pose, Matrix4f transform) {
        //calculate local transform at the given pose based on the parent transform
        Matrix4f localTransform = pose.getTransform(currentJoint.getName()).getMatrix();
        transform.mul(localTransform);
        for(Joint child : currentJoint) {
            //pass the parent transform down, but copy to another memory location so the matrix doesn't get mixed up
            transform.get(parentTransforms[child.getId()]);
            recalculateTransforms(child,pose,parentTransforms[child.getId()]);
        }
        //multiply by inverse bind transform and store in the array
        Matrix4f invBindTransform = currentJoint.getInverseBindTransform();
        transform.mul(invBindTransform,jointTransforms[currentJoint.getId()]);
    }


    /**
     * Recursively calculate all transforms for the current animation (Assumed not null)
     */
    private void recalculateTransforms(Joint currentJoint, Matrix4f transform) {
        //calculate local transform at the given pose based on the parent transform
        Matrix4f localTransform = currentAnimation.getMatrix(currentJoint.getId());
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
     * Creates a shader object and loads all the transforms to it
     */
    public ShaderObject createObject(Renderer renderer) {
        ShaderObject shaderObject = renderer.createObject(model);
        for(int i = 0;i<armature.size();i++) {
            shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"jointTransforms[" + i + "]",jointTransforms[i]));
        }
        return shaderObject;
    }

}
