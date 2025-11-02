package com.jmcore.core.player.state.managers;

import org.bukkit.entity.Player;

/**
 * Handles general setup actions for a player.
 * Currently, this makes the player invisible.
 */
public class GeneralSetup {

    /**
     * Makes the specified player invisible.
     *
     * @param player The player to make invisible.
     */
    public static void setup(Player player) {
        // Set the player invisible using the Bukkit API.
        player.setInvisible(true);
    }
}