package com.jmcore.core.command.sub.aj_test;

import org.bukkit.command.*;

import com.jmcore.core.command.*;
import com.jmcore.core.command.sub.aj_test.sub.*;

import java.util.List;

/**
 * Top-level AJTest sub command, now with nested sub-commands for each action.
 */
@SubCommandInfo
public class AJTestSubCommand extends AbstractSubCommand {
    static {
        CommandRegistry.register(new AJTestSubCommand());
    }

    public AJTestSubCommand() {
        registerSubCommand(new AJTestSetupSubCommand());
        registerSubCommand(new AJTestSummonSubCommand());
        registerSubCommand(new AJTestRemoveSubCommand());
        registerSubCommand(new AJTestCleanupSubCommand());
        registerSubCommand(new AJTestSetOffsetSubCommand());
        registerSubCommand(new AJTestVisibilitySubCommand());
        registerSubCommand(new AJTestBillboardSubCommand());
        registerSubCommand(new AJTestAnimationSubCommand());
    }

    @Override
    public String getName() { return "ajtest"; }
    @Override
    public String getDescription() { return "Test AJ rig system for a player."; }
    @Override
    public String getUsage() {
        return "/jmcore ajtest <playername> <setup|summon|remove|cleanup|setoffset|set_frame|apply_frame|play|stop|visibility|billboard|animation> ...";
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Show usage/help if no sub-command
        sender.sendMessage("Usage: " + getUsage());
        sender.sendMessage("Available actions: " + String.join(", ", getSubCommands().keySet()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getSubCommands().keySet().stream()
                .filter(name -> name.startsWith(args[0].toLowerCase()))
                .toList();
        }
        // Delegate to nested sub-commands
        if (args.length > 1) {
            String sub = args[0].toLowerCase();
            SubCommand child = getSubCommands().get(sub);
            if (child != null) {
                return child.tabComplete(sender, command, alias, java.util.Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return List.of();
    }
}