package rendering.shader;

import org.joml.Vector4f;

import static org.lwjgl.opengl.GL30.*;

public class UniformVector4f extends Uniform{
    private Vector4f value;

    public UniformVector4f(Shader shader, String uniform, Vector4f value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(Vector4f value) {this.value = value;}
    public Vector4f getValue() {return value;}

     public void bindUniform() {
         shader.setUniformVector4f(location,value);
    }
}
