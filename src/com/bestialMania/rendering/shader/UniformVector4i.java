package com.bestialMania.rendering.shader;

import org.joml.Vector4i;

import static org.lwjgl.opengl.GL30.glUniform4i;

public class UniformVector4i extends Uniform{
    private Vector4i value;

    public UniformVector4i(Shader shader, String uniform, Vector4i value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(Vector4i value) {this.value = value;}
    public Vector4i getValue() {return value;}

     public void bindUniform() {
         shader.setUniformVector4i(location,value);
    }
}
