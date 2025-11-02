package com.jmcore.core.util.display_utils.item;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

/**
 * Utility for toggling the enchantment glint (visual "enchanted" effect)
 * on ItemDisplay entities. Supports both single Entity and Set<Entity>.
 */
public final class EnchantUtil {

    private EnchantUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Toggles the enchantment glint on a single ItemDisplay entity.
     *
     * @param entity The target entity (must be ItemDisplay).
     * @param enabled True to force glint, false to suppress, null to clear override.
     */
    public static void setEnchantedGlint(Entity entity, Boolean enabled) {
        if (!(entity instanceof ItemDisplay display)) {
            return;
        }

        ItemStack stack = display.getItemStack();
        if (stack == null) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        meta.setEnchantmentGlintOverride(enabled);
        stack.setItemMeta(meta);
        display.setItemStack(stack);
    }

    /**
     * Applies the enchantment glint toggle to a set of entities.
     *
     * @param entities Set of entities to process.
     * @param enabled True to force glint, false to suppress, null to clear override.
     */
    public static void setEnchantedGlint(Set<Entity> entities, Boolean enabled) {
        for (Entity e : entities) {
            setEnchantedGlint(e, enabled);
        }
    }
}
