package com.bestialMania.object.gui.text;

import com.bestialMania.DisplaySettings;
import com.bestialMania.rendering.*;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.rendering.shader.UniformVector3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Text {
    private Model model;
    private Texture texture;
    private Matrix4f matrix;
    private Vector3f color;
    private ShaderObject shaderObject;

    public enum TextAlign {LEFT,CENTER,RIGHT};
    //public enum VerticalTextAlign {TOP,MIDDLE,BOTTOM};

    /**
     * Represents a block of text
     * @param mm Memory manager to delete this when the text is no longer used
     * @param text Text to render as a string
     * @param font The font
     * @param size Font size (in pixels)
     * @param x x position (in pixels)
     * @param y y position (in pixels)
     * @param align alignment of the text in relation to the position (either LEFT,RIGHT or CENTER)
     * @param color text colour (RGB from 0 to 1)
     */
    public Text(MemoryManager mm, String text, Font font, float size, int x, int y, TextAlign align, Vector3f color) {
        texture = font.getTexture();
        model = new Model(mm);
        List<Vector2f> vertices = new ArrayList<>();
        List<Vector2f> uvs = new ArrayList<>();

        //get scaling correct
        float scale = size/font.getLineHeight();
        float xScale = 2.0f/ DisplaySettings.WIDTH;
        float yScale = 2.0f/DisplaySettings.HEIGHT;

        //initial cursor position
        Vector2f cursor = new Vector2f(0,0);

        for(char c : text.toCharArray()) {
            FontCharacter character = font.getCharacter(c);

            Vector2f tr = character.getPosTopRight();
            Vector2f tl = character.getPosTopLeft();
            Vector2f br = character.getPosBottomRight();
            Vector2f bl = character.getPosBottomLeft();

            //vertices
            vertices.add(new Vector2f(tr.x+cursor.x,tr.y+cursor.y));
            vertices.add(new Vector2f(bl.x+cursor.x,bl.y+cursor.y));
            vertices.add(new Vector2f(tl.x+cursor.x,tl.y+cursor.y));
            vertices.add(new Vector2f(tr.x+cursor.x,tr.y+cursor.y));
            vertices.add(new Vector2f(br.x+cursor.x,br.y+cursor.y));
            vertices.add(new Vector2f(bl.x+cursor.x,bl.y+cursor.y));

            //uvs
            uvs.add(character.getUVTopRight());
            uvs.add(character.getUVBottomLeft());
            uvs.add(character.getUVTopLeft());
            uvs.add(character.getUVTopRight());
            uvs.add(character.getUVBottomRight());
            uvs.add(character.getUVBottomLeft());

            cursor.x+=character.getXAdvance();
        }
        for(Vector2f v : vertices) {
            v.x = scale*xScale*v.x-1;
            v.y = 1-scale*yScale*v.y;
        }
        model.storeAttributeVector2f(0,vertices);
        model.storeAttributeVector2f(1,uvs);
        model.setVertexCount(vertices.size());

        matrix = new Matrix4f();
        switch(align) {
            case CENTER: {
                matrix.translate((x-scale*cursor.x/2.0f)*xScale,-y*yScale,0);
                break;
            }
            case LEFT: {
                matrix.translate(x*xScale,-y*yScale,0);
                break;
            }
            case RIGHT: {
                matrix.translate((x-scale*cursor.x)*xScale,-y*yScale,0);
                break;
            }
            default: break;
        }
        this.color = color;
    }

    /**
     * Binds this object to a renderer
     */
    public void addToRenderer(Renderer renderer) {
        shaderObject  = renderer.createObject(model);
        shaderObject.addTexture(0, texture);
        shaderObject.addUniform(new UniformVector3f(renderer.getShader(),"color",color));
        shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix", matrix));
    }

    /**
     * Removes the object from a renderer
     */
    public void removeFromRenderer(Renderer renderer) {
        renderer.removeObject(shaderObject);
    }


}
