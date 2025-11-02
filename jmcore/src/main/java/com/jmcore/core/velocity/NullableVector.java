package com.jmcore.core.velocity;

import org.bukkit.util.Vector;

/**
 * A vector where each component (x, y, z) can be null.
 * Used for hybrid velocity control in VelocityManager.
 */
public class NullableVector {
    public final Double x, y, z;

    public NullableVector(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static NullableVector fromVector(Vector v) {
        return new NullableVector(v.getX(), v.getY(), v.getZ());
    }

    public Vector toVector(Vector fallback) {
        return new Vector(
            x != null ? x : fallback.getX(),
            y != null ? y : fallback.getY(),
            z != null ? z : fallback.getZ()
        );
    }
}