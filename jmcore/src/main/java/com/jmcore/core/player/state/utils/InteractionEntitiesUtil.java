package com.jmcore.core.player.state.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.component.PlayerStateData;

import java.util.UUID;

/**
 * Utility for setting up interaction entities for a player.
 * - Summons a spacer entity and makes it ride the player.
 * - Summons a target entity, makes it ride the spacer, and sets it responsive.
 * - Stores the UUIDs in PlayerData.
 * - Makes both entities visible only to the corresponding player.
 */
public class InteractionEntitiesUtil {

    /**
     * Sets up the spacer and target interaction entities for the player.
     * Removes any existing ones first.
     * After spawning, shows each entity only to the player.
     * @param plugin Your plugin instance (JavaPlugin)
     */
    public static void setup(Plugin plugin, Player player, PlayerData data) {
        PlayerStateData playerState = data.getComponent(PlayerStateData.class);
        if (playerState == null) return;
        
        // Remove any existing interaction entities first
        InteractionEntitiesCleanupUtil.cleanup(data);

        World world = player.getWorld();
        Location baseLoc = player.getLocation().add(0, 0.1, 0);

        // Summon spacer and make it ride the player
        Interaction spacer = world.spawn(baseLoc, Interaction.class, e -> {
            e.setInteractionWidth((float) playerState.getSpacerWidth());
            e.setInteractionHeight((float) playerState.getSpacerHeight());
            e.setVisibleByDefault(false); // Hide from everyone by default
        });
        player.addPassenger(spacer);
        playerState.setSpacerEntityUUID(spacer.getUniqueId());

        // Show spacer only to the corresponding player
        player.showEntity(plugin, spacer);

        // Summon target, set responsive, and make it ride the spacer
        Location targetLoc = baseLoc.clone().add(0, playerState.getSpacerHeight(), 0);
        Interaction target = world.spawn(targetLoc, Interaction.class, e -> {
            e.setInteractionWidth((float) playerState.getTargetWidth());
            e.setInteractionHeight((float) playerState.getTargetHeight());
            e.setVisibleByDefault(false); // Hide from everyone by default
            e.setResponsive(true);
        });
        spacer.addPassenger(target);
        playerState.setTargetEntityUUID(target.getUniqueId());

        // Show target only to the corresponding player
        player.showEntity(plugin, target);
    }

    /**
     * Gets the current spacer entity for the player, or null if not found.
     */
    public static Interaction getSpacer(PlayerData data) {
        PlayerStateData playerState = data.getComponent(PlayerStateData.class);
        if (playerState == null) return null;

        UUID uuid = playerState.getSpacerEntityUUID();
        if (uuid == null) return null;
        var entity = Bukkit.getEntity(uuid);
        return (entity instanceof Interaction i && i.isValid()) ? i : null;
    }

    /**
     * Gets the current target entity for the player, or null if not found.
     */
    public static Interaction getTarget(PlayerData data) {
        PlayerStateData playerState = data.getComponent(PlayerStateData.class);
        if (playerState == null) return null;
        
        UUID uuid = playerState.getTargetEntityUUID();
        if (uuid == null) return null;
        var entity = Bukkit.getEntity(uuid);
        return (entity instanceof Interaction i && i.isValid()) ? i : null;
    }
}