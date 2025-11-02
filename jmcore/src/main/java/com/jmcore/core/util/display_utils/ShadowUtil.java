package com.jmcore.core.util.display_utils;

import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import java.util.Set;

public class ShadowUtil {
    public static void setShadowRadius(Entity entity, float radius) {
        if (entity instanceof Display display) {
            display.setShadowRadius(radius);
        }
    }

    public static void setShadowRadius(Set<Entity> entities, float radius) {
        for (Entity e : entities) {
            setShadowRadius(e, radius);
        }
    }

    public static void setShadowStrength(Entity entity, float strength) {
        if (entity instanceof Display display) {
            display.setShadowStrength(strength);
        }
    }

    public static void setShadowStrength(Set<Entity> entities, float strength) {
        for (Entity e : entities) {
            setShadowStrength(e, strength);
        }
    }
}