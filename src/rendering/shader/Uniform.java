package rendering.shader;

import static org.lwjgl.opengl.GL30.*;

/**
 * Abstract class of a uniform for a shader that can be stored in collections for easier tracking of uniforms
 */
public abstract class Uniform {
    protected int location;//integer location of the uniform
    protected String uniform;//the uniform as a string

    /**
     * Constructs a uniform variable from the shader program ID and the uniform string
     */
    public Uniform(int shader, String uniform) {
        location = glGetUniformLocation(shader,uniform);
        this.uniform = uniform;
    }

    /**
     * Binds a value to the specified uniform
     */
    public abstract void bindUniform();
}
