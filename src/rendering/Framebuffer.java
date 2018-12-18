package rendering;

import rendering.shader.Shader;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Framebuffer {
    private static int DISPLAY_WIDTH = 640;
    private static int DISPLAY_HEIGHT = 480;

    private int fbo;
    private int width, height;
    private boolean depthEnabled;
    private List<Texture> textures = new ArrayList<>();
    private List<Integer> buffers = new ArrayList<>();
    private List<Renderer> renderers = new ArrayList<>();

    /**
     * Empty framebuffer - just draw to the screen
     */
    public Framebuffer() {
        fbo = 0;
        //TODO get the width and height of the window
        width = DISPLAY_WIDTH;
        height = DISPLAY_HEIGHT;
        depthEnabled = true;
    }

    /**
     * Framebuffer of width, height and number of textures to output to.
     */
    public Framebuffer(int width, int height, int nTextures, boolean depthEnabled) {
        //generate
        fbo = glGenFramebuffers();
        this.width = width;
        this.height = height;
        this.depthEnabled = depthEnabled;
        glBindFramebuffer(GL_FRAMEBUFFER,fbo);

        //textures
        for(int i = 0;i<nTextures;i++) {
            Texture texture = new Texture(width,height);
            texture.genFramebufferTexture(i);
            textures.add(texture);
        }

        //depth render buffers
        if(depthEnabled) {
            int dbuffer = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER,dbuffer);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, dbuffer);
            buffers.add(dbuffer);
        }
        //draw buffers
        for(int i = 0;i<nTextures;i++) {
            glDrawBuffer(GL_COLOR_ATTACHMENT0+i);
        }

        //check
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Error: Could not create framebuffer.");
            System.exit(-1);
        }
        //unbind
        glBindFramebuffer(GL_FRAMEBUFFER,0);
    }

    /**
     * Renders the framebuffer
     */
    public void render(){
        bind();
        for(Renderer renderer : renderers) renderer.render();

        unbind();
    }

    /**
     * Bind the framebuffer
     * Clear the screen
     */
    public void bind() {
        if(fbo>0) {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER,fbo);
            glViewport(0,0,width,height);
        }
        
        glClear(GL_COLOR_BUFFER_BIT);
        if(!depthEnabled) {
            glDisable(GL_DEPTH_TEST);
        }else{
            glClear(GL_DEPTH_BUFFER_BIT);
        }

    }

    /**
     * Unbind the framebuffer
     * finish up
     */
    public void unbind() {
        if(!depthEnabled) glEnable(GL_DEPTH_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
    }

    /**
     * Delete framebuffer and textures from memory
     */
    public void cleanUp() {
        glDeleteFramebuffers(fbo);
        for(int buffer : buffers) glDeleteRenderbuffers(buffer);
        for(Texture texture : textures) texture.cleanUp();
    }

    /**
     * Adds a new renderer to the list
     */
    /*public void addRenderer(Renderer renderer) {
        renderers.add(renderer);
    }*/

    /**
     * Creates a new renderer with a given shader and adds to the list.
     * Also returns the new renderer
     */
    public Renderer createRenderer(Shader shader) {
        Renderer renderer = new Renderer(shader);
        renderers.add(renderer);
        return renderer;
    }

}
