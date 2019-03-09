package com.bestialMania.rendering.shader;

public class UniformInt extends Uniform{
    private int value;

    public UniformInt(Shader shader, String uniform, int value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(int value) {this.value = value;}
    public int getValue() {return value;}

     public void bindUniform() {
         shader.setUniformInt(location,value);
    }
}
