package com.jmcore.core.input.events;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import com.jmcore.core.input.PluginInputEvent;

public class InventoryClickInputEvent extends PluginInputEvent {
    private final Inventory clickedInventory;
    private final int slot;
    private final ClickType clickType;

    public InventoryClickInputEvent(Player player, Inventory clickedInventory, int slot, ClickType clickType) {
        super(player);
        this.clickedInventory = clickedInventory;
        this.slot = slot;
        this.clickType = clickType;
    }

    public Inventory getClickedInventory() {
        return clickedInventory;
    }

    public int getSlot() {
        return slot;
    }

    public ClickType getClickType() {
        return clickType;
    }
}