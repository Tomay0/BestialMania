package com.bestialMania.object;

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformFloat;
import org.joml.Matrix4f;

public class StaticObject extends Object3D{
    protected Texture texture, normalTexture;
    protected float reflectivity, shineDamper;
    /**
     * Simple object with a reflectivity amount, shine damper and 1 texture
     */
    public StaticObject(Model model, Matrix4f modelMatrix, Texture texture, float reflectivity, float shineDamper) {
        super(model,modelMatrix);
        this.texture = texture;
        this.normalTexture = null;
        this.reflectivity = reflectivity;
        this.shineDamper = shineDamper;
    }

    /**
     * Simple object with a reflectivity amount, shine damper and diffuse/normal textures
     */
    public StaticObject(Model model, Matrix4f modelMatrix, Texture texture, Texture normalTexture, float reflectivity, float shineDamper) {
        super(model,modelMatrix);
        this.texture = texture;
        this.normalTexture = normalTexture;
        this.reflectivity = reflectivity;
        this.shineDamper = shineDamper;
    }

    @Override
    public void update() {}

    @Override
    public void interpolate(float interpolationAmount) {}

    @Override
    public void linkToRenderer(Renderer renderer) {
        ShaderObject shaderObject = createShaderObject(renderer);
        shaderObject.addTexture(0,texture);
        if(normalTexture!=null) shaderObject.addTexture(1,normalTexture);
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"reflectivity",reflectivity));
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"shineDamper",shineDamper));
    }
}
