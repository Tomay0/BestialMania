package com.bestialMania.collision;

import org.joml.Vector3f;

public class BoundingBox {
    //x1,y1,z1 is the lower coordinate
    private float x1,y1,z1,x2,y2,z2;

    /**
     * Bounding box
     * */
    public BoundingBox(float x1, float y1, float z1, float x2, float y2, float z2) {
        if(x1>x2) {
            this.x1 = x2;
            this.x2 = x1;
        }
        else {
            this.x1 = x1;
            this.x2 = x2;
        }

        if(y1>y2) {
            this.y1 = y2;
            this.y2 = y1;
        }
        else {
            this.y1 = y1;
            this.y2 = y2;
        }

        if(z1>z2) {
            this.z1 = z2;
            this.z2 = z1;
        }
        else {
            this.z1 = z1;
            this.z2 = z2;
        }
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getZ1() {
        return z1;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
    }

    public float getZ2() {
        return z2;
    }

    /**
     * Returns if a point is contained within the bounding box
     */
    public boolean containsPoint(Vector3f point) {
        return (point.x >= x1 && point.x<= x2 && point.y >= y1 && point.y <=y2 && point.z>=z1 && point.z<=z2);
    }
}
