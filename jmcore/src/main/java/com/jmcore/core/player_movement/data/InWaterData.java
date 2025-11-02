package com.jmcore.core.player_movement.data;

import com.jmcore.core.data.PlayerDataComponentRegistry;

/**
 * Stores in-water movement state.
 */
public class InWaterData {
    static {
        PlayerDataComponentRegistry.register(InWaterData.class, data -> new InWaterData());
    }

    private double acceleration = 0.02;
    private double buoyancy = 0.04;
    private double waterResistance = 0.1;

    public double getAcceleration() { return acceleration; }
    public void setAcceleration(double v) { acceleration = v; }
    public double getBuoyancy() { return buoyancy; }
    public void setBuoyancy(double v) { buoyancy = v; }
    public double getWaterResistance() { return waterResistance; }
    public void setWaterResistance(double v) { waterResistance = v; }
}