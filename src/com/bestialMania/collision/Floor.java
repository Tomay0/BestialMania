package com.bestialMania.collision;

import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class Floor {
    public static final float MIN_Y = -1000;

    private int minX,minZ;
    private Set<Triangle>[][] floor;


    public Floor(BoundingBox mapBoundingBox) {
        minX = (int)mapBoundingBox.getX1();
        minZ = (int)mapBoundingBox.getZ1();
        int maxX = (int)Math.ceil(mapBoundingBox.getX2());
        int maxZ = (int)Math.ceil(mapBoundingBox.getZ2());
        int xRange = maxX-minX+1;
        int zRange = maxZ-minZ+1;

        floor = new Set[xRange][zRange];
    }

    /**
     * Add a list of floor triangles
     */
    public void addTriangles(Set<Triangle> triangles) {
        for(Triangle triangle : triangles) {
            BoundingBox bb = triangle.getBoundingBox();

            int minX = (int)bb.getX1() - this.minX;
            int maxX = (int)bb.getX2() - this.minX;
            int minZ = (int)Math.ceil(bb.getZ1()) - this.minZ;
            int maxZ = (int)Math.ceil(bb.getZ2()) - this.minZ;

            for(int x = minX;x<=maxX;x++) {
                for(int z = minZ;z<=maxZ;z++) {
                    if(x<0 || z<0 || x>=floor.length || z>=floor[0].length) continue;//out of bounds
                    if(floor[x][z]==null) {
                        floor[x][z] = new HashSet<>();
                    }
                    floor[x][z].add(triangle);
                }
            }
        }
    }

    /**
     * Get all floor triangles in the chunk given by the vertex
     */
    public Set<Triangle> trianglesAtPoint(Vector3f point) {
        int x = Math.round(point.x) - minX;
        int z = Math.round(point.z) - minZ;
        HashSet<Triangle> triangles = new HashSet<>();
        if(x<0 || z<0 || x>=floor.length || z>=floor[0].length) return triangles;
        if(floor[x][z]==null) return triangles;
        triangles.addAll(floor[x][z]);
        return triangles;

    }



    /**
     * Get the floor's height at the given location
     */
    public float getHeightAtLocation(Vector3f position) {
        float maxY = MIN_Y;
        for(Triangle triangle : trianglesAtPoint(position)) {
            float y = triangle.getTriangleY(position,-2,2);
            if(y>maxY) maxY = y;
        }
        return maxY;
    }

}
