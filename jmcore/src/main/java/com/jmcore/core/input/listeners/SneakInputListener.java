package com.jmcore.core.input.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.jmcore.core.input.InputEventBus;
import com.jmcore.core.input.events.SneakInputEvent;

public class SneakInputListener implements Listener {
    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        InputEventBus.post(new SneakInputEvent(event.getPlayer(), event.isSneaking()));
    }
}