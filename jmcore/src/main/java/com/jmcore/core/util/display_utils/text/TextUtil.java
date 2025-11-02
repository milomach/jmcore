package com.jmcore.core.util.display_utils.text;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import net.kyori.adventure.text.Component;

import java.util.Set;

/**
 * Utility for setting the text of TextDisplay entities.
 * Supports both single Entity and Set<Entity> overloads.
 */
public class TextUtil {
    /**
     * Sets the text for a single TextDisplay entity.
     * Uses Adventure Component API (non-deprecated).
     */
    public static void setText(Entity entity, String text) {
        if (entity instanceof TextDisplay textDisplay) {
            textDisplay.text(Component.text(text));
        }
    }

    /**
     * Sets the text for a set of TextDisplay entities.
     */
    public static void setText(Set<Entity> entities, String text) {
        for (Entity e : entities) {
            setText(e, text);
        }
    }
}