package com.bestialMania.collision;

import org.joml.Vector3f;

public class Triangle {
    private Vector3f v1,v2,v3;
    private float a,b,c,d;//ax +by+cz=d
    private boolean planar = true;
    private BoundingBox boundingBox;
    /*public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        v1 = new Vector3f(x1,y1,z1);
        v2 = new Vector3f(x2,y2,z2);
        v3 = new Vector3f(x3,y3,z3);


    }*/
    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3) {
        this.v1 = new Vector3f(v1.x,v1.y,v1.z);
        this.v2 = new Vector3f(v2.x,v2.y,v2.z);
        this.v3 = new Vector3f(v3.x,v3.y,v3.z);
        calcBoundingBox(v1.x,v1.y,v1.z,v2.x,v2.y,v2.z,v3.x,v3.y,v3.z);
        calcPlanarEquation();
    }

    /**
     * Calculate the bounding box for the triangle
     */
    public void calcBoundingBox(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        float minX = Math.min(x1,Math.min(x2,x3));
        float minY = Math.min(y1,Math.min(y2,y3));
        float minZ = Math.min(z1,Math.min(z2,z3));
        float maxX = Math.max(x1,Math.max(x2,x3));
        float maxY = Math.max(y1,Math.max(y2,y3));
        float maxZ = Math.max(z1,Math.max(z2,z3));
        boundingBox = new BoundingBox(minX,minY,minZ,maxX,maxY,maxZ);
    }
    public BoundingBox getBoundingBox() {return boundingBox;}

    /**
     * Calculate the planar equation
     */
    public void calcPlanarEquation() {
        Vector3f v1_2 = new Vector3f(), v1_3 = new Vector3f(), cross = new Vector3f();
        v2.sub(v1,v1_2);
        v3.sub(v1,v1_3);
        v1_2.cross(v1_3,cross);
        a = cross.x;
        b = cross.y;
        c = cross.z;
        if(a==0&&b==0&&c==0) planar = false;
        d = a*v1.x + b*v2.y + c*v3.z;
    }

    /**
     * Gets the y value on the triangle of a point's x,z
     * If this is not on the triangle or not within the bounds, return the minimum float value
     * ymin is maximum distance below it must be
     * ymax is the maximum distance above it must be
     */
    public float getTriangleY(Vector3f point, float ymin, float ymax) {
        if(!planar) return Floor.MIN_Y;

        if(onTriangle(point)) {
            float y = getY(point);
            if(y>point.y+ymin && y<point.y+ymax) return y;

        }
        return Floor.MIN_Y;
    }


    private boolean onTriangle(Vector3f point) {
        boolean b1 = sign(point, v1, v2) <= 0.0f;
        boolean b2 = sign(point, v2, v3) <= 0.0f;
        boolean b3 = sign(point, v3, v1) <= 0.0f;

        return ((b1 == b2) && (b2 == b3));
    }
    private float sign (Vector3f p1, Vector3f p2, Vector3f p3) {
        return (p1.x - p3.x) * (p2.z - p3.z) - (p2.x - p3.x) * (p1.z - p3.z);
    }
    private float getY(Vector3f point) {
        if(b==0) return Floor.MIN_Y;

        return (a*point.x+c*point.z)/-b;
    }


    public String toString() {
        return v1.x + "," + v1.y + "," + v1.z + " " + v2.x + "," + v2.y + "," + v2.z + " " + v3.x + "," + v3.y + "," + v3.z;
    }
}
