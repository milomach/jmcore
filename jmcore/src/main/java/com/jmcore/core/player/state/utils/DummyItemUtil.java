package com.jmcore.core.player.state.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.key.Key;



/**
 * Utility for placing and removing dummy items in all relevant slots.
 */
public class DummyItemUtil {
    // Slot mappings for hotbar, inventory, offhand, and equipment
    private static final int HOTBAR_START = 0, HOTBAR_END = 8;
    private static final int INV_START = 9, INV_END = 35;
    private static final int OFFHAND_SLOT = 40;
    private static final int[] EQUIP_SLOTS = {36, 37, 38, 39}; // boots, leggings, chestplate, helmet

    // Use "slot" for persistent data key
    private static final NamespacedKey SLOT_KEY = new NamespacedKey(JavaPlugin.getProvidingPlugin(DummyItemUtil.class), "slot");

    /**
     * Places dummy items in all hotbar, inventory, offhand, and equipment slots.
     */
    public static void placeAllDummyItems(Player player) {
        for (int slot = HOTBAR_START; slot <= HOTBAR_END; slot++) {
            setDummyItem(player, slot);
        }
        for (int slot = INV_START; slot <= INV_END; slot++) {
            setDummyItem(player, slot);
        }
        setDummyItem(player, OFFHAND_SLOT);
        for (int slot : EQUIP_SLOTS) {
            setDummyItem(player, slot);
        }
    }

    /**
     * Removes dummy items from all hotbar, inventory, offhand, and equipment slots.
     */
    public static void removeAllDummyItems(Player player) {
        for (int slot = HOTBAR_START; slot <= HOTBAR_END; slot++) {
            clearSlot(player, slot);
        }
        for (int slot = INV_START; slot <= INV_END; slot++) {
            clearSlot(player, slot);
        }
        clearSlot(player, OFFHAND_SLOT);
        for (int slot : EQUIP_SLOTS) {
            clearSlot(player, slot);
        }
    }

    /**
     * Sets a dummy item in the specified slot using the Bukkit API and Paper Data Components.
     * The item is a totem of undying with persistent data key "slot" set to the slot index.
     */
    public static void setDummyItem(Player player, int slot) {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING, 1);

        // Set the item model
        item.setData(DataComponentTypes.ITEM_MODEL,
            Key.key("minecraft", "blank")
        );

        // Set the item name
        item.setData(DataComponentTypes.ITEM_NAME,
            Component.text("")
        );
        
        // Hide the tooltip
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY,
            TooltipDisplay.tooltipDisplay()
                .hideTooltip(true)
                .build()
        );

        // Remove death protection
        item.unsetData(DataComponentTypes.DEATH_PROTECTION);


        // 4. Set persistent data for slot (Bukkit API)
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(SLOT_KEY, PersistentDataType.INTEGER, slot);
            item.setItemMeta(meta);
        }

        player.getInventory().setItem(slot, item);
    }

    /**
     * Clears the specified slot using the Bukkit API.
     */
    private static void clearSlot(Player player, int slot) {
        player.getInventory().setItem(slot, null);
    }

    /**
     * Checks if the given item is a dummy item for the specified slot.
     * This checks for a totem of undying with persistent data key "slot" matching the slot index.
     */
    public static boolean isDummyItem(ItemStack item, int slot) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Integer slotData = meta.getPersistentDataContainer().get(SLOT_KEY, PersistentDataType.INTEGER);
        return slotData != null && slotData == slot;
    }
}