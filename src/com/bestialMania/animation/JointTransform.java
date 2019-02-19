package com.bestialMania.animation;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class JointTransform {
    private static Matrix4f translate = new Matrix4f();
    private static Vector3f interpolatedPosition = new Vector3f();
    private static Vector4f interpolatedRotation = new Vector4f();

    private Matrix4f matrix;
    private Vector3f position;
    private Vector4f rotation;

    private Joint joint;

    /**
     * A joint transform is a transform for a joint that can be converted to and from interpolatable vectors and matrices
     */
    public JointTransform(Joint joint, Matrix4f matrix) {
        this.joint = joint;
        this.matrix = matrix;
        calculatePositionAndRotation();
    }
    /**
     * Recalculate the position and rotation from the
     */
    private void calculatePositionAndRotation() {
        position = new Vector3f(matrix.m30(),matrix.m31(),matrix.m32());
        float w, x, y, z;
        float diagonal = matrix.m00() + matrix.m11() + matrix.m22();
        if (diagonal > 0) {
            float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
            w = w4 / 4f;
            x = (matrix.m21() - matrix.m12()) / w4;
            y = (matrix.m02() - matrix.m20()) / w4;
            z = (matrix.m10() - matrix.m01()) / w4;
        } else if ((matrix.m00() > matrix.m11()) && (matrix.m00() > matrix.m22())) {
            float x4 = (float) (Math.sqrt(1f + matrix.m00() - matrix.m11() - matrix.m22()) * 2f);
            w = (matrix.m21() - matrix.m12()) / x4;
            x = x4 / 4f;
            y = (matrix.m01() + matrix.m10()) / x4;
            z = (matrix.m02() + matrix.m20()) / x4;
        } else if (matrix.m11() > matrix.m22()) {
            float y4 = (float) (Math.sqrt(1f + matrix.m11() - matrix.m00() - matrix.m22()) * 2f);
            w = (matrix.m02() - matrix.m20()) / y4;
            x = (matrix.m01() + matrix.m10()) / y4;
            y = y4 / 4f;
            z = (matrix.m12() + matrix.m21()) / y4;
        } else {
            float z4 = (float) (Math.sqrt(1f + matrix.m22() - matrix.m00() - matrix.m11()) * 2f);
            w = (matrix.m10() - matrix.m01()) / z4;
            x = (matrix.m02() + matrix.m20()) / z4;
            y = (matrix.m12() + matrix.m21()) / z4;
            z = z4 / 4f;
        }
        rotation = new Vector4f(x,y,z,w);
        rotation.normalize();
    }

    /**
     * Get the matrix
     */
    public Matrix4f getMatrix() {
        return matrix;
    }


    /**
     * Interpolate 2 joint matrixs and store the result in dest
     */
    public static void interpolate(JointTransform j1, JointTransform j2, Matrix4f dest, float interpolation) {
        interpolatedPosition.x = j1.position.x + (j2.position.x - j1.position.x) * interpolation;
        interpolatedPosition.y = j1.position.y + (j2.position.y - j1.position.y) * interpolation;
        interpolatedPosition.z = j1.position.z + (j2.position.z - j1.position.z) * interpolation;

        float dot = j1.rotation.w * j2.rotation.w + j1.rotation.x * j2.rotation.x + j1.rotation.y * j2.rotation.y + j1.rotation.z * j2.rotation.z;
        float interpolationI = 1f - interpolation;
        if (dot < 0) {
            interpolatedRotation.w = interpolationI * j1.rotation.w + interpolation * -j2.rotation.w;
            interpolatedRotation.x = interpolationI * j1.rotation.x + interpolation * -j2.rotation.x;
            interpolatedRotation.y = interpolationI * j1.rotation.y + interpolation * -j2.rotation.y;
            interpolatedRotation.z = interpolationI * j1.rotation.z + interpolation * -j2.rotation.z;
        } else {
            interpolatedRotation.w = interpolationI * j1.rotation.w + interpolation * j2.rotation.w;
            interpolatedRotation.x = interpolationI * j1.rotation.x + interpolation * j2.rotation.x;
            interpolatedRotation.y = interpolationI * j1.rotation.y + interpolation * j2.rotation.y;
            interpolatedRotation.z = interpolationI * j1.rotation.z + interpolation * j2.rotation.z;
        }

        calculateMatrix(dest);
    }

    /**
     * Calculate a matrix from a rotation and position
     */
    private static void calculateMatrix(Matrix4f dest) {
        final float xy = interpolatedRotation.x * interpolatedRotation.y;
        final float xz = interpolatedRotation.x * interpolatedRotation.z;
        final float xw = interpolatedRotation.x * interpolatedRotation.w;
        final float yz = interpolatedRotation.y * interpolatedRotation.z;
        final float yw = interpolatedRotation.y * interpolatedRotation.w;
        final float zw = interpolatedRotation.z * interpolatedRotation.w;
        final float xSquared = interpolatedRotation.x * interpolatedRotation.x;
        final float ySquared = interpolatedRotation.y * interpolatedRotation.y;
        final float zSquared = interpolatedRotation.z * interpolatedRotation.z;
        dest.m00(1 - 2 * (ySquared + zSquared));
        dest.m01(2 * (xy - zw));
        dest.m02(2 * (xz + yw));
        dest.m03(0);
        dest.m10(2 * (xy + zw));
        dest.m11(1 - 2 * (xSquared + zSquared));
        dest.m12(2 * (yz - xw));
        dest.m13(0);
        dest.m20(2 * (xz - yw));
        dest.m21(2 * (yz + xw));
        dest.m22(1 - 2 * (xSquared + ySquared));
        dest.m23(0);
        dest.m30(0);
        dest.m32(0);
        dest.m31(0);
        dest.m33(1);

        translate.identity();
        translate.translate(interpolatedPosition);

        translate.mul(dest,dest);
    }

}
