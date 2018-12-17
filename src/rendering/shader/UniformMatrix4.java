package rendering.shader;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class UniformMatrix4 extends Uniform{
    private Matrix4f matrix;

    public UniformMatrix4(Shader shader, String uniform, Matrix4f matrix) {
        super(shader.getProgram(), uniform);
        this.matrix = matrix;
    }

    public void setValue(Matrix4f value) {matrix = value;}
    public Matrix4f getValue() {return matrix;}

     public void bindUniform() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        glUniformMatrix4fv(location,false, buffer);
    }
}
