package com.jmcore.core.util;

import org.joml.Quaternionf;

/**
 * Utility for converting single-axis degree rotations to quaternions.
 */
public class QuaternionUtil {

    /**
     * Returns a quaternion representing a rotation of the given degrees around the X axis (pitch).
     */
    public static Quaternionf fromPitchDegrees(float pitchDegrees) {
        float radians = (float) Math.toRadians(pitchDegrees);
        return new Quaternionf().rotationX(radians);
    }

    /**
     * Returns a quaternion representing a rotation of the given degrees around the Y axis (yaw).
     */
    public static Quaternionf fromYawDegrees(float yawDegrees) {
        float radians = (float) Math.toRadians(yawDegrees);
        return new Quaternionf().rotationY(radians);
    }

    /**
     * Returns a quaternion representing a rotation of the given degrees around the Z axis (roll).
     */
    public static Quaternionf fromRollDegrees(float rollDegrees) {
        float radians = (float) Math.toRadians(rollDegrees);
        return new Quaternionf().rotationZ(radians);
    }
}