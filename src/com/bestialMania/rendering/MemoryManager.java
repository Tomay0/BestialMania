package com.bestialMania.rendering;

import com.bestialMania.rendering.model.Model;

import java.util.HashSet;
import java.util.Set;

public class MemoryManager {

    private Set<Texture> textures = new HashSet<>();
    private Set<Model> models = new HashSet<>();
    private Set<Framebuffer> framebuffers = new HashSet<>();

    /**
     * Add model
     */
    public void addModel(Model model) {
        models.add(model);
    }

    /**
     * Add texture
     */
    public void addTexture(Texture texture) {
        textures.add(texture);
    }

    /**
     * Add framebuffer
     */
    public void addFramebuffer(Framebuffer framebuffer) {framebuffers.add(framebuffer);}

    /**
     * Removes everything from memory
     */
    public void cleanUp() {
        for(Texture texture : textures) {
            texture.cleanUp();
        }
        for(Model model : models) {
            model.cleanUp();
        }
        for(Framebuffer framebuffer : framebuffers) {
            framebuffer.cleanUp();
        }
    }

}
