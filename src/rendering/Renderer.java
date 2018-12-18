package rendering;

import rendering.model.Model;
import rendering.shader.Shader;
import rendering.shader.Uniform;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private Shader shader;
    private List<Uniform> uniforms;
    private List<ShaderObject> objects;

    /**
     * Represents a shader with several shader objects
     * Will render all those objects when the render function is called.
     * Also has it's own uniforms to be used
     */
    public Renderer(Shader shader) {
        this.shader = shader;
        uniforms = new ArrayList<>();
        objects = new ArrayList<>();
    }

    /**
     * Add a uniform
     */
    public void addUniform(Uniform uniform) {
        uniforms.add(uniform);
    }


    /**
     * Add an object
     */
    /*public void addObject(ShaderObject object) {
        objects.add(object);
    }*/

    /**
     * Creates an object and adds it, also returns the object
     */
    public ShaderObject createObject(Model model) {
        ShaderObject object = new ShaderObject(shader,model);
        objects.add(object);
        return object;
    }

    /**
     * Render
     */
    public void render() {
        shader.bind();
        for(Uniform uniform : uniforms) uniform.bindUniform();
        for(ShaderObject object : objects) {
            object.render();
        }
    }
}
