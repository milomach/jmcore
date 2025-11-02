package com.jmcore.core.util.display_utils;

import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import java.util.Set;

/**
 * General utility for setting interpolation properties on Display entities.
 * Supports both single Entity and Set<Entity>.
 */
public class InterpolationUtil {

    /**
     * Sets the interpolation delay for a single Display entity.
     */
    public static void setInterpolationDelay(Entity entity, int delay) {
        if (entity instanceof Display display) {
            display.setInterpolationDelay(delay);
        }
    }

    /**
     * Sets the interpolation delay for a set of Display entities.
     */
    public static void setInterpolationDelay(Set<Entity> entities, int delay) {
        for (Entity e : entities) {
            setInterpolationDelay(e, delay);
        }
    }

    /**
     * Sets the interpolation duration for a single Display entity.
     */
    public static void setInterpolationDuration(Entity entity, int duration) {
        if (entity instanceof Display display) {
            display.setInterpolationDuration(duration);
        }
    }

    /**
     * Sets the interpolation duration for a set of Display entities.
     */
    public static void setInterpolationDuration(Set<Entity> entities, int duration) {
        for (Entity e : entities) {
            setInterpolationDuration(e, duration);
        }
    }

    /**
     * Sets the teleport duration for a single Display entity.
     */
    public static void setTeleportDuration(Entity entity, int duration) {
        if (entity instanceof Display display) {
            display.setTeleportDuration(duration);
        }
    }

    /**
     * Sets the teleport duration for a set of Display entities.
     */
    public static void setTeleportDuration(Set<Entity> entities, int duration) {
        for (Entity e : entities) {
            setTeleportDuration(e, duration);
        }
    }
}