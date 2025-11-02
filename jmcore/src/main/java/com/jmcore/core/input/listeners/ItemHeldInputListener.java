package com.jmcore.core.input.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.jmcore.core.input.InputEventBus;
import com.jmcore.core.input.events.ItemHeldInputEvent;

public class ItemHeldInputListener implements Listener {
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        InputEventBus.post(new ItemHeldInputEvent(
            event.getPlayer(),
            event.getPreviousSlot(),
            event.getNewSlot()
        ));
    }
}