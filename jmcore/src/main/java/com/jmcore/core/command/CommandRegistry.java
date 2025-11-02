package com.jmcore.core.command;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for top-level sub-commands.
 * Handles dependency injection for all registered commands and their children.
 */
public class CommandRegistry {
    private static final Map<String, SubCommand> subCommands = new ConcurrentHashMap<>();
    private static DependencyProvider dependencyProvider;

    /**
     * Sets the dependency provider and injects dependencies into all registered commands recursively.
     */
    public static void setDependencyProvider(DependencyProvider provider) {
        dependencyProvider = provider;
        for (SubCommand sub : subCommands.values()) {
            sub.injectDependencies(provider);
        }
    }

    /**
     * Registers a top-level sub-command.
     * Injects dependencies if provider is already set.
     */
    public static void register(SubCommand subCommand) {
        if (dependencyProvider != null) {
            subCommand.injectDependencies(dependencyProvider);
        }
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    /**
     * Gets a top-level sub-command by name.
     */
    public static SubCommand get(String name) {
        return subCommands.get(name.toLowerCase());
    }

    /**
     * Gets all registered top-level sub-commands.
     */
    public static Collection<SubCommand> getAll() {
        return subCommands.values();
    }

    /**
     * Gets all registered top-level sub-command names.
     */
    public static Set<String> getNames() {
        return subCommands.keySet();
    }
}