package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

/**
 * Fired when a new interaction is detected on the target interaction entity.
 */
public class InteractInputEvent extends PluginInputEvent {
    public InteractInputEvent(Player player) {
        super(player);
    }
}