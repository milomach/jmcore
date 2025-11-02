package com.jmcore.core.player.state.managers;

import org.bukkit.entity.Player;

/**
 * Handles general cleanup actions for a player.
 * Currently, this makes the player visible.
 */
public class GeneralCleanup {

    /**
     * Makes the specified player visible.
     *
     * @param player The player to make visible.
     */
    public static void cleanup(Player player) {
        // Set the player visible using the Bukkit API.
        player.setInvisible(false);
    }
}