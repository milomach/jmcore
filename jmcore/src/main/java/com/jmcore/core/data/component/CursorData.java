package com.jmcore.core.data.component;

import com.jmcore.core.data.PlayerDataComponentRegistry;

public class CursorData {
    static {
        PlayerDataComponentRegistry.register(CursorData.class, data -> new CursorData());
    }

    private boolean headTrackingEnabled = false;
    private double degreesPerBox = 1.0;
    private double internalBoxX = 0.0, internalBoxY = 0.0;
    private int currentBoxX = 0, currentBoxY = 0;
    private int maxBoxXPos = 200, maxBoxXNeg = 200, maxBoxYPos = 200, maxBoxYNeg = 200;

    public boolean isHeadTrackingEnabled() { return headTrackingEnabled; }
    public void setHeadTrackingEnabled(boolean enabled) { this.headTrackingEnabled = enabled; }

    public double getDegreesPerBox() { return degreesPerBox; }
    public void setDegreesPerBox(double degreesPerBox) { this.degreesPerBox = degreesPerBox; }

    public double getInternalBoxX() { return internalBoxX; }
    public double getInternalBoxY() { return internalBoxY; }
    public void setInternalBox(double x, double y) { this.internalBoxX = x; this.internalBoxY = y; }
    public void resetInternalBox() { this.internalBoxX = 0.0; this.internalBoxY = 0.0; }

    public int getCurrentBoxX() { return currentBoxX; }
    public int getCurrentBoxY() { return currentBoxY; }
    public void setCurrentBox(int x, int y) { this.currentBoxX = x; this.currentBoxY = y; }
    public void resetCurrentBox() { this.currentBoxX = 0; this.currentBoxY = 0; }

    public int getMaxBoxXPos() { return maxBoxXPos; }
    public void setMaxBoxXPos(int v) { this.maxBoxXPos = v; }
    public int getMaxBoxXNeg() { return maxBoxXNeg; }
    public void setMaxBoxXNeg(int v) { this.maxBoxXNeg = v; }
    public int getMaxBoxYPos() { return maxBoxYPos; }
    public void setMaxBoxYPos(int v) { this.maxBoxYPos = v; }
    public int getMaxBoxYNeg() { return maxBoxYNeg; }
    public void setMaxBoxYNeg(int v) { this.maxBoxYNeg = v; }
}