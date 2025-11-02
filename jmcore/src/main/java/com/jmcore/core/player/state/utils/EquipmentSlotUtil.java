package com.jmcore.core.player.state.utils;

import org.bukkit.inventory.EquipmentSlot;

/**
 * Utility for mapping EquipmentSlot to raw inventory slot indices.
 */
public class EquipmentSlotUtil {
    /**
     * Maps an EquipmentSlot to the corresponding raw inventory slot index.
     * Returns -1 if the slot is not an armor or offhand slot.
     *
     * @param equipmentSlot the EquipmentSlot to map
     * @return the raw inventory slot index, or -1 if not applicable
     */
    public static int toInventorySlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> 39;
            case CHEST -> 38;
            case LEGS -> 37;
            case FEET -> 36;
            case OFF_HAND -> 40;
            default -> -1; // Not an armor/offhand slot
        };
    }
}