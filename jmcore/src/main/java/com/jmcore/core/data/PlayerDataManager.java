package com.jmcore.core.data;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages PlayerData containers for each player.
 * Automatically attaches all registered components.
 */
public class PlayerDataManager {
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerData get(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> {
            PlayerData data = new PlayerData(uuid);
            PlayerDataComponentRegistry.attachAllComponents(data);
            return data;
        });
    }

    public void remove(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public Map<UUID, PlayerData> getAll() {
        return playerDataMap;
    }

    public void clearAll() {
        playerDataMap.clear();
    }
}