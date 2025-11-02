package com.jmcore.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * Base interface for all sub-commands.
 * Supports nested sub-commands, dependency injection, execution, and tab completion.
 */
public interface SubCommand {
    /**
     * Called by the loader after instantiation, before registration.
     * Used for dependency injection.
     */
    default void injectDependencies(DependencyProvider provider) {}

    /**
     * Name of the sub-command (used for registration and lookup).
     */
    String getName();

    /**
     * Description for help/usage.
     */
    String getDescription();

    /**
     * Usage string for help/usage.
     */
    String getUsage();

    /**
     * Execute the command.
     * @param sender Command sender
     * @param command Command object
     * @param label Command label
     * @param args Arguments (excluding the sub-command name and any parent sub-command names)
     * @return true if handled, false otherwise
     */
    boolean execute(CommandSender sender, Command command, String label, String[] args);

    /**
     * Tab completion for the command.
     * @param sender Command sender
     * @param command Command object
     * @param alias Command alias
     * @param args Arguments (excluding the sub-command name and any parent sub-command names)
     * @return List of tab completions
     */
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);

    /**
     * Returns a map of nested sub-commands.
     * Key: sub-command name (lowercase), Value: SubCommand instance.
     */
    default Map<String, SubCommand> getSubCommands() {
        return Map.of();
    }

    /**
     * Registers a nested sub-command.
     * Override if you want to support dynamic registration.
     */
    default void registerSubCommand(SubCommand subCommand) {}
}