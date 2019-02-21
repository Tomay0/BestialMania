package com.bestialMania.collision;

import org.joml.Vector3f;

public class BoundingCylinder {
    private Vector3f position;
    private float height;
    private float radius;
    private float radiusSquared;

    /**
     * Bounding cylinder
     * Used mostly by entities
     */
    public BoundingCylinder(Vector3f position, float height, float radius) {
        this.position = position;
        this.height = height;
        this.radius = radius;
        radiusSquared = radius*radius;
    }

    /**
     * Test if a point lies within the cylinder
     */
    public boolean intersectsPoint(Vector3f point) {
        //y collision
        if(point.y<position.y || point.y>position.y+height) return false;

        //test if x/z distance is less than radius
        float dx = point.x-position.x;
        float dz = point.z-position.z;
        float distanceSquared = dx*dx+dz*dz;

        return distanceSquared<radiusSquared;
    }
}
