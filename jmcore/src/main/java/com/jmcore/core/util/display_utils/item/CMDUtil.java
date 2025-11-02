package com.jmcore.core.util.display_utils.item;

import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility for setting custom model data (strings, floats, flags, colors)
 * on ItemDisplay entities. Supports single Entity and Set<Entity>.
 */
public final class CMDUtil {

    private CMDUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public enum CmdType {
        STRING, FLOAT, FLAG, COLOR
    }

    /**
     * Sets the custom model data for a single Entity if it is an ItemDisplay.
     *
     * @param entity the entity to apply to
     * @param type   the type of model data
     * @param index  index within the list
     * @param value  the value to set
     */
    public static void setCustomModelData(Entity entity, CmdType type, int index, Object value) {
        if (!(entity instanceof ItemDisplay display)) {
            return;
        }
        applyToDisplay(display, type, index, value);
    }

    /**
     * Sets the custom model data for a set of entities, applying only to ItemDisplays.
     *
     * @param entities the set of entities to process
     * @param type     the type of model data
     * @param index    index within the list
     * @param value    the value to set
     */
    public static void setCustomModelData(Set<Entity> entities, CmdType type, int index, Object value) {
        for (Entity entity : entities) {
            setCustomModelData(entity, type, index, value);
        }
    }

    private static void applyToDisplay(ItemDisplay display, CmdType type, int index, Object value) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be non-negative, got: " + index);
        }

        ItemStack stack = display.getItemStack();
        if (stack == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();

        switch (type) {
            case STRING -> updateList(new ArrayList<>(cmd.getStrings()), index, (String) value, cmd::setStrings);
            case FLOAT -> updateList(new ArrayList<>(cmd.getFloats()), index, (Float) value, cmd::setFloats);
            case FLAG -> updateList(new ArrayList<>(cmd.getFlags()), index, (Boolean) value, cmd::setFlags);
            case COLOR -> updateList(new ArrayList<>(cmd.getColors()), index, (Color) value, cmd::setColors);
            default -> throw new UnsupportedOperationException("Unknown CmdType: " + type);
        }

        meta.setCustomModelDataComponent(cmd);
        stack.setItemMeta(meta);
        display.setItemStack(stack);
    }

    @FunctionalInterface
    private interface Setter<T> {
        void set(List<T> list);
    }

    private static <T> void updateList(List<T> list, int index, T value, Setter<T> setter) {
        while (list.size() <= index) {
            list.add(null); // placeholder
        }
        list.set(index, value);
        setter.set(list);
    }
}