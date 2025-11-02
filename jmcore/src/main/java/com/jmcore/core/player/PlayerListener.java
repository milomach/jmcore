package com.jmcore.core.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJRigCleanupUtil;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.AJRigInstance.AJRigScope;
import com.jmcore.core.cursor.HeadRotationTracker;
import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.GeneralData;
import com.jmcore.core.player.state.managers.CursorCleanup;

import java.util.Collection;

/**
 * Handles player join and quit events.
 * - On join: ensures PlayerData is initialized and uniqueId is generated.
 * - On quit: disables head tracking, cleans up player state, and removes PlayerData.
 */
public class PlayerListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final HeadRotationTracker tracker;
    private final CursorCleanup cursorCleanup;
    private final AJRigManager ajRigManager;
    private final Plugin plugin;

    public PlayerListener(PlayerDataManager playerDataManager, HeadRotationTracker tracker, CursorCleanup cursorCleanup, AJRigManager ajRigManager, Plugin plugin) {
        this.playerDataManager = playerDataManager;
        this.tracker = tracker;
        this.cursorCleanup = cursorCleanup;
        this.ajRigManager = ajRigManager;
        this.plugin = plugin;
    }

    /**
     * Called when a player joins the server.
     * Ensures PlayerData and GeneralData are initialized, and generates a unique short identifier.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;

        // Ensure GeneralData is present and uniqueId is generated
        GeneralData general = data.getComponent(GeneralData.class);
        if (general != null) {
            general.regenerateUniqueId(); // Regenerate in case of migration or future changes
            // Optionally log for debugging:
            // System.out.println("Player " + player.getName() + " uniqueId: " + general.getUniqueId());
        }
    }

    /**
     * Called when a player quits the server.
     * Disables head rotation tracking, cleans up player state, and removes PlayerData.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        tracker.disable(player);
        cursorCleanup.cleanup(player);
        playerDataManager.remove(player);

        // Remove all player rigs and their entities (including locators)
        Collection<AJRigInstance> rigs = ajRigManager.getAllPlayerRigs();
        for (AJRigInstance rig : rigs) {
            // Pass all required arguments
            AJRigCleanupUtil.cleanupRig(rig, ajRigManager, AJRigScope.PLAYER, player);
        }
        ajRigManager.removeAllRigsForPlayer(player);
    }
}