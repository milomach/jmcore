package com.jmcore.core.player.state.managers;

import org.bukkit.entity.Player;

import com.jmcore.core.util.VanillaMovementUtil;

/**
 * Handles setup actions related to player movement.
 * Currently, this locks vanilla movement for the provided player.
 */
public class MovementSetup {

    /**
     * Locks vanilla movement for the specified player.
     * This disables walking, flying, and jumping.
     *
     * @param player The player whose movement should be locked.
     */
    public static void setup(Player player) {
        // Lock vanilla movement using the utility method.
        VanillaMovementUtil.lockVanillaMovement(player);
    }
}