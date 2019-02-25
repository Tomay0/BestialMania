package com.bestialMania.collision;

import org.joml.Vector3f;

public class TriangleEdge {
    float x1,y1,z1,x2,y2,z2;


    float xm, xc, zm, zc;//line equations, values for x=(xm)y+(xc) and z=(zm)y+(zc) No equations if y1==y2

    /**
     * Edge of a triangle used for calculations in wall collisions
     */
    public TriangleEdge(Vector3f v1, Vector3f v2) {
        if(v1.y<v2.y) {
            x1 = v1.x;
            y1 = v1.y;
            z1 = v1.z;
            x2 = v2.x;
            y2 = v2.y;
            z2 = v2.z;
        }
        else {
            x1 = v2.x;
            y1 = v2.y;
            z1 = v2.z;
            x2 = v1.x;
            y2 = v1.y;
            z2 = v1.z;
        }

        if(y1==y2)return;

        float dx = x2-x1;
        float dz = z2-z1;
        float dy = y2-y1;

        xm = dx/dy;
        zm = dz/dy;

        xc = x1 - xm*y1;
        zc = z1 - zm*y1;
    }

    /**
     * Returns if the specified y is in range
     *
     */
    public boolean yInRange(float y) {
        return y > y1 && y < y2;
    }

    /**
     * Sets the intersection given a y value to the destination vector
     * If no intersection exists, return false.
     *
     */
    public boolean getIntersection(float y, Vector3f dest) {
        if(y1==y2) return false;
        if(!yInRange(y)) return false;

        dest.x = xm * y + xc;
        dest.y = y;
        dest.z = zm * y + zc;
        return true;
    }
}
