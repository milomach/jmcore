package com.jmcore.core.input.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import com.jmcore.core.input.InputEventBus;
import com.jmcore.core.input.events.SprintInputEvent;

public class SprintInputListener implements Listener {
    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        InputEventBus.post(new SprintInputEvent(event.getPlayer(), event.isSprinting()));
    }
}