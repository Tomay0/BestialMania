package rendering.shader;

import rendering.Texture;

import static org.lwjgl.opengl.GL30.glUniform1i;

public class UniformTexture extends Uniform{
    private int slot;
    private Texture texture;

    public UniformTexture(Shader shader, String uniform, int slot, Texture texture) {
        super(shader.getProgram(), uniform);
        shader.bind();
        glUniform1i(location,slot);
        this.slot = slot;
        this.texture = texture;
    }

    public void setTexture(Texture texture) {this.texture = texture;}
    public Texture getTexture() {return texture;}

    public void bindUniform() {
        texture.bind(slot);
    }
}
