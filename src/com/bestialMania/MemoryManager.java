package com.bestialMania;

import com.bestialMania.rendering.Framebuffer;
import com.bestialMania.rendering.texture.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.sound.Sound;

import java.util.HashSet;
import java.util.Set;

public class MemoryManager {

    private Set<Texture> textures = new HashSet<>();
    private Set<Model> models = new HashSet<>();
    private Set<Framebuffer> framebuffers = new HashSet<>();
    private Set<Sound> sounds = new HashSet<>();

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
     * Add sound
     */
    public void addSound(Sound sound) {sounds.add(sound);}


    /**
     * Remove model
     */
    public void removeModel(Model model) {
        models.remove(model);
    }

    /**
     * Remove texture
     */
    public void removeTexture(Texture texture) {
        textures.remove(texture);
    }

    /**
     * Remove framebuffer
     */
    public void removeFramebuffer(Framebuffer framebuffer) {framebuffers.remove(framebuffer);}

    /**
     * Remove sound
     */
    public void removeSound(Sound sound) {sounds.remove(sound);}


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
        for(Sound sound : sounds) {
            sound.cleanUp();
        }
    }

}
