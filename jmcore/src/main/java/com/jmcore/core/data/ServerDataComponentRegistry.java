package com.jmcore.core.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for all ServerData components.
 * Components register themselves here in a static block.
 */
public class ServerDataComponentRegistry {
    private static final Map<Class<?>, Supplier<?>> factories = new LinkedHashMap<>();

    public static <T> void register(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
    }

    public static void attachAllComponents(ServerData data) {
        for (Map.Entry<Class<?>, Supplier<?>> entry : factories.entrySet()) {
            Object component = entry.getValue().get();
            data.registerComponent(entry.getKey(), component);
        }
    }
}