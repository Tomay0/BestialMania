package rendering.shader;

import org.joml.Vector3f;
import static org.lwjgl.opengl.GL30.*;

public class UniformVector3f extends Uniform{
    private Vector3f value;

    public UniformVector3f(Shader shader, String uniform, Vector3f value) {
        super(shader.getProgram(), uniform);
        this.value = value;
    }

    public void setValue(Vector3f value) {this.value = value;}
    public Vector3f getValue() {return value;}

     public void bindUniform() {
         glUniform3f(location,value.x,value.y,value.z);
    }
}
