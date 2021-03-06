package com.bestialMania.rendering.texture;

import com.bestialMania.MemoryManager;
import com.bestialMania.Settings;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Texture {
    private int texture;
    private int type;
    private int width,height;

    /**
     * Initialize a texture of specified width and height
     */
    public Texture(MemoryManager mm, int type, int width, int height) {
        mm.addTexture(this);
        this.type = type;
        this.width = width;
        this.height = height;
        texture = glGenTextures();
        glBindTexture(type,texture);
    }

    /**
     * Generate a texture from a buffer (for images)
     */
    public void genBufferedTexture(int format, ByteBuffer buffer) {
        glTexImage2D(type,0,format,width,height,0,format,GL_UNSIGNED_BYTE,buffer);
    }

    /**
     * Generate a cube map texture
     */
    public void genCubemapTexture(int index, int format, ByteBuffer buffer) {
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + index,0,format,width,height,0,format,GL_UNSIGNED_BYTE,buffer);
    }

    /**
     * Generate a framebuffer texture
     */
    public void genFramebufferTexture(int target, int internalFormat, int format, int formatType) {
        glTexImage2D(type,0,internalFormat,width,height,0,format,formatType, NULL);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0 + target,type,texture,0);
    }

    /**
     * Generate a framebuffer depth texture
     */
    public void genFramebufferDepthTexture() {
        glTexImage2D(type,0,GL_DEPTH_COMPONENT32,width,height,0,GL_DEPTH_COMPONENT,GL_FLOAT, NULL);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,type,texture,0);
    }

    /**
     * Apply filters to the texture
     */
    public void applyFilters(int filter, int wrap, boolean mipmap) {
        int minFilter = filter;
        if(mipmap) {
            glGenerateMipmap(type);
            if(filter==GL_LINEAR) minFilter = GL_LINEAR_MIPMAP_LINEAR;
            else if(filter==GL_NEAREST) minFilter = GL_NEAREST_MIPMAP_NEAREST;
            glTexParameterf(type, GL_TEXTURE_LOD_BIAS, -0.4f);
        }

        glTexParameterf(type,GL_TEXTURE_MIN_FILTER,minFilter);
        glTexParameterf(type,GL_TEXTURE_MAG_FILTER,filter);
        glTexParameterf(type,GL_TEXTURE_WRAP_S,wrap);
        glTexParameterf(type,GL_TEXTURE_WRAP_T,wrap);
        if(mipmap && Settings.ANISOTROPIC_FILTERING>0) {
            float amount = GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
            if (amount > Settings.ANISOTROPIC_FILTERING) amount = Settings.ANISOTROPIC_FILTERING;
            glTexParameterf(type, GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
        }
    }

    /**
     * Bind the texture to the given position
     */
    public void bind(int position) {
        glActiveTexture(GL_TEXTURE0 + position);
        glBindTexture(type,texture);
    }

    /**
     * Generate a mipmap
     */
    public void genMipmap() {
        bind(0);
        glGenerateMipmap(type);
    }


    /**
     * Removes the texture from memory
     */
    public void cleanUp() {
        glDeleteTextures(texture);
    }

    /**
     * Width
     */
    public int getWidth() {return width;}

    /**
     * Height
     */
    public int getHeight() {return height;}

    /**
     * Load a texture from a file with default settings for 3D textures
     */
    public static Texture loadImageTexture3D(MemoryManager mm, String fileName) {return loadImageTexture(mm,fileName,GL_RGBA,GL_LINEAR,GL_REPEAT,true);}

    /**
     * Load a texture from a file with default settings for 2D textures
     */
    public static Texture loadImageTexture2D(MemoryManager mm, String fileName) {return loadImageTexture(mm,fileName,GL_RGBA,GL_LINEAR,GL_CLAMP_TO_EDGE,false);}

    /**
     * Load a texture from a file and return the texture
     */
    public static Texture loadImageTexture(MemoryManager mm, String fileName, int format, int filter, int wrap, boolean mipmap) {
        try {
            //load image
            TextureImage image = TextureImage.loadImage(fileName);

            //generate the texture
            Texture texture = new Texture(mm,GL_TEXTURE_2D, image.getWidth(),image.getHeight());
            texture.genBufferedTexture(format, image.getByteBuffer());
            texture.applyFilters(filter,wrap,mipmap);

            return texture;
        }catch(Exception e) {
            System.err.println("Could not load image: " + fileName);
            System.exit(-1);
        }
        return null;
    }

    /**
     * Load a cube map texture
     */
    public static Texture loadCubemapTexture(MemoryManager mm, String path) {
        try {
            String[] files = new String[]{
                    path + "_r.bmt",
                    path + "_l.bmt",
                    path + "_u.bmt",
                    path + "_d.bmt",
                    path + "_b.bmt",
                    path + "_f.bmt"
            };
            TextureImage[] images = new TextureImage[6];
            int width = 0,height = 0;

            for(int i = 0;i<6;i++) {
                images[i] = TextureImage.loadImage(files[i]);
                if(width==0) {
                    width = images[i].getWidth();
                    height = images[i].getHeight();
                }
                else if(width!=images[i].getWidth() || height!=images[i].getHeight()) {
                    System.err.println("Could not load cube map images: " + path + "_x.bmt");
                    System.err.println("All must have the same dimensions");
                    System.exit(-1);
                }


            }
            Texture texture = new Texture(mm,GL_TEXTURE_CUBE_MAP,width,height);
            for(int i = 0;i<6;i++) {
                texture.genCubemapTexture(i,GL_RGBA,images[i].getByteBuffer());
            }
            texture.applyFilters(GL_LINEAR,GL_CLAMP_TO_EDGE, false);

            return texture;
        }catch(Exception e) {
            System.err.println("Could not load cube map images: " + path + "_x.bmt");
            System.exit(-1);
        }
        return null;
    }
}
