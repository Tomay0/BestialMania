package rendering.shader;

import org.joml.Matrix4f;

public class UniformMatrix4 extends Uniform{
    private Matrix4f value;

    public UniformMatrix4(Shader shader, String uniform, Matrix4f value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(Matrix4f value) {value = value;}
    public Matrix4f getValue() {return value;}

     public void bindUniform() {
        shader.setUniformMatrix4(location,value);
    }
}
