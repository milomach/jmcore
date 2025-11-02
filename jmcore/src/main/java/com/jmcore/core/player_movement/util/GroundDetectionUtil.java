package com.jmcore.core.player_movement.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Utility for efficiently checking if a player is on the ground using a downward raycast.
 */
public class GroundDetectionUtil {

    /**
     * Checks if the player is on the ground by raycasting down from their feet.
     * This is a loose check for performance and reliability.
     *
     * @param player The player to check.
     * @return true if there is a solid block within a small distance below the player.
     */
    public static boolean isOnGround(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return false;

        // Start just below the player's feet (Y - 0.01 to avoid floating point issues)
        double startY = loc.getY() - 0.01;
        // Check a few increments down (e.g., up to 0.3 blocks below feet)
        double maxDistance = 0.3;
        double step = 0.05;

        for (double y = startY; y >= startY - maxDistance; y -= step) {
            Location checkLoc = new Location(world, loc.getX(), y, loc.getZ());
            Material mat = checkLoc.getBlock().getType();
            if (mat.isSolid() && mat != Material.AIR) {
                return true;
            }
        }
        return false;
    }
}