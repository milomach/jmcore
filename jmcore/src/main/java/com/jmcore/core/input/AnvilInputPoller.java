package com.jmcore.core.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.AnvilInputData;
import com.jmcore.core.input.events.AnvilInputEvent;

import org.bukkit.inventory.view.AnvilView;

public class AnvilInputPoller extends BukkitRunnable {
    private final PlayerDataManager playerDataManager;

    public AnvilInputPoller(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void run() {
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerDataManager.get(player); // CHANGED: new type
            if (data == null) continue;
            AnvilInputData anvilInput = data.getComponent(AnvilInputData.class);
            if (anvilInput == null) continue;

            // Only poll if the player has an anvil menu open
            if (!(player.getOpenInventory().getTopInventory() instanceof AnvilView
                || player.getOpenInventory().getTopInventory() instanceof AnvilInventory)) continue;

            AnvilView view = (AnvilView) player.getOpenInventory();
            String current = view.getRenameText();
            if (current == null) current = "";

            if (!current.equals(anvilInput.getLastAnvilInput())) {
                anvilInput.setLastAnvilInput(current);
                InputEventBus.post(new AnvilInputEvent(player, current));
            }
        }
    }
}