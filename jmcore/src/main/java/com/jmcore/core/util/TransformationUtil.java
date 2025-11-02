package com.jmcore.core.util;

import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/**
 * Utility for converting Bukkit Transformation objects to JOML Matrix4f.
 */
public class TransformationUtil {

    /**
     * Converts a Bukkit Transformation to a JOML Matrix4f.
     * Order: translate -> rotate(left) -> scale -> rotate(right)
     */
    public static Matrix4f toMatrix(Transformation t) {
        Vector3f translation = new Vector3f((float)t.getTranslation().x, (float)t.getTranslation().y, (float)t.getTranslation().z);
        Vector3f scale = new Vector3f((float)t.getScale().x, (float)t.getScale().y, (float)t.getScale().z);
        Quaternionf leftRotation = new Quaternionf((float)t.getLeftRotation().x, (float)t.getLeftRotation().y, (float)t.getLeftRotation().z, (float)t.getLeftRotation().w);
        Quaternionf rightRotation = new Quaternionf((float)t.getRightRotation().x, (float)t.getRightRotation().y, (float)t.getRightRotation().z, (float)t.getRightRotation().w);

        return new Matrix4f()
                .translate(translation)
                .rotate(leftRotation)
                .scale(scale)
                .rotate(rightRotation);
    }
}