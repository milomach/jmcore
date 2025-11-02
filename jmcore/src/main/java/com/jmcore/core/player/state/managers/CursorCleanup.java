package com.jmcore.core.player.state.managers;

import org.bukkit.entity.Player;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.PlayerStateData;
import com.jmcore.core.player.state.utils.CameraDistanceUtil;
import com.jmcore.core.player.state.utils.DummyItemUtil;
import com.jmcore.core.player.state.utils.InteractionEntitiesCleanupUtil;

/**
 * Handles all cleanup actions needed when disabling GUI cursor for a player.
 */
public class CursorCleanup {
    private final PlayerDataManager playerDataManager;
    private final CursorMaintenance cursorMaintenance;

    public CursorCleanup(PlayerDataManager playerDataManager, CursorMaintenance cursorMaintenance) {
        this.playerDataManager = playerDataManager;
        this.cursorMaintenance = cursorMaintenance;
    }

    public void cleanup(Player player) {
        // Stop maintenance (event listeners, periodic checks)
        cursorMaintenance.stop(player);

        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        PlayerStateData playerState = data.getComponent(PlayerStateData.class);
        if (playerState == null) return;

        // Restore view direction if saved
        if (playerState.getOriginalYaw() != null && playerState.getOriginalPitch() != null) {
            player.setRotation(playerState.getOriginalYaw(), playerState.getOriginalPitch());
            playerState.clearOriginalView();
        }

        // Restore camera_distance attribute base if saved
        if (playerState.getOriginalCameraDistance() != null) {
            CameraDistanceUtil.setCameraDistance(player, playerState.getOriginalCameraDistance());
            playerState.clearOriginalCameraDistance();
        }

        // Remove dummy items from all slots
        DummyItemUtil.removeAllDummyItems(player);

        // Remove interaction entities
        InteractionEntitiesCleanupUtil.cleanup(data);
    }
}