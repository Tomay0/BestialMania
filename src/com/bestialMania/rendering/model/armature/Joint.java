package com.bestialMania.rendering.model.armature;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Joint implements Iterable<Joint> {
    private String name;
    private int id;
    private List<Joint> children = new ArrayList<>();
    private Matrix4f matrix;

    public Joint(String name, int id) {
        this.name = name;
        this.id = id;
    }


    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }

    public void addChild(Joint joint) {
        children.add(joint);
    }
    @Override
    public Iterator<Joint> iterator() {return children.iterator();}

    public void setMatrix(Matrix4f matrix) {
        this.matrix = matrix;
    }
}
