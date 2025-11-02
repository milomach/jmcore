package com.jmcore.core.player_movement.data;

import com.jmcore.core.data.PlayerDataComponentRegistry;

/**
 * Stores configurable movement magnitudes and factors for a player.
 */
public class MovementConfigData {
    static {
        PlayerDataComponentRegistry.register(MovementConfigData.class, data -> new MovementConfigData());
    }

    private double forwardMagnitude = 0.2;
    private double backwardMagnitude = 0.15;
    private double strafeMagnitude = 0.18;
    private double diagonalFactor = 0.7071; // 1/sqrt(2)
    private double sprintMagnitude = 0.3;
    private double sneakMagnitude = 0.1;
    private double jumpMagnitude = 0.42;

    public double getForwardMagnitude() { return forwardMagnitude; }
    public void setForwardMagnitude(double v) { forwardMagnitude = v; }
    public double getBackwardMagnitude() { return backwardMagnitude; }
    public void setBackwardMagnitude(double v) { backwardMagnitude = v; }
    public double getStrafeMagnitude() { return strafeMagnitude; }
    public void setStrafeMagnitude(double v) { strafeMagnitude = v; }
    public double getDiagonalFactor() { return diagonalFactor; }
    public void setDiagonalFactor(double v) { diagonalFactor = v; }
    public double getSprintMagnitude() { return sprintMagnitude; }
    public void setSprintMagnitude(double v) { sprintMagnitude = v; }
    public double getSneakMagnitude() { return sneakMagnitude; }
    public void setSneakMagnitude(double v) { sneakMagnitude = v; }
    public double getJumpMagnitude() { return jumpMagnitude; }
    public void setJumpMagnitude(double v) { jumpMagnitude = v; }
}