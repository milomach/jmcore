package com.jmcore.core.player_movement.data;

import com.jmcore.core.data.PlayerDataComponentRegistry;

/**
 * Stores in-air movement state.
 */
public class InAirData {
    static {
        PlayerDataComponentRegistry.register(InAirData.class, data -> new InAirData());
    }

    private double acceleration = 0.02;
    private double gravity = 0.08;
    private double airResistance = 0.02;

    public double getAcceleration() { return acceleration; }
    public void setAcceleration(double v) { acceleration = v; }
    public double getGravity() { return gravity; }
    public void setGravity(double v) { gravity = v; }
    public double getAirResistance() { return airResistance; }
    public void setAirResistance(double v) { airResistance = v; }
}