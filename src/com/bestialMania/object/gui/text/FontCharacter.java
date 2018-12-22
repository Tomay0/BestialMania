package com.bestialMania.object.gui.text;

import org.joml.Vector2f;
import org.joml.Vector2f;

public class FontCharacter {
    private char id;
    private int xadvance;

    private Vector2f uvTopLeft,uvTopRight,uvBottomLeft,uvBottomRight;//locations of the corners in uv space
    private Vector2f posTopLeft,posTopRight,posBottomLeft,posBottomRight;//locations of the corners in position space (pixels)

    public FontCharacter(int textureWidth, char id, int x, int y, int width, int height, int xoff, int yoff, int xadvance) {
        this.id = id;
        this.xadvance = xadvance;

        //uv coordinates
        float fx1 = (float)x/(float)textureWidth;
        float fy1 = 1-(float)y/(float)textureWidth;
        float fx2 = fx1+(float)width/(float)textureWidth;
        float fy2 = fy1-(float)height/(float)textureWidth;

        uvTopLeft = new Vector2f(fx1,fy1);
        uvTopRight = new Vector2f(fx2,fy1);
        uvBottomLeft = new Vector2f(fx1,fy2);
        uvBottomRight = new Vector2f(fx2,fy2);

        //position coordinates based on cursor location
        int ix1 = xoff;
        int iy1 = yoff;
        int ix2 = xoff+width;
        int iy2 = yoff+height;

        posTopLeft = new Vector2f(ix1,iy1);
        posTopRight = new Vector2f(ix2,iy1);
        posBottomLeft = new Vector2f(ix1,iy2);
        posBottomRight = new Vector2f(ix2,iy2);
    }

    public char getChar() {
        return id;
    }

    public int getXAdvance() {
        return xadvance;
    }

    public Vector2f getUVTopLeft() {
        return uvTopLeft;
    }

    public Vector2f getUVTopRight() {
        return uvTopRight;
    }

    public Vector2f getUVBottomLeft() {
        return uvBottomLeft;
    }

    public Vector2f getUVBottomRight() {
        return uvBottomRight;
    }

    public Vector2f getPosTopLeft() {
        return posTopLeft;
    }

    public Vector2f getPosTopRight() {
        return posTopRight;
    }

    public Vector2f getPosBottomLeft() {
        return posBottomLeft;
    }

    public Vector2f getPosBottomRight() {
        return posBottomRight;
    }
}
