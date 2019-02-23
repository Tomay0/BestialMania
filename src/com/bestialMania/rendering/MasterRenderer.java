package com.bestialMania.rendering;

import com.bestialMania.rendering.model.Model;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for rendering everything to the screen and keeping track of everything stored to memory
 *
 */
public class MasterRenderer {
    private List<Framebuffer> framebuffers = new ArrayList<>();
    private Framebuffer window, windowResized;

    /**
     * Renders everything in the scene.
     * contains a list of framebuffers in order to render to,
     * each framebuffer contains their own renderers which contain the objects
     */
    public MasterRenderer() {
        window = new Framebuffer();
        framebuffers.add(window);
    }

    public Framebuffer getWindowFramebuffer() {return window;}

    /**
     * Adds a framebuffer to the list at the end
     */
    public void addFramebuffer(Framebuffer framebuffer) {
        framebuffers.add(framebuffers.size()-1,framebuffer);
    }

    /**
     * Renders the entire scene
     */
    public void render() {
        for(Framebuffer framebuffer : framebuffers) framebuffer.render();
    }
}
