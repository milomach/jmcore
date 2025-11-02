package com.jmcore.core.util.display_utils.text;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.Set;

/**
 * Utility for modifying whether or not TextDisplay entities are shadowed.
 * Supports both single Entity and Set<Entity>.
 */
public class ShadowedUtil {
    /**
     * Sets shadowed for a single TextDisplay entity.
     */
    public static void setShadowed(Entity entity, boolean shadowed) {
        if (entity instanceof TextDisplay textDisplay) {
            textDisplay.setShadowed(shadowed);
        }
    }

    /**
     * Sets shadowed for a set of TextDisplay entities.
     */
    public static void setShadowed(Set<Entity> entities, boolean shadowed) {
        for (Entity e : entities) {
            setShadowed(e, shadowed);
        }
    }
}