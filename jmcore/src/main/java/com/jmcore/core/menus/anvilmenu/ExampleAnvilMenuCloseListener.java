package com.jmcore.core.menus.anvilmenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.AnvilInputData;
import com.jmcore.core.data.component.AnvilMenuData;

public class ExampleAnvilMenuCloseListener implements Listener {
    private final PlayerDataManager playerDataManager;

    public ExampleAnvilMenuCloseListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        AnvilMenuData anvilMenu = data.getComponent(AnvilMenuData.class);
        if (anvilMenu == null) return;
        AnvilInputData anvilInput = data.getComponent(AnvilInputData.class);
        if (anvilInput == null) return;

        InventoryView closedView = event.getView();
        InventoryView exampleView = anvilMenu.getExampleAnvilMenuView();

        // Only act if the closed inventory is the example anvil menu and the open state is true
        if (exampleView != null && closedView.equals(exampleView) && anvilMenu.isAnvilMenuOpened()) {
            // Add a cooldown to avoid rapid reopen loops
            long now = System.currentTimeMillis();
            if (anvilMenu.getLastAnvilMenuReopen() != null && now - anvilMenu.getLastAnvilMenuReopen() < 500) {
                return; // Prevent rapid reopen
            }
            anvilMenu.setLastAnvilMenuReopen(now);

            // Schedule reopen 1 tick later to avoid event conflicts
            Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () -> {
                if (!player.isOnline()) return;
                // Create a new anvil menu and update the saved view
                InventoryView newView = MenuType.ANVIL.create(player, Component.text(""));
                player.openInventory(newView);
                anvilMenu.setExampleAnvilMenuView(newView);

                // Set slot 0 to the latest anvil input, slots 1 and 2 blank
                Inventory inv = newView.getTopInventory();
                String name = anvilInput.getLastAnvilInput() != null ? anvilInput.getLastAnvilInput() : "";
                inv.setItem(0, AnvilDummyItemUtil.createDummyItem(0, name));
                inv.setItem(1, AnvilDummyItemUtil.createDummyItem(1, ""));
                inv.setItem(2, AnvilDummyItemUtil.createDummyItem(2, ""));

                // Sync the last input state so the poller and debug are correct
                anvilInput.setLastAnvilInput(name);
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        AnvilMenuData anvilMenu = data.getComponent(AnvilMenuData.class);
        if (anvilMenu == null) return;

        // Clean up any open anvil menu state
        anvilMenu.setAnvilMenuOpened(false);
        anvilMenu.clearExampleAnvilMenuView();
        anvilMenu.setAnvilMenuCallback(null);
    }
}