package com.jmcore.core.player.state.managers;

import org.bukkit.entity.Player;

import com.jmcore.core.util.VanillaMovementUtil;

/**
 * Handles cleanup actions related to player movement.
 * Currently, this unlocks vanilla movement for the provided player.
 */
public class MovementCleanup {

    /**
     * Unlocks vanilla movement for the specified player.
     * This restores walking, flying, and jumping to default values.
     *
     * @param player The player whose movement should be unlocked.
     */
    public static void cleanup(Player player) {
        // Unlock vanilla movement using the utility method.
        VanillaMovementUtil.unlockVanillaMovement(player);
    }
}