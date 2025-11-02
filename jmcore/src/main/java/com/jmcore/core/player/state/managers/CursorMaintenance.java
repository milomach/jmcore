package com.jmcore.core.player.state.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Interaction;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.player.state.utils.DummyItemUtil;
import com.jmcore.core.player.state.utils.EquipmentSlotUtil;
import com.jmcore.core.player.state.utils.InteractionEntitiesCleanupUtil;
import com.jmcore.core.player.state.utils.InteractionEntitiesUtil;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles maintenance of dummy items: prevents movement, removal, and restores them if tampered with.
 */
public class CursorMaintenance implements Listener {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;
    private final Set<Player> maintainedPlayers = new HashSet<>();
    private BukkitRunnable periodicCheckTask;
    private BukkitRunnable yawSyncTask;

    public CursorMaintenance(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    public void start(Player player) {
        if (maintainedPlayers.isEmpty()) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            startPeriodicCheck();
            startYawSync();
        }
        maintainedPlayers.add(player);
    }

    public void stop(Player player) {
        maintainedPlayers.remove(player);
        if (maintainedPlayers.isEmpty()) {
            if (periodicCheckTask != null) {
                periodicCheckTask.cancel();
            }
            if (yawSyncTask != null) {
                yawSyncTask.cancel();
            }
            HandlerList.unregisterAll(this);
        }
    }

    // --- Event Listeners ---

    // Cancel all cancellable inventory events for maintained players
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && maintainedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && maintainedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() instanceof Player player && maintainedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (maintainedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (maintainedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    // --- Handle armor changes: restore dummy item if tampered with ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        if (maintainedPlayers.contains(player)) {
            int slot = EquipmentSlotUtil.toInventorySlot(event.getSlot());
            if (slot != -1) {
                ItemStack item = player.getInventory().getItem(slot);
                if (!DummyItemUtil.isDummyItem(item, slot)) {
                    DummyItemUtil.setDummyItem(player, slot);
                }
            }
        }
    }

    // --- Handle inventory slot changes: restore dummy item if tampered with ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSlotChange(PlayerInventorySlotChangeEvent event) {
        Player player = event.getPlayer();
        if (maintainedPlayers.contains(player)) {
            int slot = event.getSlot();
            ItemStack item = player.getInventory().getItem(slot);
            if (!DummyItemUtil.isDummyItem(item, slot)) {
                DummyItemUtil.setDummyItem(player, slot);
            }
        }
    }

    // --- Cancel item pickup for maintained players ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && maintainedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    // --- Periodic check for maintained systems ---
    private void startPeriodicCheck() {
        periodicCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : maintainedPlayers) {
                    PlayerData data = playerDataManager.get(player);

                    
                    // --- Interaction entity maintenance ---
                    Interaction spacer = InteractionEntitiesUtil.getSpacer(data);
                    Interaction target = InteractionEntitiesUtil.getTarget(data);

                    boolean needsReset = false;

                    // Check existence
                    if (spacer == null || target == null) {
                        needsReset = true;
                    }

                    // Check stacking: spacer must ride player, target must ride spacer
                    if (!needsReset) {
                        if (!player.getPassengers().contains(spacer) || !spacer.getPassengers().contains(target)) {
                            needsReset = true;
                        }
                    }

                    // Reset if needed
                    if (needsReset) {
                        InteractionEntitiesCleanupUtil.cleanup(data);
                        InteractionEntitiesUtil.setup(plugin, player, data);
                        spacer = InteractionEntitiesUtil.getSpacer(data);
                        target = InteractionEntitiesUtil.getTarget(data);
                    }
                    
                    // --- Dummy item maintenance ---
                    // Check all slots 0-40 (including offhand)
                    for (int slot = 0; slot <= 40; slot++) {
                        ItemStack item = player.getInventory().getItem(slot);
                        if (!DummyItemUtil.isDummyItem(item, slot)) {
                            DummyItemUtil.setDummyItem(player, slot);
                        }
                    }
                }
            }
        };
        periodicCheckTask.runTaskTimer(plugin, 20, 20); // every second
    }
    
    private void startYawSync() {
        yawSyncTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : maintainedPlayers) {
                    PlayerData data = playerDataManager.get(player);
                    var target = InteractionEntitiesUtil.getTarget(data);
                    if (target != null) {
                        target.setRotation(player.getLocation().getYaw(), 0);
                    }
                }
            }            };
        yawSyncTask.runTaskTimer(plugin, 1, 1); // every tick
    }
        
}