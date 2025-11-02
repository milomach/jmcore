package com.jmcore.core.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for sub-commands supporting nested sub-commands.
 * Use this for any command that should have children.
 */
public abstract class AbstractSubCommand implements SubCommand {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    @Override
    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    /**
     * Inject dependencies into this command and all nested sub-commands recursively.
     */
    @Override
    public void injectDependencies(DependencyProvider provider) {
        for (SubCommand sub : subCommands.values()) {
            sub.injectDependencies(provider);
        }
    }
}