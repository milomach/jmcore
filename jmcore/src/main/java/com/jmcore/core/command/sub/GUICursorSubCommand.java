package com.jmcore.core.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import com.jmcore.core.command.*;
import com.jmcore.core.cursor.HeadRotationTracker;
import com.jmcore.core.data.PlayerDataManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SubCommandInfo
public class GUICursorSubCommand extends AbstractSubCommand {
    // Static block for registration
    static {
        CommandRegistry.register(new GUICursorSubCommand());
    }

    private HeadRotationTracker tracker;
    private PlayerDataManager playerDataManager;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.tracker = provider.get(HeadRotationTracker.class);
        this.playerDataManager = provider.get(PlayerDataManager.class);
    }

    @Override
    public String getName() { return "guicursor"; }
    @Override
    public String getDescription() { return "Enable or disable head rotation tracking for a player."; }
    @Override
    public String getUsage() { return "/jmcore guicursor <playername> <enable|disable>"; }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gui.use")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("Player not found or not online: " + args[0]);
            return true;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "enable" -> {
                tracker.enable(target);
                sender.sendMessage("Head rotation tracking enabled for " + target.getName() + ".");
                target.sendMessage("Head rotation tracking has been enabled for you.");
            }
            case "disable" -> {
                tracker.disable(target);
                sender.sendMessage("Head rotation tracking disabled for " + target.getName() + ".");
                target.sendMessage("Head rotation tracking has been disabled for you.");
            }
            default -> sender.sendMessage("Usage: " + getUsage());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2) {
            return Arrays.asList("enable", "disable");
        }
        return Collections.emptyList();
    }
}