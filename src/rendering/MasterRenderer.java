package rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterRenderer {
    private List<Framebuffer> framebuffers;
    private Framebuffer window;

    /**
     * Renders everything in the scene.
     * contains a list of framebuffers in order to render to,
     * each framebuffer contains their own renderers which contain the objects
     */
    public MasterRenderer() {
        framebuffers = new ArrayList<>();
        window = new Framebuffer();
        framebuffers.add(window);
    }

    public Framebuffer getWindowFramebuffer() {return window;}


    /**
     * Adds a framebuffer to the list at the end
     */
    public void addFramebufferEnd(Framebuffer framebuffer) {
        framebuffers.add(framebuffer);
    }

    /**
     * Renders the entire scene
     */
    public void render() {
        for(Framebuffer framebuffer : framebuffers) framebuffer.render();
    }
}
