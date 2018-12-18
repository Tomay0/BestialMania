package rendering;

import rendering.model.Model;
import rendering.shader.Shader;
import rendering.shader.Uniform;

import java.util.ArrayList;
import java.util.List;

public class ShaderObject {
    private Shader shader;
    private Model model;
    private List<Uniform> uniforms;

    /**
     * Represents a model with some uniforms to be drawn within a shader
     */
    public ShaderObject(Shader shader, Model model) {
        this.shader = shader;
        this.model = model;
        uniforms = new ArrayList<>();
    }

    /**
     * Add a uniform
     */
    public void addUniform(Uniform uniform) {
        uniforms.add(uniform);
    }

    /**
     * Renders the object
     */
    public void render() {
        for(Uniform uniform : uniforms) uniform.bindUniform();
        model.draw();
    }
}
