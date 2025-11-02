package com.jmcore.core.input.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.jmcore.core.input.InputEventBus;
import com.jmcore.core.input.events.InventoryClickInputEvent;

public class InventoryClickInputListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InputEventBus.post(new InventoryClickInputEvent(
            player,
            event.getClickedInventory(),
            event.getSlot(),
            event.getClick()
        ));
    }
}