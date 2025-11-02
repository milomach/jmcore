package com.jmcore.core.util.display_utils.item;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Utility for setting the item of ItemDisplay entities.
 * Supports both single Entity and Set<Entity> overloads.
 */
public class ItemUtil {
    /**
     * Sets the item for a single ItemDisplay entity.
     */
    public static void setItem(Entity entity, String itemName) {
        if (entity instanceof ItemDisplay itemDisplay) {
            Material mat = Material.matchMaterial(itemName);
            if (mat != null) {
                itemDisplay.setItemStack(new ItemStack(mat));
            }
        }
    }

    /**
     * Sets the item for a set of ItemDisplay entities.
     */
    public static void setItem(Set<Entity> entities, String itemName) {
        for (Entity e : entities) {
            setItem(e, itemName);
        }
    }
}