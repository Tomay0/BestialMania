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

public class ShaderObject {
    private Shader shader;
    private Model model;
    private List<Uniform> uniforms;
    private Map<Integer, Texture> textures;

    private boolean depth = true;

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
     * Whether depth should be used
     */
    public void disableDepth() {
        depth = false;
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
        if(!depth) glDepthFunc(GL_LEQUAL);
        for(Uniform uniform : uniforms) uniform.bindUniform();
        for(int slot : textures.keySet()) textures.get(slot).bind(slot);
        model.draw();
        if(!depth) glDepthFunc(GL_LESS);
    }

    public Shader getShader() {
        return shader;
    }
}
