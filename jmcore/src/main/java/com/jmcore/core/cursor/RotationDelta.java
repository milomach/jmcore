package com.jmcore.core.cursor;

/**
 * Represents the change in yaw and pitch for a player in a single tick.
 * Pitch is inverted so positive = up, negative = down (plugin convention).
 */
public class RotationDelta {
    private final double deltaYaw;
    private final double deltaPitch; // positive = up, negative = down

    public RotationDelta(double deltaYaw, double deltaPitch) {
        this.deltaYaw = deltaYaw;
        this.deltaPitch = deltaPitch;
    }

    /**
     * @return Change in yaw in degrees (positive = right, negative = left)
     */
    public double getDeltaYaw() {
        return deltaYaw;
    }

    /**
     * @return Change in pitch in degrees (positive = up, negative = down)
     */
    public double getDeltaPitch() {
        return deltaPitch;
    }
}