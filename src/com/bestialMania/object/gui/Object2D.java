package com.bestialMania.object.gui;

import com.bestialMania.DisplaySettings;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Rect2D;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;

public class Object2D {
    private Rect2D model;
    private Texture texture;
    protected Matrix4f matrix;
    protected float x, y, width, height;

    /**
     * Creates a 2D object at position x,y with the texture as the file name given
     */
    public Object2D(float x, float y, String textureFile) {
        this.x = x;
        this.y = y;
        //load texture
        texture = Texture.loadImageTexture2D(textureFile);
        this.width = texture.getWidth();
        this.height = texture.getHeight();

        generateModel();
    }

    /**
     * Creates a 2D object at position x,y with a set width and height and a texture as the file name given
     */
    public Object2D(float x, float y, float width, float height, String textureFile) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        //load texture
        texture = Texture.loadImageTexture2D(textureFile);

        generateModel();
    }

    /**
     * Create a model from the given x,y,width and height
     * converts to OpenGL coordinates
     */
    private void generateModel() {
        //create the model
        float absWidth = 2 * width/ DisplaySettings.WIDTH;
        float absHeight = 2 * height/ DisplaySettings.HEIGHT;

        System.out.println(absWidth + "," + absHeight);

        model = new Rect2D(-1,1-absHeight,absWidth-1,1);//convert to OpenGL coordinate system

        //create the transformation matrix
        matrix = new Matrix4f();
        matrix.translate(2 * x/DisplaySettings.WIDTH, -2 * y/DisplaySettings.HEIGHT,0);
    }

    /**
     * Creates a 2D object at position x,y with the given texture
     */
    public Object2D(float x, float y, Texture texture) {
        this.x = x;
        this.y = y;
        //load texture
        this.texture = texture;
        this.width = texture.getWidth();
        this.height = texture.getHeight();

        generateModel();
    }

    /**
     * Creates a 2D object at position x,y,width,height with the given texture
     */
    public Object2D(float x, float y, float width, float height, Texture texture) {
        this.x = x;
        this.y = y;
        //load texture
        this.texture = texture;
        this.width = width;
        this.height = height;

        generateModel();
    }

    /**
     * Binds this object to a renderer
     */
    public void addToRenderer(Renderer renderer) {
        ShaderObject obj = renderer.createObject(model);
        obj.addTexture(0,texture);
        obj.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix",matrix));
    }


}
