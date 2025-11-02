package com.jmcore.core.util.display_utils;

import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import java.util.Set;

public class BrightnessUtil {
    public enum BrightnessMode { CUSTOM, AMBIENT }

    public static void setBrightness(Entity entity, BrightnessMode mode, Integer block, Integer sky) {
        if (entity instanceof Display display) {
            if (mode == BrightnessMode.CUSTOM && block != null && sky != null) {
                display.setBrightness(new Display.Brightness(block, sky));
            } else if (mode == BrightnessMode.AMBIENT) {
                display.setBrightness(null);
            }
        }
    }

    public static void setBrightness(Set<Entity> entities, BrightnessMode mode, Integer block, Integer sky) {
        for (Entity e : entities) {
            setBrightness(e, mode, block, sky);
        }
    }
}