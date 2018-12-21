package com.bestialMania.rendering;

import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.rendering.shader.Uniform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {
    private Shader shader;
    private List<Uniform> uniforms;
    private List<ShaderObject> objects;
    private Map<Integer,Texture> textures;

    /**
     * Represents a shader with several shader objects
     * Will render all those objects when the render function is called.
     * Also has it's own uniforms to be used
     */
    public Renderer(Shader shader) {
        this.shader = shader;
        uniforms = new ArrayList<>();
        objects = new ArrayList<>();
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
        for(int slot : textures.keySet()) textures.get(slot).bind(slot);
        for(ShaderObject object : objects) {
            object.render();
        }
    }

    /**
     * Get the shader
     */
    public Shader getShader() {return shader;}
}
