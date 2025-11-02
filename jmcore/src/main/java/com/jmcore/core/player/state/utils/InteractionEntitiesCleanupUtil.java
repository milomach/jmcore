package com.jmcore.core.player.state.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.component.PlayerStateData;

import java.util.UUID;

/**
 * Utility for cleaning up interaction entities for a player.
 */
public class InteractionEntitiesCleanupUtil {

    /**
     * Removes the spacer and target interaction entities for the player, if present.
     * Clears the UUIDs from PlayerData.
     */
    public static void cleanup(PlayerData data) {
        PlayerStateData playerState = data.getComponent(PlayerStateData.class);
        if (playerState == null) return;
        // Remove spacer
        UUID spacerUUID = playerState.getSpacerEntityUUID();
        if (spacerUUID != null) {
            Entity e = Bukkit.getEntity(spacerUUID);
            if (e != null) e.remove();
            playerState.setSpacerEntityUUID(null);
        }
        // Remove target
        UUID targetUUID = playerState.getTargetEntityUUID();
        if (targetUUID != null) {
            Entity e = Bukkit.getEntity(targetUUID);
            if (e != null) e.remove();
            playerState.setTargetEntityUUID(null);
        }
    }

    /**
     * Overload for convenience if you have a Player object.
     */
    public static void cleanup(org.bukkit.entity.Player player, PlayerData data) {
        cleanup(data);
    }
}