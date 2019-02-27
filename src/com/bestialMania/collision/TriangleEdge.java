package com.bestialMania.collision;

import org.joml.Vector3f;

public class TriangleEdge {
    float x1,y1,z1,x2,y2,z2;//points on the line
    float ox,oy,oz;//opposite point

    float xm, xc, zm, zc;//line equations, values for x=(xm)y+(xc) and z=(zm)y+(zc) No equations if y1==y2


    float projx,projy,projz,projLength;//orthogonal vector that points from the opposite vector to the line

    /**
     * Edge of a triangle used for calculations in wall collisions
     */
    public TriangleEdge(Vector3f v1, Vector3f v2, Vector3f opposite) {
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
        ox = opposite.x;
        oy = opposite.y;
        oz = opposite.z;


        float dx = x2-x1;
        float dy = y2-y1;
        float dz = z2-z1;
        if(dy!=0) {
            xm = dx/dy;
            zm = dz/dy;

            xc = x1 - xm*y1;
            zc = z1 - zm*y1;
        }

        //calculate projection of opposite onto v1,v2
        float px = ox-x1;
        float py = oy-y1;
        float pz = oz-z1;
        //System.out.println("edge");
        //System.out.println(ox + "," + oy + "," + oz);
        //System.out.println(dx + "," + dy + "," + dz);


        float dot = px*dx + py*dy + pz*dz;
        float dsquare = dx*dx + dy*dy + dz*dz;
        if(dsquare == 0) {
            projx = -px;
            projy = -py;
            projz = -pz;
            projLength = (float)Math.sqrt(projx*projx+projy*projy+projz*projz);
        }else {
            float s = dot/dsquare;
            float sx = x1 + dx*s;
            float sy = y1 + dy*s;
            float sz = z1 + dz*s;
            //System.out.println(sx + "," + sy + "," + sz);

            projx = sx - ox;
            projy = sy - oy;
            projz = sz - oz;
            projLength = (float)Math.sqrt(projx*projx+projy*projy+projz*projz);
           // System.out.println(projx + "," + projy + "," + projz);
        }

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

    /**
     * Calculate if the point v is inside the edge of the triangle
     */
    public boolean isInsideEdge(Vector3f v) {
        if(projLength==0) return false;
        //vector o to v
        float ovx = v.x-ox;
        float ovy = v.y-oy;
        float ovz = v.z-oz;

        float dot = ovx*projx + ovy*projy + ovz*projz;
        float projSquared = projLength*projLength;

        float s = dot/projSquared;
        return s>=0 && s<=1;

    }
}
