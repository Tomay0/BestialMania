package com.bestialMania.object;

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;

public abstract class Object3D {
    protected Matrix4f modelMatrix;
    protected Model model;
    public Object3D(Model model, Matrix4f modelMatrix) {
        this.model = model;
        this.modelMatrix = modelMatrix;

    }

    public abstract void update();
    public abstract void interpolate(float interpolationAmount);


    /**
     * Create a shader object and link the model matrix
     */
    public ShaderObject createShaderObject(Renderer renderer) {
        ShaderObject shaderObject = renderer.createObject(model);
        shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix",modelMatrix));
        return shaderObject;
    }
    public abstract void linkToRenderer(Renderer renderer);
}
