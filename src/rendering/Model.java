package rendering;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.CallbackI;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Model {
    private int vao;
    private List<Integer> vbos;//list of buffers
    private List<Integer> attributes;//list of attribute positions to bind to when drawing

    private int vertexCount = 0;
    private int indicesCount = 0;

    /**
     * Initializes the model's VAO and binds to it
     */
    public Model() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbos = new ArrayList<>();
        attributes = new ArrayList<>();
    }

    /**
     * Deletes the model from memory
     */
    public void cleanUp() {
        glDeleteVertexArrays(vao);
        for(int vbo : vbos) glDeleteBuffers(vbo);
    }

    /**
     * Draws the model
     */
    public void draw() {
        if(vertexCount==0 && indicesCount==0) return;//no model loaded = don't draw

        glBindVertexArray(vao);//bind the VAO

        //enable attributes
        for(int attrib : attributes) glEnableVertexAttribArray(attrib);

        //draw indices
        if(indicesCount>0) {
            glDrawElements(GL_TRIANGLES,indicesCount,GL_UNSIGNED_INT,0);
        }
        //no indices loaded - draw vertices
        else{
            glDrawArrays(GL_TRIANGLES,0,vertexCount);
        }
        //disable atributes
        for(int attrib : attributes) glDisableVertexAttribArray(attrib);
    }

    /**
     * Bind the indices array
     */
    public void bindIndices(int[] indices) {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,vbo);
        IntBuffer buffer = getIntBuffer(indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);

        vbos.add(vbo);
        indicesCount = indices.length;
    }

    /**
     * Set number of vertices for models that do not have an index buffer
     */
    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    /**
     * List of 4d float vectors
     */
    public void storeAttributeVector4f(int position, List<Vector4f> vectors) {
        float[] data = new float[vectors.size()*4];
        for(int i = 0;i<vectors.size();i++) {
            Vector4f v = vectors.get(i);
            data[i*4] = v.x;
            data[i*4+1] = v.y;
            data[i*4+2] = v.z;
            data[i*4+3] = v.w;
        }
        genFloatAttribute(position,3,data);
    }

    /**
     * List of 3d float vectors
     */
    public void storeAttributeVector3f(int position, List<Vector3f> vectors) {
        float[] data = new float[vectors.size()*3];
        for(int i = 0;i<vectors.size();i++) {
            Vector3f v = vectors.get(i);
            data[i*3] = v.x;
            data[i*3+1] = v.y;
            data[i*3+2] = v.z;
        }
        genFloatAttribute(position,3,data);
    }

    /**
     * List of 2d float vectors
     */
    public void storeAttributeVector2f(int position, List<Vector2f> vectors) {
        float[] data = new float[vectors.size()*2];
        for(int i = 0;i<vectors.size();i++) {
            Vector2f v = vectors.get(i);
            data[i*2] = v.x;
            data[i*2+1] = v.y;
        }
        genFloatAttribute(position,2,data);
    }

    /**
     * List of floats
     */
    public void storeAttributeFloat(int position, List<Float> vectors) {
        float[] data = new float[vectors.size()];
        for(int i = 0;i<vectors.size();i++) data[i] = vectors.get(i);
        genFloatAttribute(position,1,data);
    }

    /**
     * List of 4d int vectors
     */
    public void storeAttributeVector4i(int position, List<Vector4i> vectors) {
        int[] data = new int[vectors.size()*4];
        for(int i = 0;i<vectors.size();i++) {
            Vector4i v = vectors.get(i);
            data[i*4] = v.x;
            data[i*4+1] = v.y;
            data[i*4+2] = v.z;
            data[i*4+3] = v.w;
        }
        genIntAttribute(position,3,data);
    }

    /**
     * List of 3d int vectors
     */
    public void storeAttributeVector3i(int position, List<Vector3i> vectors) {
        int[] data = new int[vectors.size()*3];
        for(int i = 0;i<vectors.size();i++) {
            Vector3i v = vectors.get(i);
            data[i*3] = v.x;
            data[i*3+1] = v.y;
            data[i*3+2] = v.z;
        }
        genIntAttribute(position,3,data);
    }

    /**
     * List of 2d int vectors
     */
    public void storeAttributeVector2i(int position, List<Vector2i> vectors) {
        int[] data = new int[vectors.size()*2];
        for(int i = 0;i<vectors.size();i++) {
            Vector2i v = vectors.get(i);
            data[i*2] = v.x;
            data[i*2+1] = v.y;
        }
        genIntAttribute(position,2,data);
    }

    /**
     * List of ints
     */
    public void storeAttributeInt(int position, List<Integer> vectors) {
        int[] data = new int[vectors.size()];
        for(int i = 0;i<vectors.size();i++) data[i] = vectors.get(i);
        genIntAttribute(position,1,data);
    }

    /**
     * Stores a list of floats of a specified size
     * @param position position to store the attribute in to be used by the shader
     * @param size the number of dimensions of the float (eg: 3d vectors would be size 3)
     * @param data the data
     */
    public void genFloatAttribute(int position, int size, float[] data) {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER,vbo);
        FloatBuffer buffer = getFloatBuffer(data);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glVertexAttribPointer(position,size,GL_FLOAT,false,0,0);

        vbos.add(vbo);
        attributes.add(position);
    }

    /**
     * Stores a list of ints of a specified size
     * @param position position to store the attribute in to be used by the shader
     * @param size the number of dimensions of the int (eg: 3d vectors would be size 3)
     * @param data the data
     */
    public void genIntAttribute(int position, int size, int[] data) {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER,vbo);
        IntBuffer buffer = getIntBuffer(data);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glVertexAttribIPointer(position,size,GL_INT,size*4,0);

        vbos.add(vbo);
        attributes.add(position);
    }


    /**
     * Converts a list of floats into a float buffer
     */
    private FloatBuffer getFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    /**
     * Converts a list of ints into an int buffer
     */
    private IntBuffer getIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }
}
