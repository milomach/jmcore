package com.jmcore.core.data.component;

import java.util.UUID;

import com.jmcore.core.data.PlayerDataComponentRegistry;

public class PlayerStateData {
    static {
        PlayerDataComponentRegistry.register(PlayerStateData.class, data -> new PlayerStateData());
    }

    private UUID spacerEntityUUID, targetEntityUUID;
    private double spacerWidth = 1.0, spacerHeight = -0.5, targetWidth = 1.0, targetHeight = 2.0;
    private Float originalYaw = null, originalPitch = null;
    private Double originalCameraDistance = null;

    public UUID getSpacerEntityUUID() { return spacerEntityUUID; }
    public void setSpacerEntityUUID(UUID uuid) { this.spacerEntityUUID = uuid; }
    public UUID getTargetEntityUUID() { return targetEntityUUID; }
    public void setTargetEntityUUID(UUID uuid) { this.targetEntityUUID = uuid; }

    public double getSpacerWidth() { return spacerWidth; }
    public void setSpacerWidth(double width) { this.spacerWidth = width; }
    public double getSpacerHeight() { return spacerHeight; }
    public void setSpacerHeight(double height) { this.spacerHeight = height; }
    public double getTargetWidth() { return targetWidth; }
    public void setTargetWidth(double width) { this.targetWidth = width; }
    public double getTargetHeight() { return targetHeight; }
    public void setTargetHeight(double height) { this.targetHeight = height; }

    public void saveOriginalView(float yaw, float pitch) { this.originalYaw = yaw; this.originalPitch = pitch; }
    public Float getOriginalYaw() { return originalYaw; }
    public Float getOriginalPitch() { return originalPitch; }
    public void clearOriginalView() { this.originalYaw = null; this.originalPitch = null; }

    public void saveOriginalCameraDistance(double value) { this.originalCameraDistance = value; }
    public Double getOriginalCameraDistance() { return originalCameraDistance; }
    public void clearOriginalCameraDistance() { this.originalCameraDistance = null; }
}