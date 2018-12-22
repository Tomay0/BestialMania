package com.bestialMania.rendering;

import com.bestialMania.DisplaySettings;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    public Texture(int type, int width, int height) {
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
        if(mipmap) glGenerateMipmap(type);
        glTexParameterf(type,GL_TEXTURE_MIN_FILTER,filter);
        glTexParameterf(type,GL_TEXTURE_MAG_FILTER,filter);
        glTexParameterf(type,GL_TEXTURE_WRAP_S,wrap);
        glTexParameterf(type,GL_TEXTURE_WRAP_T,wrap);
        if(mipmap && DisplaySettings.ANISOTROPIC_FILTERING) {
            glTexParameterf(type, GL_TEXTURE_LOD_BIAS, 0.4f);
            float amount = GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
            if (amount > 4) amount = 4;
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
     * Load a texture from a file with default settings for 3D textures
     */
    public static Texture loadImageTexture3D(String fileName) {return loadImageTexture(fileName,GL_RGBA,GL_LINEAR,GL_REPEAT,true);}

    /**
     * Load a texture from a file with default settings for 2D textures
     */
    public static Texture loadImageTexture2D(String fileName) {return loadImageTexture(fileName,GL_RGBA,GL_LINEAR,GL_CLAMP_TO_EDGE,false);}

    /**
     * Load a texture from a file and return the texture
     */
    public static Texture loadImageTexture(String fileName, int format, int filter, int wrap, boolean mipmap) {
        try {
            //load image
            BufferedImage image = ImageIO.read(new File(fileName));
            int width = image.getWidth();
            int height = image.getHeight();

            //convert to pixel array
            int[] pixels = new int[width*height*4];
            pixels = image.getRGB(0,0,width,height,null,0,width);

            //convert to byte buffer
            ByteBuffer buffer = BufferUtils.createByteBuffer(width*height*4);

            for(int y = height-1;y>=0;y--) {
                for(int x = 0;x<width;x++) {
                    int px = pixels[y*width+x];
                    buffer.put((byte) ((px >> 16) & 0xFF));//R
                    buffer.put((byte) ((px >> 8) & 0xFF));//G
                    buffer.put((byte) (px & 0xFF));//B
                    buffer.put((byte) ((px >> 24) & 0xFF));//A
                }
            }

            buffer.flip();

            //generate the texture
            Texture texture = new Texture(GL_TEXTURE_2D, width,height);
            texture.genBufferedTexture(format, buffer);
            texture.applyFilters(filter,wrap,mipmap);

            return texture;
        }catch(IOException e) {
            System.err.println("Could not load image: " + fileName);
            System.exit(-1);
        }
        return null;
    }

    /**
     * Width
     */
    public int getWidth() {return width;}

    /**
     * Height
     */
    public int getHeight() {return height;}
}
