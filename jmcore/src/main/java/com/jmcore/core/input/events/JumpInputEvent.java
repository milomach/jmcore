package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

/**
 * Plugin event for when a player jumps.
 */
public class JumpInputEvent extends PluginInputEvent {
    public JumpInputEvent(Player player) {
        super(player);
    }
}