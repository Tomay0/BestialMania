package com.bestialMania.rendering;

import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.rendering.shader.Uniform;
import com.bestialMania.rendering.texture.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private Shader shader;
    private List<Uniform> uniforms;
    private List<ShaderObject> objects;
    private Map<Integer, Texture> textures;

    private int cull = GL_BACK;

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
     * Set culling
     */
    public void setCull(int cull) {
        this.cull = cull;
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
     * Remove an object
     */
    public void removeObject(ShaderObject obj) {
        objects.remove(obj);
    }

    /**
     * Render
     */
    public void render() {
        if(cull==GL_FRONT) {
            glCullFace(GL_FRONT);
        }
        else if(cull==GL_NONE) {
            glDisable(GL_CULL_FACE);
        }
        shader.bind();
        for(Uniform uniform : uniforms) uniform.bindUniform();
        for(int slot : textures.keySet()) textures.get(slot).bind(slot);
        for(ShaderObject object : objects) {
            object.render();
        }
        if(cull==GL_FRONT) {
            glCullFace(GL_BACK);
        }
        else if(cull==GL_NONE) {
            glEnable(GL_CULL_FACE);
        }
    }

    /**
     * Get the shader
     */
    public Shader getShader() {return shader;}
}
