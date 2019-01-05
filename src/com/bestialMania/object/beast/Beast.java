package com.bestialMania.object.beast;

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * TODO:
 * Make movement smoother, so you don't turn instantly in another direction
 */
public class Beast {
    private Vector3f position;
    private Vector2f lookDirection;//direction they are facing
    private Matrix4f modelMatrix;
    private float speed;//current speed

    //TODO expand rendering out more to allow for animation
    private Model model;
    private Texture texture;
    private ShaderObject shaderObject;

    /**
     * Create a beast
     */
    public Beast(Model model, Texture texture){
        position = new Vector3f(0,0,0);//TODO have some sort of spawn point
        lookDirection = new Vector2f(0,-1);
        modelMatrix = new Matrix4f();
        speed = 0;
        this.model = model;
        this.texture = texture;
    }

    /**
     * Set a new direction
     */
    public void setDirection(Vector2f direction) {
        this.lookDirection = direction;
    }

    /**
     * Set a new speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Update position and orientation
     */
    public void update() {
        position.x+=lookDirection.x*speed;
        position.z+=lookDirection.y*speed;

        //recalculate matrix
        modelMatrix.identity();
        modelMatrix.translate(position);
        modelMatrix.scale(0.1f,0.1f,0.1f);
        //TODO add rotation
    }

    /**
     * Add the model to the renderer
     */
    public void linkToRenderer(Renderer renderer) {
        shaderObject = renderer.createObject(model);
        shaderObject.addTexture(0,texture);
        shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix",modelMatrix));
    }

}
