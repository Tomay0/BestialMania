package com.bestialMania.collision;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Triangle {
    private Vector3f v1,v2,v3;
    private float a,b,c,d;//ax +by+cz=d
    private boolean planar = true;
    private BoundingBox boundingBox;
    private TriangleEdge[] edges = new TriangleEdge[3];//edges
    private Vector3f[] intersects = new Vector3f[]{new Vector3f(),new Vector3f()};//for calculating intersections
    /*public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        v1 = new Vector3f(x1,y1,z1);
        v2 = new Vector3f(x2,y2,z2);
        v3 = new Vector3f(x3,y3,z3);
    }*/

    /**
     * Floor/Wall triangle of 3 vertices
     */
    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3) {
        //vertices
        this.v1 = new Vector3f(v1.x,v1.y,v1.z);
        this.v2 = new Vector3f(v2.x,v2.y,v2.z);
        this.v3 = new Vector3f(v3.x,v3.y,v3.z);
        //edges
        edges[0] = new TriangleEdge(v1,v2);
        edges[1] = new TriangleEdge(v1,v3);
        edges[2] = new TriangleEdge(v3,v2);
        //bounding box
        calcBoundingBox(v1.x,v1.y,v1.z,v2.x,v2.y,v2.z,v3.x,v3.y,v3.z);
        //planar equation
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
     * Calculate the planar equation ax + by + cz = d
     *
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
        d = a*v1.x + b*v1.y + c*v1.z;
    }

    /**
     * Gets the y value on the triangle of a point's x,z
     * If this is not on the triangle or not within the bounds, return Floor.MIN_Y
     * the y must lie within ymin and ymax from the point's y value to be considered close enough
     */
    public float getTriangleY(Vector3f point, float ymin, float ymax) {
        if(!planar) return CollisionHandler.MIN_Y;

        if(onTriangle(point)) {
            float y = getY(point);
            if(y>point.y+ymin && y<point.y+ymax) return y;

        }
        return CollisionHandler.MIN_Y;
    }

    /**
     * Calculate if a point is within the triangle from a top-down view
     */
    public boolean onTriangle(Vector3f point) {
        boolean b1 = sign(point, v1, v2) <= 0.0f;
        boolean b2 = sign(point, v2, v3) <= 0.0f;
        boolean b3 = sign(point, v3, v1) <= 0.0f;

        return ((b1 == b2) && (b2 == b3));
    }
    private float sign (Vector3f p1, Vector3f p2, Vector3f p3) {
        return (p1.x - p3.x) * (p2.z - p3.z) - (p2.x - p3.x) * (p1.z - p3.z);
    }

    /**
     * Get y on the triangle's plane using planar equation
     */
    public float getY(Vector3f point) {
        if(b==0 || !planar) return CollisionHandler.MIN_Y;

        return (a*point.x+c*point.z-d)/-b;
    }

    /**
     * calculate the intersection between p1 and p2 through the plane.
     * returns an interpolated value s, which is a linear interpolation between the 2 vectors where the intersection is.
     * return 2 if there is none
     */
    public float getLineIntersection(Vector3f p1,Vector3f p2) {
        float p = d - (a * p1.x + b * p1.y + c * p1.z);
        float q = a * (p2.x-p1.x) + b * (p2.y-p1.y) + c * (p2.z-p1.z);
        if(q==0) return 1;

        float s = p/q;
        if(s>1||s<0) return 1;

        return s;
    }

    /**
     * Testing purposes only
     */
    public String toString() {
        return "TRIANGLE: " + v1.x + "," + v1.y + "," + v1.z + " " + v2.x + "," + v2.y + "," + v2.z + " " + v3.x + "," + v3.y + "," + v3.z;
    }

    public String getEquation() {
        return a + "x + " + b + "y + " + c + "z = " + d;
    }

    /**
     * Works out if this triangle intersects the circle at the specified positon and radius
     * Aligned with the X/Z axis
     *
     * Returns null if no such intersection exists
     */
    public boolean getIntersectWithCircle(Vector3f position, float radius, Vector2f wallPushVector) {
        if(a==0 && c==0) return false;//wall is parallel
        if(position.y<=boundingBox.getY1() || position.y>=boundingBox.getY2()) return false;//out of y range
        int nIntersects = 0;
        for(int i = 0;i<3;i++) {
            TriangleEdge edge = edges[i];
            if(edge.getIntersection(position.y,intersects[nIntersects])) nIntersects++;

            if(nIntersects==2) break;
        }

        //there should be 2 intersections forming a line that the circle collides with at the specified y. Collision follows
        if(nIntersects<2) {
            System.err.println("Error with wall intersection calculation");
            return false;
        }
        //Get the closest point to the line from the position
        Vector3f ab = new Vector3f(intersects[1].x-intersects[0].x,position.y,intersects[1].z-intersects[0].z);
        Vector3f ap = new Vector3f(position.x-intersects[0].x,position.y,position.z-intersects[0].z);

        float APdotAB = ab.x*ap.x + ab.z*ap.z;
        float ABsquare = ab.x*ab.x + ab.z*ab.z;
        if(ABsquare==0) {
            System.err.println("Error with wall intersection calculation");
            return false;
        }

        float s = APdotAB/ABsquare;
        Vector3f closest = new Vector3f();
        if(s<0) closest = intersects[0];
        else if(s>1) closest = intersects[1];
        else {
            closest.x = intersects[0].x + s*ab.x;
            closest.z = intersects[0].z + s*ab.z;
            closest.y = intersects[0].y;
        }

        //check if the closest point is within the circle
        float dx = closest.x-position.x;
        float dz = closest.x-position.x;
        float dsquare = dx*dx+dz*dz;
        float rsquare = radius*radius;
        System.out.println("intersection found");
        System.out.println(position.x + "," + position.y + "," + position.z);
        System.out.println(closest.x + "," + closest.z);
        System.out.println(Math.sqrt(dsquare) + ">" + Math.sqrt(rsquare));
        if(dsquare > rsquare) return false;//the wall is outside the circle

        //calculate the vector that the wall should push you back
        if(s<0||s>1) {
            closest.x = intersects[0].x + s*ab.x;
            closest.z = intersects[0].z + s*ab.z;
            closest.y = intersects[0].y;
        }

        wallPushVector.x = position.x-closest.x;
        wallPushVector.y = position.z-closest.z;
        float len = wallPushVector.length();
        if(len==0) {
            System.err.println("inside wall");
            //somehow your character is in the exact middle of the wall, I won't make any collisions occur in this scenario
            return false;
        }
        float scale = (radius-len)/len;
        wallPushVector.mul(scale);

        /*System.out.println(this);
        System.out.println(getEquation());
        System.out.println(intersects[0].x + "," + intersects[0].z);
        System.out.println(intersects[1].x + "," + intersects[1].z);
        System.out.println(position.x + "," + position.y + "," + position.z);
        System.out.println("-");
        System.out.println("AB:" + ab.x + "," + ab.z);
        System.out.println("AP:" + ap.x + "," + ap.z);
        System.out.println("-");
        System.out.println(radius-ap.x);
        System.out.println(wallPushVector.x + "," + wallPushVector.y);*/
        return true;
        /*DONT REMOVE THIS PLEASE UNTIL I KNOW IT WORKS
        //line in the form x = h
        if(c==0) {
            float h = (d-b*position.y)/a;

        }
        //line in the form z = mx + h
        else {
            float m = -a/c;
            float h = (d-b*position.y)/c;


        }
        */
    }
}
