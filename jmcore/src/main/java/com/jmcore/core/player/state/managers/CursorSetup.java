package com.jmcore.core.player.state.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.PlayerStateData;
import com.jmcore.core.player.state.utils.CameraDistanceUtil;
import com.jmcore.core.player.state.utils.DummyItemUtil;
import com.jmcore.core.player.state.utils.InteractionEntitiesUtil;

/**
 * Handles all setup actions needed when enabling GUI cursor for a player.
 */
public class CursorSetup {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;
    private final CursorMaintenance cursorMaintenance;

    public CursorSetup(Plugin plugin, PlayerDataManager playerDataManager, CursorMaintenance cursorMaintenance) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.cursorMaintenance = cursorMaintenance;
    }

    public void setup(Player player) {
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        PlayerStateData playerState = data.getComponent(PlayerStateData.class);
        if (playerState == null) return;

        // Save original view direction if not already saved
        if (playerState.getOriginalYaw() == null || playerState.getOriginalPitch() == null) {
            Location loc = player.getLocation();
            playerState.saveOriginalView(loc.getYaw(), loc.getPitch());
        }

        // Save and set camera_distance attribute base to zero using CameraDistanceUtil
        if (playerState.getOriginalCameraDistance() == null) {
            playerState.saveOriginalCameraDistance(4.0);
            CameraDistanceUtil.setCameraDistance(player, 0.0);
        }

        // Place dummy items in all relevant slots
        DummyItemUtil.placeAllDummyItems(player);

        // Setup interaction entities
        InteractionEntitiesUtil.setup(plugin, player, data);

        // Start maintenance (event listeners, periodic checks)
        cursorMaintenance.start(player);
    }
}