package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

public class AnvilInputEvent extends PluginInputEvent {
    private final String text;

    public AnvilInputEvent(Player player, String text) {
        super(player);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}