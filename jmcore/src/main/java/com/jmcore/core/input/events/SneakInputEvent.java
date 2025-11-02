package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

public class SneakInputEvent extends PluginInputEvent {
    private final boolean sneaking;

    public SneakInputEvent(Player player, boolean sneaking) {
        super(player);
        this.sneaking = sneaking;
    }

    public boolean isSneaking() {
        return sneaking;
    }
}