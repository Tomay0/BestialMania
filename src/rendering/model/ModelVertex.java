package rendering.model;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * For use in loading models from files
 */
public class ModelVertex {
    private Vector3f vertex;
    private Vector2f uv;
    private Vector3f normal;
    private int id;

    /**
     * Vertex/UV/Normal
     */
    public ModelVertex(int id, Vector3f vertex, Vector2f uv, Vector3f normal) {
        this.id = id;
        this.vertex = vertex;
        this.uv = uv;
        this.normal = normal;
    }

    /**
     * Vertex only
     */
    public ModelVertex(int id, Vector3f vertex) {
        this.id = id;
        this.vertex = vertex;
    }

    /**
     * Vertex/UV
     */
    public ModelVertex(int id, Vector3f vertex, Vector2f uv) {
        this.id = id;
        this.vertex = vertex;
        this.uv = uv;
    }

    /**
     * Vertex/Normal
     */
    public ModelVertex(int id, Vector3f vertex, Vector3f normal) {
        this.id = id;
        this.vertex = vertex;
        this.normal = normal;
    }

    public int getID() {return id;}
    public Vector3f getVertex() {return vertex;}
    public Vector2f getUV() {return uv;}
    public Vector3f getNormal() {return normal;}
}
