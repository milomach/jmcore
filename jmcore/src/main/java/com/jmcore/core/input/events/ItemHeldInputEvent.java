package com.jmcore.core.input.events;

import org.bukkit.entity.Player;

import com.jmcore.core.input.PluginInputEvent;

public class ItemHeldInputEvent extends PluginInputEvent {
    private final int previousSlot;
    private final int newSlot;

    public ItemHeldInputEvent(Player player, int previousSlot, int newSlot) {
        super(player);
        this.previousSlot = previousSlot;
        this.newSlot = newSlot;
    }

    public int getPreviousSlot() {
        return previousSlot;
    }

    public int getNewSlot() {
        return newSlot;
    }
}