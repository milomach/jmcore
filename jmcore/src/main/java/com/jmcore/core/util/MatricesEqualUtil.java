package com.jmcore.core.util;

import org.joml.Matrix4f;

public class MatricesEqualUtil {
    /**
     * Helper to compare two Matrix4f objects for near-equality.
     */
    public static boolean matricesEqual(Matrix4f a, Matrix4f b, float epsilon) {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                float va = a.get(row, col);
                float vb = b.get(row, col);
                if (Math.abs(va - vb) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }
}
