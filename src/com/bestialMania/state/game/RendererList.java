package com.bestialMania.state.game;

import com.bestialMania.object.beast.Player;
import com.bestialMania.rendering.Framebuffer;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.shader.Shader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Useful way of representing renderers for in-game.
 * there is 1 renderer per person in split-screen.
 * Information about what variables to load to the shader is given.
 *
 * All instances of this will have the player's view matrix
 */
public class RendererList implements Iterable<Renderer>{
    private Shader shader;
    private List<Renderer> renderers = new ArrayList<>();
    private boolean receiveShadows, viewMatrixDirOnly;

    /**
     * Initialize the renderer list.
     * Give the shader that will be used and some parameters
     */
    public RendererList(Shader shader, boolean receiveShadows, boolean viewMatrixDirOnly) {
        this.shader = shader;
        this.receiveShadows = receiveShadows;
        this.viewMatrixDirOnly = viewMatrixDirOnly;
    }

    /**
     * Create a renderer to add to the list (do in order of player id)
     * with the corresponding player and fbo.
     */
    public void createRenderer(Framebuffer fbo, Player player) {
        Renderer renderer = fbo.createRenderer(shader);
        renderers.add(renderer);
        if(viewMatrixDirOnly) player.linkCameraDirectionToRenderer(renderer);
        else player.linkCameraToRenderer(renderer);
    }

    public Shader getShader() {
        return shader;
    }

    @Override
    public Iterator<Renderer> iterator() {
        return renderers.iterator();
    }

    public boolean receivesShadows() {
        return receiveShadows;
    }

    public Renderer getRenderer(int id) {
        return renderers.get(id);
    }

    public int size() {return renderers.size();}
}
