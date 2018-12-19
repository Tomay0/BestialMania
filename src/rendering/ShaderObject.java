package rendering;

import rendering.model.Model;
import rendering.shader.Shader;
import rendering.shader.Uniform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaderObject {
    private Shader shader;
    private Model model;
    private List<Uniform> uniforms;
    private Map<Integer,Texture> textures;

    /**
     * Represents a model with some uniforms to be drawn within a shader
     */
    public ShaderObject(Shader shader, Model model) {
        this.shader = shader;
        this.model = model;
        uniforms = new ArrayList<>();
        textures = new HashMap<>();
    }

    /**
     * Add a uniform
     */
    public void addUniform(Uniform uniform) {
        uniforms.add(uniform);
    }
    /**
     * Add a texture
     */
    public void addTexture(int slot, Texture texture) {
        textures.put(slot,texture);
    }

    /**
     * Renders the object
     */
    public void render() {
        for(Uniform uniform : uniforms) uniform.bindUniform();
        for(int slot : textures.keySet()) textures.get(slot).bind(slot);
        model.draw();
    }
}
