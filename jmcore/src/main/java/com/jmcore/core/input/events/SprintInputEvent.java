package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

public class SprintInputEvent extends PluginInputEvent {
    private final boolean sprinting;

    public SprintInputEvent(Player player, boolean sprinting) {
        super(player);
        this.sprinting = sprinting;
    }

    public boolean isSprinting() {
        return sprinting;
    }
}