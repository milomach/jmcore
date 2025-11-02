package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

/**
 * Fired when a new attack is detected on the target interaction entity.
 */
public class AttackInputEvent extends PluginInputEvent {
    public AttackInputEvent(Player player) {
        super(player);
    }
}