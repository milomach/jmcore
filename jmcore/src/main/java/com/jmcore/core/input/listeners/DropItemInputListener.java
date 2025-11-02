package com.jmcore.core.input.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.jmcore.core.input.InputEventBus;
import com.jmcore.core.input.events.DropItemInputEvent;

public class DropItemInputListener implements Listener {
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        InputEventBus.post(new DropItemInputEvent(event.getPlayer()));
    }
}