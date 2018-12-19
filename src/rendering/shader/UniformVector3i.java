package rendering.shader;

import org.joml.Vector3i;

import static org.lwjgl.opengl.GL30.glUniform3i;

public class UniformVector3i extends Uniform{
    private Vector3i value;

    public UniformVector3i(Shader shader, String uniform, Vector3i value) {
        super(shader, uniform);
        this.value = value;
    }

    public void setValue(Vector3i value) {this.value = value;}
    public Vector3i getValue() {return value;}

     public void bindUniform() {
         shader.setUniformVector3i(location,value);
    }
}
