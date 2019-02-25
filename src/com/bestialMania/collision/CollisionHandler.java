package com.bestialMania.collision;

import com.bestialMania.object.beast.Beast;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class CollisionHandler {

    public static final float MIN_Y = -1000;

    private int minX,minZ;
    private Chunk[][] chunks;
    private Set<Triangle> tempTriangleSet = new HashSet<>();

    public CollisionHandler(BoundingBox mapBoundingBox) {
        minX = (int)Math.floor(mapBoundingBox.getX1());
        minZ = (int)Math.floor(mapBoundingBox.getZ1());
        int maxX = (int)Math.ceil(mapBoundingBox.getX2());
        int maxZ = (int)Math.ceil(mapBoundingBox.getZ2());
        int xRange = maxX-minX+1;
        int zRange = maxZ-minZ+1;

        chunks = new Chunk[xRange][zRange];
    }

    /**
     * Add a list of floor triangles
     */
    public void addFloor(Set<Triangle> triangles) {
        for(Triangle triangle : triangles) {
            BoundingBox bb = triangle.getBoundingBox();

            int minX = (int)Math.floor(bb.getX1()) - this.minX;
            int maxX = (int)Math.ceil(bb.getX2()) - this.minX;
            int minZ = (int)Math.floor(bb.getZ1()) - this.minZ;
            int maxZ = (int)Math.ceil(bb.getZ2()) - this.minZ;

            for(int x = minX;x<=maxX;x++) {
                for(int z = minZ;z<=maxZ;z++) {
                    if(x<0 || z<0 || x>=chunks.length || z>=chunks[0].length) continue;//out of bounds
                    if(chunks[x][z]==null) {
                        chunks[x][z] = new Chunk();
                    }
                    chunks[x][z].addFloor(triangle);
                }
            }
        }
    }

    /**
     * Add a list of wall triangles
     */
    public void addWalls(Set<Triangle> triangles) {
        for(Triangle triangle : triangles) {
            BoundingBox bb = triangle.getBoundingBox();

            int minX = (int)Math.floor(bb.getX1()) - this.minX;
            int maxX = (int)Math.ceil(bb.getX2()) - this.minX;
            int minZ = (int)Math.floor(bb.getZ1()) - this.minZ;
            int maxZ = (int)Math.ceil(bb.getZ2()) - this.minZ;

            for(int x = minX;x<=maxX;x++) {
                for(int z = minZ;z<=maxZ;z++) {
                    if(x<0 || z<0 || x>=chunks.length || z>=chunks[0].length) continue;//out of bounds
                    if(chunks[x][z]==null) {
                        chunks[x][z] = new Chunk();
                    }
                    chunks[x][z].addWall(triangle);
                }
            }
        }
    }

    /**
     * Get all floor triangles in the chunk given by the vertex
     */
    private Set<Triangle> floorAtPoint(Vector3f point) {
        int x = (int)Math.floor(point.x) - minX;
        int z = (int)Math.floor(point.z) - minZ;
        tempTriangleSet.clear();
        if(x<0 || z<0 || x>=chunks.length || z>=chunks[0].length) return tempTriangleSet;
        if(chunks[x][z]==null) return tempTriangleSet;
        tempTriangleSet.addAll(chunks[x][z].getFloor());
        return tempTriangleSet;

    }

    /**
     * Get all floor triangles in the chunk given by the vertex
     */
    private Set<Triangle> wallsAtArea(int minX, int maxX, int minZ, int maxZ) {
        tempTriangleSet.clear();
        for(int x = minX;x<=maxX;x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if(x<0 || z<0 || x>=chunks.length || z>=chunks[0].length) continue;
                if(chunks[x][z]==null) continue;
                tempTriangleSet.addAll(chunks[x][z].getWalls());
            }
        }

        return tempTriangleSet;

    }



    /**
     * Get the floor's height at the given location
     */
    public float getHeightAtLocation(Vector3f position) {
        float maxY = MIN_Y;
        for(Triangle triangle : floorAtPoint(position)) {
            float y = triangle.getTriangleY(position, -Beast.DOWNHILL_CLIMB_HEIGHT,Beast.UPHILL_CLIMB_HEIGHT);
            if(y>maxY) maxY = y;
        }
        return maxY;
    }

    /**
     * Duplicate of the code above but with printing
     */
    public float printHeightAtLocation(Vector3f position) {
        System.out.println(position.x + "," + position.y + "," + position.z);
        float maxY = MIN_Y;
        for(Triangle triangle : floorAtPoint(position)) {
            float y = triangle.getTriangleY(position,-Beast.DOWNHILL_CLIMB_HEIGHT,Beast.UPHILL_CLIMB_HEIGHT);
            System.out.println(triangle);
            System.out.println(triangle.onTriangle(position));
            System.out.println(triangle.getEquation());
            System.out.println(triangle.getY(position));
            System.out.println(y + "\n");
            if(y>maxY) maxY = y;
        }
        System.out.println(maxY + "\n");
        return maxY;
    }

    /**
     * Work out the intersection point between a player and the closest wall
     * Return null if there is no intersection
     */
    public float getWallIntersection(Vector3f p1, Vector3f p2) {
        int minX,maxX,minZ,maxZ;
        minX = (int)Math.floor(Math.min(p1.x,p2.x)) - this.minX;
        maxX = (int)Math.floor(Math.max(p1.x,p2.x)) - this.minX;
        minZ = (int)Math.floor(Math.min(p1.z,p2.z)) - this.minZ;
        maxZ = (int)Math.floor(Math.max(p1.z,p2.z)) - this.minZ;

        for(Triangle triangle : wallsAtArea(minX,maxX,minZ,maxZ)) {
            float intersection = triangle.getLineIntersection(p1,p2);
            if(intersection!=1) return intersection;
        }

        return 1;
    }

    /**
     * Works out the wall push vector, which pushes the beast away from the wall
     * if there is no intersection with walls, return false
     */
    public boolean getWallIntersection(Vector3f position, float radius, Vector2f wallPushVector) {
        int minX,maxX,minZ,maxZ;
        minX = (int)Math.floor(position.x-radius) - this.minX;
        maxX = (int)Math.floor(position.x+radius) - this.minX;
        minZ = (int)Math.floor(position.x-radius) - this.minZ;
        maxZ = (int)Math.floor(position.x+radius) - this.minZ;
        for(Triangle triangle : wallsAtArea(minX,maxX,minZ,maxZ)) {
            if(triangle.getIntersectWithCircle(position,radius,wallPushVector)) return true;
        }

        return false;
    }
}
