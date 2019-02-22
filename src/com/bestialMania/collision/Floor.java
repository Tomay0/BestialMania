package com.bestialMania.collision;

import com.bestialMania.object.beast.Beast;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class Floor {
    public static final float MIN_Y = -1000;

    private int minX,minZ;
    private Set<Triangle>[][] floor;


    public Floor(BoundingBox mapBoundingBox) {
        minX = (int)Math.floor(mapBoundingBox.getX1());
        minZ = (int)Math.floor(mapBoundingBox.getZ1());
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

            int minX = (int)Math.floor(bb.getX1()) - this.minX;
            int maxX = (int)Math.ceil(bb.getX2()) - this.minX;
            int minZ = (int)Math.floor(bb.getZ1()) - this.minZ;
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
        int x = (int)Math.floor(point.x) - minX;
        int z = (int)Math.floor(point.z) - minZ;
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
        for(Triangle triangle : trianglesAtPoint(position)) {
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
}
