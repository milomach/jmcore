package com.jmcore.core.util.display_utils.text;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.Set;

/**
 * Utility for modifying the opacity of TextDisplay entities.
 * Supports both single Entity and Set<Entity>.
 */
public class OpacityUtil {
    /**
     * Sets text opacity for a single TextDisplay entity.
     */
    public static void setTextOpacity(Entity entity, int opacity) {
        if (entity instanceof TextDisplay textDisplay) {
            textDisplay.setTextOpacity((byte) opacity);
        }
    }

    /**
     * Sets text opacity for a set of TextDisplay entities.
     */
    public static void setTextOpacity(Set<Entity> entities, int opacity) {
        for (Entity e : entities) {
            setTextOpacity(e, opacity);
        }
    }
}