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
        edges[0] = new TriangleEdge(v1,v2,v3);
        edges[1] = new TriangleEdge(v1,v3,v2);
        edges[2] = new TriangleEdge(v3,v2,v1);
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
     * return 1 if there is no intersection
     */
    public float getLineIntersection(Vector3f p1,Vector3f p2) {
        float q = a * (p2.x-p1.x) + b * (p2.y-p1.y) + c * (p2.z-p1.z);
        if(q==0) return 1;
        float p = d - (a * p1.x + b * p1.y + c * p1.z);

        float s = p/q;
        if(s>1||s<0) return 1;
        intersects[0].x = p1.x + s*(p2.x-p1.x);
        intersects[0].y = p1.y + s*(p2.y-p1.y);
        intersects[0].z = p1.z + s*(p2.z-p1.z);
        //System.out.println(this);
        //System.out.println(intersects[0].x + "," + intersects[0].y + "," + intersects[0].z);

        //check if the point is inside the triangle
        for(int i = 0;i<3;i++) {
            //System.out.println("line " + i);
            if(!edges[i].isInsideEdge(intersects[0])) return 1;
        }

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
     * Returns a vector which pushes the circle at least "radius" away from the wall.
     *
     * Returns null if the circle does not collide with the wall.
     */
    public WallCollision getWallCollision(Vector3f position, float radius) {
        if(a==0 && c==0) return null;//wall is parallel
        if(position.y<=boundingBox.getY1() || position.y>=boundingBox.getY2()) return null;//out of y range
        int nIntersects = 0;
        for(int i = 0;i<3;i++) {
            TriangleEdge edge = edges[i];
            if(edge.getIntersection(position.y,intersects[nIntersects])) nIntersects++;

            if(nIntersects==2) break;
        }

        //there should be 2 intersections forming a line that the circle collides with at the specified y. Collision follows
        if(nIntersects<2) {
            System.err.println("Error with wall intersection calculation");
            return null;
        }
        //Get the closest point to the line from the position
        float abx = intersects[1].x-intersects[0].x;
        float abz = intersects[1].z-intersects[0].z;
        float apx = position.x-intersects[0].x;
        float apz = position.z-intersects[0].z;

        float APdotAB = abx*apx + abz*apz;
        float ABsquare = abx*abx + abz*abz;
        if(ABsquare==0) {
            System.err.println("Error with wall intersection calculation");
            return null;
        }

        float s = APdotAB/ABsquare;
        float cx,cz;
        if(s<0) {
            cx = intersects[0].x;
            cz = intersects[0].z;
        }
        else if(s>1) {
            cx = intersects[1].x;
            cz = intersects[1].z;
        }
        else {
            cx = intersects[0].x + s*abx;
            cz = intersects[0].z + s*abz;
        }

        //check if the closest point is within the circle
        float dx = cx-position.x;
        float dz = cz-position.z;
        if(dx*dx+dz*dz > radius*radius - 0.001f) return null;//the wall is outside the circle

        //calculate the vector that the wall should push you back
        if(s<0||s>1) {
            cx = intersects[0].x + s*abx;
            cz = intersects[0].z + s*abz;
        }
        float wx = position.x-cx;
        float wy = position.z-cz;
        float len = (float)Math.sqrt(wx*wx+wy*wy);
        if(len==0) {
            System.err.println("inside wall");
            //somehow your character is in the exact middle of the wall, I won't make any collisions occur in this scenario
            return null;
        }
        float scale = (radius-len)/len;
        wx*=scale;
        wy*=scale;
        float order = Math.abs(0.5f-s);

        return new WallCollision(this,wx,wy,order);
    }
}
