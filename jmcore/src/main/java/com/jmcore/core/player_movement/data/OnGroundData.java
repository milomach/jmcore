package com.jmcore.core.player_movement.data;

import com.jmcore.core.data.PlayerDataComponentRegistry;

/**
 * Stores on-ground movement state, friction, and jumping state.
 */
public class OnGroundData {
    static {
        PlayerDataComponentRegistry.register(OnGroundData.class, data -> new OnGroundData());
    }

    private double acceleration = 0.1;
    private double friction = 0.4;
    private boolean jumping = false;

    public double getAcceleration() { return acceleration; }
    public void setAcceleration(double v) { acceleration = v; }
    public double getFriction() { return friction; }
    public void setFriction(double v) { friction = v; }
    public boolean isJumping() { return jumping; }
    public void setJumping(boolean v) { jumping = v; }
}