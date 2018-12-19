package rendering.shader;

import static org.lwjgl.opengl.GL30.*;

public class UniformFloat extends Uniform{
    private float value;

    public UniformFloat(Shader shader, String uniform, float value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(float value) {this.value = value;}
    public float getValue() {return value;}

     public void bindUniform() {
         shader.setUniformFloat(location,value);
    }
}
