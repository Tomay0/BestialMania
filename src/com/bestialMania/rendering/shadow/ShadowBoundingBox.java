package com.bestialMania.rendering.shadow;

import com.bestialMania.collision.BoundingBox;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShadowBoundingBox {
    private Matrix4f shadowBoxMatrix,biasShadowBoxMatrix;

    public ShadowBoundingBox(BoundingBox boundingBox, Vector3f lightDir) {
        //light view matrix
        Matrix4f rotate90 = new Matrix4f();
        rotate90.identity();

        if(lightDir.x==0 && lightDir.z==0) rotate90.rotateX((float)Math.PI/2.0f);
        else rotate90.rotateY((float)Math.PI/2.0f);

        Vector4f lightRight = new Vector4f(lightDir.x,lightDir.y,lightDir.z,0);

        lightRight.mul(rotate90);
        Vector3f lightUp = new Vector3f();
        lightDir.cross(new Vector3f(lightRight.x,lightRight.y,lightRight.z),lightUp);

        Matrix4f lightViewMatrix = new Matrix4f();
        lightViewMatrix.identity();
        lightViewMatrix.lookAt(new Vector3f(0,0,0),lightDir,lightUp);

        Matrix4f inverseLightViewMatrix = new Matrix4f();
        lightViewMatrix.invert(inverseLightViewMatrix);

        shadowBoxMatrix = new Matrix4f();
        biasShadowBoxMatrix = new Matrix4f();


        Vector4f[] bboxCorners = new Vector4f[] {
            new Vector4f(boundingBox.getX1(), boundingBox.getY1(), boundingBox.getZ1(), 1.0f),
            new Vector4f(boundingBox.getX1(), boundingBox.getY1(), boundingBox.getZ2(), 1.0f),
            new Vector4f(boundingBox.getX1(), boundingBox.getY2(), boundingBox.getZ1(), 1.0f),
            new Vector4f(boundingBox.getX1(), boundingBox.getY2(), boundingBox.getZ2(), 1.0f),
            new Vector4f(boundingBox.getX2(), boundingBox.getY1(), boundingBox.getZ1(), 1.0f),
            new Vector4f(boundingBox.getX2(), boundingBox.getY1(), boundingBox.getZ2(), 1.0f),
            new Vector4f(boundingBox.getX2(), boundingBox.getY2(), boundingBox.getZ1(), 1.0f),
            new Vector4f(boundingBox.getX2(), boundingBox.getY2(), boundingBox.getZ2(), 1.0f)
        };
        //apply transformation
        for(int i = 0;i<8;i++) {
            bboxCorners[i].mul(lightViewMatrix);
        }

        float minX, maxX, minY, maxY, minZ, maxZ;
        Vector4f v = bboxCorners[0];
        minX = v.x;
        maxX = v.x;
        minY = v.y;
        maxY = v.y;
        minZ = v.z;
        maxZ = v.z;
        for (int i = 1; i < 8; i++) {
            v = bboxCorners[i];
            if (v.x < minX) minX = v.x;
            if (v.x > maxX) maxX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.y > maxY) maxY = v.y;
            if (v.z < minZ) minZ = v.z;
            if (v.z > maxZ) maxZ = v.z;
        }


        //scaling
        float bw = maxX - minX;
        float bh = maxY - minY;
        float bl = maxZ - minZ;
        for (int i = 1; i < 8; i++) {
            bboxCorners[i].mul(inverseLightViewMatrix);
        }

        //translation in world space to centre
        Vector4f worldCentre = new Vector4f();
        worldCentre.x = (maxX + minX) / 2.0f;
        worldCentre.y = (maxY + minY) / 2.0f;
        worldCentre.z = (maxZ + minZ) / 2.0f;
        worldCentre.w = 1;

        worldCentre.mul(inverseLightViewMatrix);
        //calculate matrices
        shadowBoxMatrix.identity();

        //note this is: scale x rotation x translation
        shadowBoxMatrix.scale(2.0f / bw, 2.0f / bh, -2.0f / bl);
        shadowBoxMatrix.mul(lightViewMatrix);
        shadowBoxMatrix.translate(-worldCentre.x,-worldCentre.y,-worldCentre.z);

        ShadowBox.BIAS_MATRIX.mul(shadowBoxMatrix, biasShadowBoxMatrix);

    }

    /**
     * Link the matrix to the depth renderer
     */
    public void linkToDepthRenderer(Renderer renderer) {
        renderer.addUniform(new UniformMatrix4(renderer.getShader(),"viewMatrix",shadowBoxMatrix));
    }

    /**
     * Link shadow bias matrix to a renderer that draws shadows
     */
    public void linkToRenderer(Renderer renderer, int id) {
        renderer.addUniform(new UniformMatrix4(renderer.getShader(),"shadowMatrix[" + id + "]",biasShadowBoxMatrix));

    }
}
