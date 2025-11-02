package com.jmcore.core.util.display_utils.text;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.Set;

/**
 * Utility for modifying whether or not TextDisplay entities are see-through.
 * Supports both single Entity and Set<Entity>.
 */
public class SeeThroughUtil {
    /**
     * Sets see-through for a single TextDisplay entity.
     */
    public static void setSeeThrough(Entity entity, boolean seeThrough) {
        if (entity instanceof TextDisplay textDisplay) {
            textDisplay.setSeeThrough(seeThrough);
        }
    }

    /**
     * Sets see-through for a set of TextDisplay entities.
     */
    public static void setSeeThrough(Set<Entity> entities, boolean seeThrough) {
        for (Entity e : entities) {
            setSeeThrough(e, seeThrough);
        }
    }
}