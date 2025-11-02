package com.jmcore.core.util.display_utils;

import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import java.util.Set;

public class ViewRangeUtil {
    public static void setViewRange(Entity entity, float viewRange) {
        if (entity instanceof Display display) {
            display.setViewRange(viewRange);
        }
    }

    public static void setViewRange(Set<Entity> entities, float viewRange) {
        for (Entity e : entities) {
            setViewRange(e, viewRange);
        }
    }
}