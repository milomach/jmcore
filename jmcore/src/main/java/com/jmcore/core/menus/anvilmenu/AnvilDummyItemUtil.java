package com.jmcore.core.menus.anvilmenu;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.key.Key;

public class AnvilDummyItemUtil {
    // Use "anvil_slot" for persistent data key
    private static final NamespacedKey SLOT_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(AnvilDummyItemUtil.class), "anvil_slot");

    /**
     * Creates a dummy item for the anvil slot.
     * For slot 0, the name is set to the provided text; for others, it's blank.
     */
    public static ItemStack createDummyItem(int slot, String name) {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING, 1);

        // Set the item model
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft", "blank"));

        // Set the item name
        item.setData(DataComponentTypes.ITEM_NAME, Component.text(name == null ? "" : name));

        // Hide the tooltip
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY,
            TooltipDisplay.tooltipDisplay().hideTooltip(true).build()
        );

        // Remove death protection
        item.unsetData(DataComponentTypes.DEATH_PROTECTION);

        // Set persistent data for slot
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(SLOT_KEY, PersistentDataType.INTEGER, slot);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Checks if the given item is a dummy item for the specified anvil slot.
     */
    public static boolean isDummyItem(ItemStack item, int slot) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Integer slotData = meta.getPersistentDataContainer().get(SLOT_KEY, PersistentDataType.INTEGER);
        return slotData != null && slotData == slot;
    }
}