package com.jmcore.core.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry for all PlayerData components.
 * Components register themselves here in a static block.
 */
public class PlayerDataComponentRegistry {
    private static final Map<Class<?>, Function<PlayerData, ?>> factories = new LinkedHashMap<>();

    public static <T> void register(Class<T> type, Function<PlayerData, T> factory) {
        factories.put(type, factory);
    }

    public static void attachAllComponents(PlayerData data) {
        for (Map.Entry<Class<?>, Function<PlayerData, ?>> entry : factories.entrySet()) {
            Object component = entry.getValue().apply(data);
            data.registerComponent(entry.getKey(), component);
        }
    }
}