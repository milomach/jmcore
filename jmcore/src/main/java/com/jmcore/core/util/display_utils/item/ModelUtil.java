package com.jmcore.core.util.display_utils.item;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import io.papermc.paper.datacomponent.DataComponentTypes;

/**
 * Utility for setting the ITEM_MODEL component on ItemDisplay entities.
 * Supports both single Entity and Set<Entity> overloads.
 */
public final class ModelUtil {

    private ModelUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Sets the item model component for a single ItemDisplay entity.
     * Ensures the entity is an ItemDisplay, not others like TextDisplay.
     *
     * @param entity The target entity
     * @param modelPath The item model path, e.g., "namespace:path/to/model"
     */
    public static void setItemModel(Entity entity, String modelPath) {
        if (!(entity instanceof ItemDisplay)) {
            return;
        }

        ItemDisplay display = (ItemDisplay) entity;
        ItemStack stack = display.getItemStack();
        if (stack == null) return;

        // Build the NamespacedKey from the input string
        NamespacedKey key = NamespacedKey.fromString(modelPath);

        stack.setData(DataComponentTypes.ITEM_MODEL, key);
        display.setItemStack(stack);
    }
}
