package com.jmcore.core.util.display_utils.text;

import java.util.Set;

import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import com.jmcore.core.util.ARGBUtil;

/**
 * Utility for modifying the background of TextDisplay entities.
 * Supports both single Entity and Set<Entity>.
 */
public class BackgroundUtil {
    /**
     * Sets background for a single TextDisplay entity.
     * @param entity The entity to modify.
     * @param option "custom" or "default"
     * @param argb ARGB color (required for custom)
     */
    public static void setBackground(Entity entity, String option, Integer argb) {
        if (entity instanceof TextDisplay textDisplay) {
            if ("custom".equalsIgnoreCase(option) && argb != null) {
                textDisplay.setDefaultBackground(false);
                Color color = ARGBUtil.fromARGB(argb);
                textDisplay.setBackgroundColor(color);
            } else if ("default".equalsIgnoreCase(option)) {
                textDisplay.setDefaultBackground(true);
            }
        }
    }

    /**
     * Sets background for a set of TextDisplay entities.
     * @param entities The entities to modify.
     * @param option "custom" or "default"
     * @param argb ARGB color (required for custom)
     */
    public static void setBackground(Set<Entity> entities, String option, Integer argb) {
        for (Entity e : entities) {
            setBackground(e, option, argb);
        }
    }
}
