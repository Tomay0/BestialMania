package com.bestialMania.object;

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public abstract class Object3D {
    protected Matrix4f modelMatrix;
    protected Model model;
    protected Game game;

    private Map<ShaderObject, Renderer> shaderObjects = new HashMap<>();

    public Object3D(Game game, Model model, Matrix4f modelMatrix) {
        this.model = model;
        this.modelMatrix = modelMatrix;
        this.game = game;

    }


    /**
     * Create a shader object and link the model matrix
     */
    public ShaderObject createShaderObject(Renderer renderer) {
        ShaderObject shaderObject = renderer.createObject(model);
        shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix",modelMatrix));
        shaderObjects.put(shaderObject, renderer);
        return shaderObject;
    }

    /**
     * Remove the object from all of its renderers
     */
    public void removeObject() {
        for(ShaderObject shaderObject : shaderObjects.keySet()) {
           Renderer renderer = shaderObjects.get(shaderObject);
           renderer.removeObject(shaderObject);
        }
        game.removeObject(this);
    }

    /**
     * Update
     */
    public abstract void update();

    /**
     * Update interpolation
     */
    public abstract void interpolate(float interpolationAmount);

    /**
     * Main renderer linking method for the object's major renderer
     */
    public abstract void linkToRenderer(Renderer renderer);
}
