package com.jmcore.core.util.display_utils;

import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import com.jmcore.core.util.ARGBUtil;

import java.util.Set;

public class GlowUtil {
    public enum GlowMode { CUSTOM, NONE }

    /**
     * Sets glow color override for a single entity.
     */
    public static void setGlowColorOverride(Entity entity, GlowMode mode, Integer argb) {
        if (entity instanceof Display display) {
            if (mode == GlowMode.CUSTOM && argb != null) {
                Color color = ARGBUtil.fromARGB(argb);
                display.setGlowColorOverride(color);
            } else if (mode == GlowMode.NONE) {
                display.setGlowColorOverride(null);
            }
        }
    }

    /**
     * Sets glow color override for a set of entities.
     */
    public static void setGlowColorOverride(Set<Entity> entities, GlowMode mode, Integer argb) {
        for (Entity e : entities) {
            setGlowColorOverride(e, mode, argb);
        }
    }
}