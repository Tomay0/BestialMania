package rendering.shader;

import static org.lwjgl.opengl.GL30.*;

public class UniformInt extends Uniform{
    private int value;

    public UniformInt(Shader shader, String uniform, int value) {
        super(shader.getProgram(), uniform);
        this.value = value;
    }

    public void setValue(int value) {this.value = value;}
    public int getValue() {return value;}

     public void bindUniform() {
         glUniform1i(location,value);
    }
}
