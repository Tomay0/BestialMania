package com.bestialMania.object;

import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.texture.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformFloat;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;

public class StaticObject extends Object3D{
    protected Texture texture, normalTexture;
    protected float reflectivity, shineDamper;
    protected boolean lighting = true, glowing = false;
    protected float glow;
    /**
     * Simple glowing object with a texture
     */
    public StaticObject(Game game, Model model, Matrix4f modelMatrix, Texture texture, float glow) {
        super(game,model,modelMatrix);
        this.texture = texture;
        this.normalTexture = null;
        lighting = false;
        glowing = true;
        this.glow = glow;

    }
    /**
     * Simple object with a reflectivity amount, shine damper and 1 texture
     */
    public StaticObject(Game game, Model model, Matrix4f modelMatrix, Texture texture, float reflectivity, float shineDamper) {
        super(game,model,modelMatrix);
        this.texture = texture;
        this.normalTexture = null;
        this.reflectivity = reflectivity;
        this.shineDamper = shineDamper;
    }

    /**
     * Simple object with a reflectivity amount, shine damper and diffuse/normal textures
     */
    public StaticObject(Game game, Model model, Matrix4f modelMatrix, Texture texture, Texture normalTexture, float reflectivity, float shineDamper) {
        super(game,model,modelMatrix);
        this.texture = texture;
        this.normalTexture = normalTexture;
        this.reflectivity = reflectivity;
        this.shineDamper = shineDamper;
    }

    @Override
    public void update() {
    }

    @Override
    public void interpolate(float interpolationAmount) {}

    @Override
    public ShaderObject linkToRenderer(Renderer renderer) {
        ShaderObject shaderObject = createShaderObject(renderer);
        shaderObject.addTexture(0,texture);
        if(normalTexture!=null) shaderObject.addTexture(1,normalTexture);
        if(lighting) {
            shaderObject.addUniform(new UniformFloat(renderer.getShader(),"reflectivity",reflectivity));
            shaderObject.addUniform(new UniformFloat(renderer.getShader(),"shineDamper",shineDamper));
        }
        if(glowing) shaderObject.addUniform(new UniformFloat(renderer.getShader(),"glow",glow));
        return shaderObject;
    }
}
