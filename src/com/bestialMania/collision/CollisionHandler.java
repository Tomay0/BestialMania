package com.bestialMania.collision;

import com.bestialMania.object.beast.Beast;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

public class CollisionHandler {

    public static final float MIN_Y = -1000;
    public static final float MAX_Y = 1000;

    private int minX,minZ;
    private Chunk[][] chunks;
    private Set<Triangle> tempTriangleSet = new HashSet<>();

    /**
     * Represents all walls and floors within an environment that can be collided with
     */
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
     * Add a list of ceiling triangles
     */
    public void addCeiling(Set<Triangle> triangles) {
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
                    chunks[x][z].addCeiling(triangle);
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
    private Set<Triangle> ceilingAtPoint(Vector3f point) {
        int x = (int)Math.floor(point.x) - minX;
        int z = (int)Math.floor(point.z) - minZ;
        tempTriangleSet.clear();
        if(x<0 || z<0 || x>=chunks.length || z>=chunks[0].length) return tempTriangleSet;
        if(chunks[x][z]==null) return tempTriangleSet;
        tempTriangleSet.addAll(chunks[x][z].getCeilings());
        return tempTriangleSet;

    }

    /**
     * Get all wall triangles in the chunks by the given area
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
     * Get all wall, ceiling and floor triangles in the chunks by the given area
     */
    private Set<Triangle> trianglesAtArea(int minX, int maxX, int minZ, int maxZ) {
        tempTriangleSet.clear();
        for(int x = minX;x<=maxX;x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if(x<0 || z<0 || x>=chunks.length || z>=chunks[0].length) continue;
                if(chunks[x][z]==null) continue;
                tempTriangleSet.addAll(chunks[x][z].getWalls());
                tempTriangleSet.addAll(chunks[x][z].getFloor());
                tempTriangleSet.addAll(chunks[x][z].getCeilings());
            }
        }

        return tempTriangleSet;

    }



    /**
     * Get the floor's height at the given location
     */
    public float getFloorHeightAtLocation(Vector3f position) {
        float maxY = MIN_Y;
        for(Triangle triangle : floorAtPoint(position)) {
            float y = triangle.getTriangleY(position, -1.0f,Beast.UPHILL_CLIMB_HEIGHT, MIN_Y);
            if(y>maxY) maxY = y;
        }
        return maxY;
    }
    /**
     * Get the ceilings's height at the given location
     */
    public float getCeilingHeightAtLocation(Vector3f position) {
        float minY = MAX_Y;
        for(Triangle triangle : ceilingAtPoint(position)) {
            float y = triangle.getTriangleY(position, -1.0f,Beast.UPHILL_CLIMB_HEIGHT, MAX_Y);
            if(y<minY) minY = y;
        }
        return minY;
    }

    /**
     * Returns an interpolation value of the closest triangle intersection to p1 of the line p1->p2
     * 0 = at p1, 1 = at p2 or no intersection
     */
    public float getTriangleIntersection(Vector3f p1, Vector3f p2) {
        int minX,maxX,minZ,maxZ;
        minX = (int)Math.floor(Math.min(p1.x,p2.x)) - this.minX;
        maxX = (int)Math.floor(Math.max(p1.x,p2.x)) - this.minX;
        minZ = (int)Math.floor(Math.min(p1.z,p2.z)) - this.minZ;
        maxZ = (int)Math.floor(Math.max(p1.z,p2.z)) - this.minZ;

        //find the clo
        float minValue = 1;

        for(Triangle triangle : trianglesAtArea(minX,maxX,minZ,maxZ)) {
            float intersection = triangle.getLineIntersection(p1,p2);
            if(intersection<minValue) minValue = intersection;
        }

        return minValue;
    }

    /**
     * Works out the wall push vector, which pushes the beast away from the wall
     * if there is no intersection with walls, return false
     *
     * //If nTests is 1, you will only test a flat disc at y = position.y, if you want to test at different y values you can do more tests, with a y increment for each test.
     */
    public boolean calculateWallPush(Vector3f position, float radius, Vector2f wallPushVector/*, int nTests, float yincrement*/) {
        int minX,maxX,minZ,maxZ;
        minX = (int)Math.floor(position.x-radius) - this.minX;
        maxX = (int)Math.floor(position.x+radius) - this.minX;
        minZ = (int)Math.floor(position.z-radius) - this.minZ;
        maxZ = (int)Math.floor(position.z+radius) - this.minZ;
        wallPushVector.x = 0;
        wallPushVector.y = 0;

        //for(int i = 0;i<nTests;i++) {
            List<WallCollision> wallCollisions = new ArrayList<>();

            Set<Triangle> triangleSet = wallsAtArea(minX,maxX,minZ,maxZ);

            for(Triangle triangle : triangleSet) {
                WallCollision wallCollision = triangle.getWallCollision(position,radius);

                if(wallCollision!=null) {
                    wallCollisions.add(wallCollision);
                }
            }

            //do all wall collisions in the right order
            while(wallCollisions.size()>0) {

                Collections.sort(wallCollisions);

                //update wall push vector
                WallCollision w = wallCollisions.get(0);
                wallPushVector.x+=w.getX();
                wallPushVector.y+=w.getY();
                position.x+=w.getX();
                position.z+=w.getY();

                //
                wallCollisions.remove(w);
                triangleSet.clear();
                for(WallCollision wallCollision : wallCollisions) triangleSet.add(wallCollision.getTriangle());
                wallCollisions.clear();

                for(Triangle triangle : triangleSet) {
                    WallCollision wallCollision2 = triangle.getWallCollision(position,radius);
                    if(wallCollision2!=null) {
                        wallCollisions.add(wallCollision2);
                    }
                }
            }
            //position.y += yincrement;
        //}




        if(wallPushVector.x==0&&wallPushVector.y==0) return false;
        return true;
    }
}
