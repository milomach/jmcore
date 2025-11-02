package com.jmcore.core.command;

import org.bukkit.command.*;
import java.util.*;

/**
 * Root command executor and tab completer.
 * Recursively delegates to nested sub-commands.
 */
public class RootCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /" + label + " <subcommand> [args...]");
            sender.sendMessage("§7Available subcommands: " + String.join(", ", CommandRegistry.getNames()));
            return true;
        }
        SubCommand sub = CommandRegistry.get(args[0]);
        if (sub == null) {
            sender.sendMessage("§cUnknown subcommand: " + args[0]);
            return true;
        }
        return executeRecursive(sub, sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }

    /**
     * Recursively executes nested sub-commands.
     */
    private boolean executeRecursive(SubCommand sub, CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && sub.getSubCommands().containsKey(args[0].toLowerCase())) {
            SubCommand child = sub.getSubCommands().get(args[0].toLowerCase());
            return executeRecursive(child, sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        }
        return sub.execute(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String name : CommandRegistry.getNames()) {
                if (name.startsWith(prefix)) matches.add(name);
            }
            return matches;
        }
        SubCommand sub = CommandRegistry.get(args[0]);
        if (sub != null) {
            return tabCompleteRecursive(sub, sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        }
        return Collections.emptyList();
    }

    /**
     * Recursively delegates tab completion to nested sub-commands.
     */
    private List<String> tabCompleteRecursive(SubCommand sub, CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 0 && sub.getSubCommands().containsKey(args[0].toLowerCase())) {
            SubCommand child = sub.getSubCommands().get(args[0].toLowerCase());
            return tabCompleteRecursive(child, sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        }
        return sub.tabComplete(sender, command, alias, args);
    }
}