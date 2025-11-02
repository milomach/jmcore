package com.jmcore.core.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central container for all server-wide data components.
 */
public class ServerData {
    private final Map<Class<?>, Object> components = new ConcurrentHashMap<>();

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