package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

public class DropItemInputEvent extends PluginInputEvent {
    public DropItemInputEvent(Player player) {
        super(player);
    }
}