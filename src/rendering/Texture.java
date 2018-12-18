package rendering;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Texture {
    private int texture;
    private int type;
    private int width,height;

    /**
     * Load a texture from a file
     */
    public Texture(String file) {
        type = GL_TEXTURE_2D;
        try {
            //load image
            BufferedImage image = ImageIO.read(new File(file));
            width = image.getWidth();
            height = image.getHeight();

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
            texture = glGenTextures();
            glBindTexture(type,texture);

            //TODO make filters adjustable
            applyFilters();

            glTexImage2D(type,0,GL_RGBA,width,height,0,GL_RGBA,GL_UNSIGNED_BYTE,buffer);

        }catch(IOException e) {
            System.err.println("Could not load image: " + file);
            System.exit(-1);
        }
    }

    /**
     * Initialize a texture of specified width and height
     */
    public Texture(int width, int height) {
        type = GL_TEXTURE_2D;
        this.width = width;
        this.height = height;
        texture = glGenTextures();
        glBindTexture(type,texture);
    }

    /**
     * Generate a framebuffer texture
     * TODO allow different formats
     */
    public void genFramebufferTexture(int target) {
        glTexImage2D(type,0,GL_RGB,width,height,0,GL_RGB,GL_UNSIGNED_BYTE, NULL);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0 + target,type,texture,0);
        applyFilters();
    }

    /**
     * Generate a framebuffer depth texture
     */
    public void genFramebufferDepthTexture() {
        glTexImage2D(type,0,GL_DEPTH_COMPONENT32,width,height,0,GL_DEPTH_COMPONENT,GL_FLOAT, NULL);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,type,texture,0);
        applyFilters();
    }

    /**
     * Apply filters to the texture
     */
    public void applyFilters() {
        glTexParameterf(type,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glTexParameterf(type,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameterf(type,GL_TEXTURE_WRAP_S,GL_REPEAT);
        glTexParameterf(type,GL_TEXTURE_WRAP_T,GL_REPEAT);
    }

    /**
     * Bind the texture to the given position
     */
    public void bind(int position) {
        glActiveTexture(GL_TEXTURE0 + position);
        glBindTexture(type,texture);
    }


    /**
     * Removes the texture from memory
     */
    public void cleanUp() {
        glDeleteTextures(texture);
    }
}
