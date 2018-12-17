import rendering.Texture;
import rendering.model.Model;

/**
 * Test class for rendering 2D rectangle with a texture
 */
public class Rect2D {
    Model model;
    Texture texture;

    public Rect2D(float x1, float y1, float x2, float y2, Texture tex) {
        model = new Model();
        this.texture = tex;
        float[] vertices = new float[] {
                x2,y2,0,
                x1,y1,0,
                x1,y2,0,
                x2,y2,0,
                x2,y1,0,
                x1,y1,0
        };
        float[] uvs = new float[] {
                1,1,
                0,0,
                0,1,
                1,1,
                1,0,
                0,0
        };
        model.genFloatAttribute(0,3,vertices);
        model.genFloatAttribute(1,2,uvs);
        model.setVertexCount(6);
    }

    public void draw() {
        texture.bind(0);
        model.draw();
    }
}
