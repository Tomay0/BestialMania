package com.bestialMania.collision;

public class WallCollision implements Comparable{
    private float x,y;
    private Triangle triangle;
    private float order;//order collisions by a dot product so that the closest walls get processed first

    public WallCollision(Triangle triangle, float x, float y, float order) {
        this.x = x;
        this.y = y;
        this.triangle = triangle;
        this.order = order;
    }

    @Override
    public int compareTo(Object o) {
        if(o==null) return 0;
        if(!(o instanceof WallCollision)) return 0;

        WallCollision w = (WallCollision) o;

        if(order<w.order) return -1;
        else if(w.order<order) return 1;
        else return 0;

    }

    public float getX() {return x;}
    public float getY() {return y;}

    public Triangle getTriangle() {return triangle;}
}
