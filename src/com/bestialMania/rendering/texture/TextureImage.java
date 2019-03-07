package com.bestialMania.rendering.texture;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
public class TextureImage {
    private int width,height;
    private ByteBuffer byteBuffer;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public TextureImage(int width, int height, ByteBuffer byteBuffer) {
        this.width = width;
        this.height = height;
        this.byteBuffer = byteBuffer;
    }

    /**
     * Load a PNG/JPG/etc using java's buffered image
     */
    public static TextureImage loadImageJava(String fileName, boolean flip) throws IOException {
        BufferedImage image = ImageIO.read(new File(fileName));

        int width = image.getWidth();
        int height = image.getHeight();

        //convert to pixel array
        int[] pixels = new int[width*height*4];
        pixels = image.getRGB(0,0,width,height,null,0,width);

        //convert to byte buffer
        ByteBuffer buffer = BufferUtils.createByteBuffer(width*height*4);

        for(int yi = height-1;yi>=0;yi--) {
            int y = yi;
            if(!flip) y = height-1-y;
            for(int x = 0;x<width;x++) {
                int px = pixels[y*width+x];
                buffer.put((byte) ((px >> 16) & 0xFF));//R
                buffer.put((byte) ((px >> 8) & 0xFF));//G
                buffer.put((byte) (px & 0xFF));//B
                buffer.put((byte) ((px >> 24) & 0xFF));//A
            }
        }

        buffer.flip();

        TextureImage textureImage = new TextureImage(width,height,buffer);
        return textureImage;
    }

    public static TextureImage loadImage(String fileName) {
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //get the header
            char[] chars = new char[3];
            for (int i = 0; i < 3; i++) chars[i] = ois.readChar();
            if (chars[0] != 'B' || chars[1] != 'M' || chars[2] != 'T') {
                System.err.println("Invalid format for the texture " + fileName + " must be bmt!");
                return null;
            }
            //get dimensions
            int width = ois.readInt();
            int height = ois.readInt();

            //get the byte buffer
            int bufferSize=width*height*4;
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(bufferSize);
            for(int i = 0;i<bufferSize;i++) {
                byteBuffer.put(ois.readByte());
            }
            byteBuffer.flip();

            ois.close();
            inputStream.close();

            return new TextureImage(width,height,byteBuffer);
        }catch(
        FileNotFoundException e) {
            System.err.println("Could not find the file for the model: " + fileName);
            return null;
        }catch(EOFException  e) {
            System.err.println("Model file " + fileName + " appears to be incomplete, end of file reached.");
            e.printStackTrace();
            return null;
        }catch(IOException e) {
            System.err.println("Unable to load model: " + fileName);
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Save a texture image as a BMT format
     */
    public void saveAsBMT(String fileName) {
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);

            oos.writeChars("BMT");
            oos.writeInt(width);
            oos.writeInt(height);
            byteBuffer.rewind();
            while(byteBuffer.hasRemaining()) {
                oos.writeByte(byteBuffer.get());
            }


            oos.close();
            outputStream.close();

        }catch(Exception e) {
            System.err.println("Unable to save texture: " + fileName);
            e.printStackTrace();
        }


    }

}
