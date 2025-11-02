package com.jmcore.core.menus.anvilmenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.AnvilMenuData;

public class AnvilMenuDummyItemMaintenance implements Listener {
    private final PlayerDataManager playerDataManager;

    public AnvilMenuDummyItemMaintenance(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        startPeriodicCheck();
    }

    // --- Event-based cancellation for moving/removing dummy items ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        AnvilMenuData anvilMenu = data.getComponent(AnvilMenuData.class);
        if (anvilMenu == null || !anvilMenu.isAnvilMenuOpened()) return;

        InventoryView view = anvilMenu.getExampleAnvilMenuView();
        if (view == null || !event.getView().equals(view)) return;

        int slot = event.getRawSlot();
        if (slot >= 0 && slot <= 2) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        AnvilMenuData anvilMenu = data.getComponent(AnvilMenuData.class);
        if (anvilMenu == null || !anvilMenu.isAnvilMenuOpened()) return;

        InventoryView view = anvilMenu.getExampleAnvilMenuView();
        if (view == null || !event.getView().equals(view)) return;

        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= 2) {
                event.setCancelled(true);
                break;
            }
        }
    }

    // --- Periodic check and replacement ---
    private void startPeriodicCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = playerDataManager.get(player);
                    if (data == null) continue;
                    AnvilMenuData anvilMenu = data.getComponent(AnvilMenuData.class);
                    if (anvilMenu == null || !anvilMenu.isAnvilMenuOpened()) continue;

                    InventoryView view = anvilMenu.getExampleAnvilMenuView();
                    if (view == null) continue;

                    Inventory inv = view.getTopInventory();
                    // All slots: just check for correct dummy item, always blank name
                    for (int slot = 0; slot <= 2; slot++) {
                        ItemStack item = inv.getItem(slot);
                        if (!AnvilDummyItemUtil.isDummyItem(item, slot)) {
                            inv.setItem(slot, AnvilDummyItemUtil.createDummyItem(slot, ""));
                        }
                    }
                }
            }
        }.runTaskTimer(JavaPlugin.getProvidingPlugin(AnvilMenuDummyItemMaintenance.class), 10, 10);
    }
}