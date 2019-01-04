package com.bestialMania.rendering.model;

import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.MemoryManager;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;

/**
 * Test class for rendering 2D rectangle with a texture
 */
public class Rect2D extends Model{
    public Rect2D(MemoryManager mm, float x1, float y1, float x2, float y2) {
        super(mm);
        float[] vertices = new float[] {
                x2,y2,
                x1,y1,
                x1,y2,
                x2,y2,
                x2,y1,
                x1,y1
        };
        float[] uvs = new float[] {
                1,1,
                0,0,
                0,1,
                1,1,
                1,0,
                0,0
        };
        genFloatAttribute(0,2,vertices);
        genFloatAttribute(1,2,uvs);
        setVertexCount(6);
    }
}
