package com.bestialMania.rendering.shadow;

import com.bestialMania.Settings;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShadowBox {
    private static final Matrix4f BIAS_MATRIX = getBias();

    private static Matrix4f getBias() {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.translate(0.5f,0.5f,0.5f);
        matrix.scale(0.5f,0.5f,0.5f);
        return matrix;
    }

    private static final float OFFSET = 64;

    private Matrix4f viewMatrix, inverseViewMatrix, lightViewMatrix, inverseLightViewMatrix, shadowBoxMatrix, biasShadowBoxMatrix, transformationMatrix;
    private Vector4f[] frustumCorners;
    private Vector4f[] frustumCornersLightSpace = new Vector4f[8];
    private Vector4f worldCentre, worldCentreLightSpace;
    private Matrix4f[] testMatrices = new Matrix4f[9];

    /**
     * Create a new shadow box
     */
    public ShadowBox(float aspectRatio, Matrix4f viewMatrix, Vector3f lightDirection, float near, float far) {
        this.viewMatrix = viewMatrix;

        float tan = (float) Math.tan(Settings.FOV / 2.0f);
        float yn = near * tan;
        float yf = far * tan;
        float xn = yn * aspectRatio;
        float xf = yf * aspectRatio;

        Matrix4f rotate90 = new Matrix4f();
        rotate90.identity();

        if(lightDirection.x==0 && lightDirection.z==0) rotate90.rotateX((float)Math.PI/2.0f);
        else rotate90.rotateY((float)Math.PI/2.0f);

        Vector4f lightRight = new Vector4f(lightDirection.x,lightDirection.y,lightDirection.z,0);

        lightRight.mul(rotate90);
        Vector3f lightUp = new Vector3f();
        lightDirection.cross(new Vector3f(lightRight.x,lightRight.y,lightRight.z),lightUp);
        //LIGHT UP IS CORRECTLY CALCULATED

        lightViewMatrix = new Matrix4f();
        lightViewMatrix.identity();
        lightViewMatrix.lookAt(new Vector3f(0,0,0),lightDirection,lightUp);
        inverseLightViewMatrix = new Matrix4f();
        lightViewMatrix.invert(inverseLightViewMatrix);

        shadowBoxMatrix = new Matrix4f();
        biasShadowBoxMatrix = new Matrix4f();
        inverseViewMatrix = new Matrix4f();
        transformationMatrix = new Matrix4f();

        frustumCorners = new Vector4f[] {
                new Vector4f(-xn, -yn, -near, 1.0f),
                new Vector4f(xn, -yn, -near, 1.0f),
                new Vector4f(-xn, yn, -near, 1.0f),
                new Vector4f(xn, yn, -near, 1.0f),
                new Vector4f(-xf, -yf, -far, 1.0f),
                new Vector4f(xf, -yf, -far, 1.0f),
                new Vector4f(-xf, yf, -far, 1.0f),
                new Vector4f(xf, yf, -far, 1.0f)
        };
        for(int i = 0;i<8;i++){
            frustumCornersLightSpace[i]=new Vector4f();
            testMatrices[i] = new Matrix4f();
        }
        testMatrices[8] = new Matrix4f();
        worldCentre = new Vector4f();
        worldCentreLightSpace = new Vector4f(0,0,0,1);
    }

    /**
     * Update the shadow box
     */
    public void update() {
        //calculate the transformation matrix for changing the camera's frustum corners
        viewMatrix.invert(inverseViewMatrix);
        lightViewMatrix.mul(inverseViewMatrix,transformationMatrix);

        //apply transformation
        for(int i = 0;i<8;i++) {
            frustumCorners[i].mul(transformationMatrix, frustumCornersLightSpace[i]);
        }

        float minX, maxX, minY, maxY, minZ, maxZ;
        Vector4f v = frustumCornersLightSpace[0];
        minX = v.x;
        maxX = v.x;
        minY = v.y;
        maxY = v.y;
        minZ = v.z;
        maxZ = v.z;
        for (int i = 1; i < 8; i++) {
            v = frustumCornersLightSpace[i];
            if (v.x < minX) minX = v.x;
            if (v.x > maxX) maxX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.y > maxY) maxY = v.y;
            if (v.z < minZ) minZ = v.z;
            if (v.z > maxZ) maxZ = v.z;
        }
        //an offset
        maxZ += OFFSET;

        //scaling
        float bw = maxX - minX;
        float bh = maxY - minY;
        float bl = maxZ - minZ;
        for (int i = 1; i < 8; i++) {
            frustumCornersLightSpace[i].mul(inverseLightViewMatrix);
            testMatrices[i].identity();
            testMatrices[i].translate(frustumCornersLightSpace[i].x,frustumCornersLightSpace[i].y,frustumCornersLightSpace[i].z);
        }

        //translation in world space to centre
        worldCentreLightSpace.x = (maxX + minX) / 2.0f;
        worldCentreLightSpace.y = (maxY + minY) / 2.0f;
        worldCentreLightSpace.z = (maxZ + minZ) / 2.0f;

        worldCentreLightSpace.mul(inverseLightViewMatrix, worldCentre);

        testMatrices[8].identity();
        testMatrices[8].translate(worldCentre.x,worldCentre.y,worldCentre.z);

        //calculate matrices
        shadowBoxMatrix.identity();

        //note this is: scale x rotation x translation
        shadowBoxMatrix.scale(2.0f / bw, 2.0f / bh, -2.0f / bl);
        shadowBoxMatrix.mul(lightViewMatrix);
        shadowBoxMatrix.translate(-worldCentre.x,-worldCentre.y,-worldCentre.z);

        BIAS_MATRIX.mul(shadowBoxMatrix, biasShadowBoxMatrix);
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

    public Matrix4f testMatrix(int i) {
        return testMatrices[i];
    }
}
