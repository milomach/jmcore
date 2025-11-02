package com.jmcore.core.input.listeners;

// Import the Paper event, not the Bukkit one!
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.jmcore.core.input.InputEventBus;
import com.jmcore.core.input.events.JumpInputEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for Paper's PlayerJumpEvent and posts a plugin jump input event.
 * Requires PaperMC server (not just Bukkit/Spigot).
 */
public class JumpInputListener implements Listener {
    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        // Post to the plugin's internal input event bus
        InputEventBus.post(new JumpInputEvent(event.getPlayer()));
    }
}