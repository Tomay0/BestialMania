package com.bestialMania.gui;

import com.bestialMania.MemoryManager;
import com.bestialMania.Settings;
import com.bestialMania.rendering.*;
import com.bestialMania.rendering.model.Rect2D;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.rendering.texture.Texture;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class Object2D {
    private Rect2D model;
    private Texture texture;
    private ShaderObject shaderObject;
    protected Matrix4f matrix;
    protected float x, y, width, height;
    private Map<Renderer,ShaderObject> objects = new HashMap<>();

    /**
     * Creates a 2D object at position x,y with the texture as the file name given
     */
    public Object2D(MemoryManager mm, float x, float y, String textureFile) {
        this.x = x;
        this.y = y;
        //load texture
        texture = Texture.loadImageTexture2D(mm,textureFile);
        this.width = texture.getWidth();
        this.height = texture.getHeight();

        generateModel(mm);
    }

    /**
     * Creates a 2D object at position x,y with a set width and height and a texture as the file name given
     */
    public Object2D(MemoryManager mm, float x, float y, float width, float height, String textureFile) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        //load texture
        texture = Texture.loadImageTexture2D(mm,textureFile);

        generateModel(mm);
    }

    /**
     * Create a model from the given x,y,width and height
     * converts to OpenGL coordinates
     */
    private void generateModel(MemoryManager mm) {
        //create the model
        float absWidth = 2 * width/ Settings.WIDTH;
        float absHeight = 2 * height/ Settings.HEIGHT;

        model = new Rect2D(mm,-1,1-absHeight,absWidth-1,1);//convert to OpenGL coordinate system

        //create the transformation matrix
        matrix = new Matrix4f();
        matrix.translate(2 * x/ Settings.WIDTH, -2 * y/ Settings.HEIGHT,0);
    }

    /**
     * Creates a 2D object at position x,y with the given texture
     */
    public Object2D(MemoryManager mm, float x, float y, Texture texture) {
        this.x = x;
        this.y = y;
        //load texture
        this.texture = texture;
        this.width = texture.getWidth();
        this.height = texture.getHeight();

        generateModel(mm);
    }

    /**
     * Creates a 2D object at position x,y,width,height with the given texture
     */
    public Object2D(MemoryManager mm, float x, float y, float width, float height, Texture texture) {
        this.x = x;
        this.y = y;
        //load texture
        this.texture = texture;
        this.width = width;
        this.height = height;

        generateModel(mm);
    }


    /**
     * Binds this object to a renderer
     */
    public void addToRenderer(Renderer renderer) {
        ShaderObject shaderObject  = renderer.createObject(model);
        shaderObject.addTexture(0, texture);
        shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix", matrix));
        objects.put(renderer,shaderObject);
    }

    /**
     * Removes this object from a renderer
     */
    public void removeFromRenderer(Renderer renderer) {
        if(objects.containsKey(renderer)) {
            renderer.removeObject(objects.get(renderer));
        }
    }

    public Texture getObject2DTexture(){return texture;}
    public Rect2D getObject2DModel(){return model;}
    public Matrix4f getMatrix(){return matrix;}


}
