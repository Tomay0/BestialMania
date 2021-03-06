package com.bestialMania.collision;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Chunk {
    private Set<Triangle> floor = new HashSet<>();
    private Set<Triangle> walls = new HashSet<>();
    private Set<Triangle> ceilings = new HashSet<>();

    public void addFloor(Triangle triangle) {
        floor.add(triangle);
    }

    public void addWall(Triangle triangle) {
        walls.add(triangle);
    }
    public void addCeiling(Triangle triangle) {
        ceilings.add(triangle);
    }

    public Set<Triangle> getFloor() {
        return Collections.unmodifiableSet(floor);
    }

    public Set<Triangle> getWalls() {
        return Collections.unmodifiableSet(walls);
    }
    public Set<Triangle> getCeilings() {
        return Collections.unmodifiableSet(ceilings);
    }
}
