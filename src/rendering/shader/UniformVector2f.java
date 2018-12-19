package rendering.shader;

import org.joml.Vector2f;

import static org.lwjgl.opengl.GL30.*;

public class UniformVector2f extends Uniform{
    private Vector2f value;

    public UniformVector2f(Shader shader, String uniform, Vector2f value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(Vector2f value) {this.value = value;}
    public Vector2f getValue() {return value;}

     public void bindUniform() {
         shader.setUniformVector2f(location,value);
    }
}
