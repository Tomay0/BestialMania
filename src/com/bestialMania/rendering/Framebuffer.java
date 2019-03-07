package com.bestialMania.rendering;

import com.bestialMania.MemoryManager;
import com.bestialMania.Settings;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.rendering.texture.Texture;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Framebuffer {

    private int fbo;
    private int width, height;
    private boolean depthEnabled = false,mipmapping = false;
    private Vector4f backgroundColor = new Vector4f(0,0,0,0);
    private List<Texture> textures = new ArrayList<>();
    private List<Integer> buffers = new ArrayList<>();
    private List<Renderer> renderers = new ArrayList<>();
    private List<Integer> blitFbos = new ArrayList<>();

    /**
     * Screen framebuffer (no depth, must use another framebuffer to use depth)
     */
    public Framebuffer() {
        fbo = 0;
        width = Settings.WIDTH;
        height =  Settings.HEIGHT;
    }

    /**
     * Initialize a framebuffer
     */
    public Framebuffer(int width, int height) {
        fbo = glGenFramebuffers();
        this.width = width;
        this.height = height;
        glBindFramebuffer(GL_FRAMEBUFFER,fbo);
    }

    /**
     * Generate a list of textures for a frame buffer
     * @param nTextures number of textures to output
     * @param internalFormat internal format eg: GL_RGB, GL_RGBA or GL_RGBA32F
     * @param format format eg: GL_RGB, GL_RGBA
     * @param formatType type of format eg: GL_UNSIGNED_INT or GL_FLOAT
     * @param filter filter to use eg: GL_LINEAR
     * @param wrap texture wrapping eg: GL_CLAMP_TO_EDGE
     * @param mipmap if mipmapping/anisotropic filtering should be used
     */
    private void genTextures(MemoryManager mm, int nTextures, int internalFormat, int format, int formatType, int filter, int wrap, boolean mipmap) {
        for(int i = 0;i<nTextures;i++) {
            Texture texture = new Texture(mm,GL_TEXTURE_2D,width,height);
            texture.genFramebufferTexture(i,internalFormat,format,formatType);
            texture.applyFilters(filter,wrap,mipmap);
            textures.add(texture);
        }

    }
    private void genDepthTexture(MemoryManager mm, int filter, int wrap, boolean mipmap) {
        Texture texture = new Texture(mm,GL_TEXTURE_2D,width,height);
        texture.genFramebufferDepthTexture();
        texture.applyFilters(filter,wrap,mipmap);
        textures.add(texture);
    }

    /**
     * Generate renderbuffers for a multisampled fbo
     */
    private void genMultisampleRenderbuffers(int nTextures) {
        for (int i = 0; i < nTextures; i++) {
            int buffer = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, buffer);
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, Settings.ANTIALIASING, GL_RGBA8, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_RENDERBUFFER, buffer);
            buffers.add(buffer);
        }
    }

    /**
     * Generate a depth render buffer
     */
    private void genDepthRenderbuffers() {
        int dbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER,dbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, dbuffer);
        buffers.add(dbuffer);
    }

    /**
     * Generate a multisampled depth render buffer
     */
    private void genMultisampledDepthRenderbuffers() {
        int dbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER,dbuffer);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, Settings.ANTIALIASING, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, dbuffer);
        buffers.add(dbuffer);
    }

    /**
     * Generate some framebuffers with a single texture for
     */
    private void genMultisampledFboTextures(MemoryManager mm, int nTextures, int internalFormat, int format, int formatType, int filter, int wrap, boolean mipmap) {
        for(int i = 0; i < nTextures;i++) {
            int blitFbo = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER,blitFbo);
            Texture texture = new Texture(mm,GL_TEXTURE_2D,width,height);
            texture.genFramebufferTexture(0,internalFormat,format,formatType);
            texture.applyFilters(filter,wrap,mipmap);
            textures.add(texture);
            blitFbos.add(blitFbo);
        }
    }

    /**
     * Enable draw buffers
     */
    private void genDrawBuffers(int nTextures) {
        for(int i = 0;i<nTextures;i++) {
            glDrawBuffer(GL_COLOR_ATTACHMENT0+i);
        }
    }

    /**
     * Check that the framebuffer was created correctly
     */
    private boolean checkStatus() {
        return glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE;
    }

    /**
     * Renders the framebuffer
     */
    public void render(){
        bind();
        for(Renderer renderer : renderers) renderer.render();

        resolve();
    }

    /**
     * Set the background colour
     */
    public void setBackgroundColor(Vector3f color) {
        backgroundColor.x = color.x;
        backgroundColor.y = color.y;
        backgroundColor.z = color.z;
        backgroundColor.w = 1;
    }

    /**
     * Bind the framebuffer
     * Clear the screen
     */
    public void bind() {
        if(fbo>0) {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER,fbo);
            glViewport(0,0,width,height);
        }else unbind();

        glClearColor(backgroundColor.x,backgroundColor.y,backgroundColor.z,backgroundColor.w);
        glClear(GL_COLOR_BUFFER_BIT);
        if(!depthEnabled) {
            glDisable(GL_DEPTH_TEST);
        }else{
            glClear(GL_DEPTH_BUFFER_BIT);
        }

    }

    /**
     * Do necessary steps to finish rendering of the framebuffer
     */
    public void resolve() {
        if(!depthEnabled) glEnable(GL_DEPTH_TEST);

        glBindFramebuffer(GL_READ_FRAMEBUFFER,fbo);
        //blit to fbos for multisampled framebuffers
        for(int i = 0;i<blitFbos.size();i++) {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER,blitFbos.get(i));
            glReadBuffer(GL_COLOR_ATTACHMENT0 + i);
            glBlitFramebuffer(0,0,width,height,0,0,width,height,GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT,GL_NEAREST);
        }

        //generate mipmaps
        if(mipmapping) {
            for(Texture texture : textures) {
                texture.genMipmap();
            }
        }

        unbind();
    }


    /**
     * Unbind the framebuffer
     */
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Settings.WIDTH, Settings.HEIGHT);
    }

    /**
     * Delete framebuffer and textures from memory
     */
    public void cleanUp() {
        glDeleteFramebuffers(fbo);
        for(int buffer : buffers) glDeleteRenderbuffers(buffer);
        for(int blitFbo : blitFbos) glDeleteFramebuffers(blitFbo);
    }

    /**
     * Gets the texture at the given slot
     */
    public Texture getTexture(int slot) {
        return textures.get(slot);
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

    /**
     * Create a normal framebuffer and return it
     */
    public static Framebuffer createFramebuffer(MemoryManager mm, int width, int height, boolean depthEnabled, int nTextures, int internalFormat, int format, int formatType, int filter, int wrap, boolean mipmap) {
        Framebuffer framebuffer = new Framebuffer(width,height);
        framebuffer.depthEnabled = depthEnabled;
        framebuffer.mipmapping = mipmap;
        framebuffer.genTextures(mm,nTextures,internalFormat,format,formatType,filter,wrap,mipmap);
        if(depthEnabled) framebuffer.genDepthRenderbuffers();
        framebuffer.genDrawBuffers(nTextures);

        if(!framebuffer.checkStatus()) {
            System.out.println("Error: Could not create framebuffer.");
            System.exit(-1);
        }
        mm.addFramebuffer(framebuffer);
        return framebuffer;
    }


    /**
     * Create a depth framebuffer and return it
     */
    public static Framebuffer createDepthFramebuffer(MemoryManager mm,int width, int height, int filter, int wrap, boolean mipmap) {
        Framebuffer framebuffer = new Framebuffer(width,height);
        framebuffer.depthEnabled = true;
        framebuffer.mipmapping = mipmap;
        framebuffer.genDepthTexture(mm, filter,wrap,mipmap);
        glDrawBuffers(GL_NONE);

        if(!framebuffer.checkStatus()) {
            System.out.println("Error: Could not create framebuffer.");
            System.exit(-1);
        }
        mm.addFramebuffer(framebuffer);
        return framebuffer;
    }

    /**
     * Create a multisampled framebuffer and return it
     */
    public static Framebuffer createMultisampledFramebuffer(MemoryManager mm, int width, int height, boolean depthEnabled, int nTextures, int internalFormat, int format, int formatType, int filter, int wrap, boolean mipmap) {
        Framebuffer framebuffer = new Framebuffer(width,height);
        framebuffer.depthEnabled = depthEnabled;
        framebuffer.mipmapping = mipmap;
        framebuffer.genMultisampleRenderbuffers(nTextures);
        if(depthEnabled) framebuffer.genMultisampledDepthRenderbuffers();
        framebuffer.genDrawBuffers(nTextures);
        framebuffer.genMultisampledFboTextures(mm,nTextures,internalFormat,format,formatType,filter,wrap,mipmap);

        if(!framebuffer.checkStatus()) {
            System.out.println("Error: Could not create framebuffer.");
            System.exit(-1);
        }
        mm.addFramebuffer(framebuffer);

        return framebuffer;
    }

    /**
     * Create a normal framebuffer with:
     * - depth enabled
     * - 1 texture
     * - RGBA/UINT format
     * - LINEAR filter
     * - Clamp to edge
     * - No mipmap
     */
    public static Framebuffer createFramebuffer3Dto2D(MemoryManager mm, int width, int height) {
        return createFramebuffer(mm,width,height,true,1,GL_RGBA,GL_RGBA,GL_UNSIGNED_INT,GL_LINEAR,GL_CLAMP_TO_EDGE,false);
    }

    /**
     * Create a multisampled framebuffer with:
     * - depth enabled
     * - 1 texture
     * - RGBA/UINT format
     * - LINEAR filter
     * - Clamp to edge
     * - No mipmap
     */
    public static Framebuffer createMultisampledFramebuffer3Dto2D(MemoryManager mm, int width, int height) {
        return createMultisampledFramebuffer(mm, width,height,true,1,GL_RGBA,GL_RGBA,GL_UNSIGNED_INT,GL_LINEAR,GL_CLAMP_TO_EDGE,false);
    }

    /**
     * Create a depth frame buffer with:
     * - linear filter
     * - clamp to edge
     * - No mipmap
     */
    public static Framebuffer createDepthFramebuffer2D(MemoryManager mm, int width, int height) {
        return createDepthFramebuffer(mm,width,height,GL_LINEAR,GL_CLAMP_TO_EDGE,false);
    }
}
