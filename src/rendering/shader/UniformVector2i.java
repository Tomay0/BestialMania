package rendering.shader;

import org.joml.Vector2i;

import static org.lwjgl.opengl.GL30.*;

public class UniformVector2i extends Uniform{
    private Vector2i value;

    public UniformVector2i(Shader shader, String uniform, Vector2i value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(Vector2i value) {this.value = value;}
    public Vector2i getValue() {return value;}

     public void bindUniform() {
         shader.setUniformVector2i(location,value);
    }
}
