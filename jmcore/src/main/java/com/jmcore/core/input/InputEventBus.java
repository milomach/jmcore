package com.jmcore.core.input;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple event bus for plugin input events.
 */
public class InputEventBus {
    private static final List<Consumer<PluginInputEvent>> listeners = new CopyOnWriteArrayList<>();

    public static void register(Consumer<PluginInputEvent> listener) {
        listeners.add(listener);
    }

    public static void unregister(Consumer<PluginInputEvent> listener) {
        listeners.remove(listener);
    }

    public static void post(PluginInputEvent event) {
        for (Consumer<PluginInputEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}