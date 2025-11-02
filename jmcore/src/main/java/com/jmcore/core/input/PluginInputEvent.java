package com.jmcore.core.input;

import org.bukkit.entity.Player;

/**
 * Base class for all plugin input events.
 */
public abstract class PluginInputEvent {
    private final Player player;
    private final long timestamp;

    public PluginInputEvent(Player player) {
        this.player = player;
        this.timestamp = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public long getTimestamp() {
        return timestamp;
    }
}