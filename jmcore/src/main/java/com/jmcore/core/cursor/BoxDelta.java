package com.jmcore.core.cursor;

/**
 * Represents the change in grid boxes for a player in a single tick.
 * Positive = right (yaw), up (pitch); negative = left, down.
 */
public class BoxDelta {
    private final double deltaBoxesYaw;
    private final double deltaBoxesPitch;

    public BoxDelta(double deltaBoxesYaw, double deltaBoxesPitch) {
        this.deltaBoxesYaw = deltaBoxesYaw;
        this.deltaBoxesPitch = deltaBoxesPitch;
    }

    public double getDeltaBoxesYaw() {
        return deltaBoxesYaw;
    }

    public double getDeltaBoxesPitch() {
        return deltaBoxesPitch;
    }
}