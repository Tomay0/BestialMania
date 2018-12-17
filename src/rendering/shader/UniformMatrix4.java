package rendering.shader;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class UniformMatrix4 extends Uniform{
    private Matrix4f value;

    public UniformMatrix4(Shader shader, String uniform, Matrix4f value) {
        super(shader.getProgram(), uniform);
        this.value = value;
    }

    public void setValue(Matrix4f value) {value = value;}
    public Matrix4f getValue() {return value;}

     public void bindUniform() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        glUniformMatrix4fv(location,false, buffer);
    }
}
