package com.bestialMania.object;

import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;

public abstract class AnimatedObject extends Object3D{
    protected AnimatedModel animatedModel;
    public AnimatedObject(Game game, AnimatedModel animatedModel, Matrix4f modelMatrix) {
        super(game,animatedModel.getModel(),modelMatrix);
        this.animatedModel = animatedModel;
    }

    /**
     * Link joint transforms to a shader object
     */
    public void linkTransformsToShaderObject(ShaderObject shaderObject) {
        for(int i = 0;i<animatedModel.getArmature().size();i++) {
            shaderObject.addUniform(new UniformMatrix4(shaderObject.getShader(),"jointTransforms[" + i + "]",animatedModel.getJointTransform(i)));
        }
    }
}
