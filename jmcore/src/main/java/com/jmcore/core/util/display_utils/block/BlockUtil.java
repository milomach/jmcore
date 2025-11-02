package com.jmcore.core.util.display_utils.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;

import java.util.Set;

/**
 * Utility for setting the block of BlockDisplay entities.
 * Supports both single Entity and Set<Entity> overloads.
 */
public class BlockUtil {
    /**
     * Sets the block for a single BlockDisplay entity.
     */
    public static void setBlock(Entity entity, String blockName) {
        if (entity instanceof BlockDisplay blockDisplay) {
            Material mat = Material.matchMaterial(blockName);
            if (mat != null) {
                BlockData data = mat.createBlockData();
                blockDisplay.setBlock(data);
            }
        }
    }

    /**
     * Sets the block for a set of BlockDisplay entities.
     */
    public static void setBlock(Set<Entity> entities, String blockName) {
        for (Entity e : entities) {
            setBlock(e, blockName);
        }
    }
}