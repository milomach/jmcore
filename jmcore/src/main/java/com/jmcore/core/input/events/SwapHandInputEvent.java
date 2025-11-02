package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

public class SwapHandInputEvent extends PluginInputEvent {
    public SwapHandInputEvent(Player player) {
        super(player);
    }
}