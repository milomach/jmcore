package com.jmcore.core.util.display_utils;

import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import java.util.Set;

public class CullingUtil {
    public static void setDisplayWidth(Entity entity, float width) {
        if (entity instanceof Display display) {
            display.setDisplayWidth(width);
        }
    }

    public static void setDisplayWidth(Set<Entity> entities, float width) {
        for (Entity e : entities) {
            setDisplayWidth(e, width);
        }
    }

    public static void setDisplayHeight(Entity entity, float height) {
        if (entity instanceof Display display) {
            display.setDisplayHeight(height);
        }
    }

    public static void setDisplayHeight(Set<Entity> entities, float height) {
        for (Entity e : entities) {
            setDisplayHeight(e, height);
        }
    }
}