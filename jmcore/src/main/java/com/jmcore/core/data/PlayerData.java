package com.jmcore.core.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central container for all per-player data components.
 */
public class PlayerData {
    private final UUID playerId;
    private final Map<Class<?>, Object> components = new ConcurrentHashMap<>();

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    // Accept any component type for registration (fixes registry error)
    public void registerComponent(Class<?> type, Object component) {
        components.put(type, component);
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> type) {
        return (T) components.get(type);
    }

    public void removeComponent(Class<?> type) {
        components.remove(type);
    }
}