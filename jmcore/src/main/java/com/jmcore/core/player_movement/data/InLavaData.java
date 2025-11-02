package com.jmcore.core.player_movement.data;

import com.jmcore.core.data.PlayerDataComponentRegistry;

/**
 * Stores in-lava movement state.
 */
public class InLavaData {
    static {
        PlayerDataComponentRegistry.register(InLavaData.class, data -> new InLavaData());
    }

    private double acceleration = 0.01;
    private double buoyancy = 0.02;
    private double lavaResistance = 0.2;

    public double getAcceleration() { return acceleration; }
    public void setAcceleration(double v) { acceleration = v; }
    public double getBuoyancy() { return buoyancy; }
    public void setBuoyancy(double v) { buoyancy = v; }
    public double getLavaResistance() { return lavaResistance; }
    public void setLavaResistance(double v) { lavaResistance = v; }
}