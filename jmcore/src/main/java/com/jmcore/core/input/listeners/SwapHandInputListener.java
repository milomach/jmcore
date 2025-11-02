package com.jmcore.core.input.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import com.jmcore.core.input.InputEventBus;
import com.jmcore.core.input.events.SwapHandInputEvent;

public class SwapHandInputListener implements Listener {
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        InputEventBus.post(new SwapHandInputEvent(event.getPlayer()));
    }
}