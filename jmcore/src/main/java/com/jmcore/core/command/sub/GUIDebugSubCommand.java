package com.jmcore.core.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import com.jmcore.core.command.*;
import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.DebugData;
import com.jmcore.core.debug.DebugType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Subcommand for /jmcore guidebug <playername> <enable|disable> <type|all>
 * Uses dependency injection for PlayerDataManager.
 */
@SubCommandInfo
public class GUIDebugSubCommand extends AbstractSubCommand {
    static {
        CommandRegistry.register(new GUIDebugSubCommand());
    }
    private PlayerDataManager playerDataManager;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.playerDataManager = provider.get(PlayerDataManager.class);
    }

    @Override
    public String getName() { return "guidebug"; }
    @Override
    public String getDescription() { return "Enable or disable debug types for a player."; }
    @Override
    public String getUsage() { return "/jmcore guidebug <playername> <enable|disable> <type|all>"; }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gui.debug")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage("Usage: " + getUsage());
            sender.sendMessage("Valid types: " + getValidTypes());
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("Player not found or not online: " + args[0]);
            return true;
        }
        String action = args[1].toLowerCase();
        String typeStr = args[2].toUpperCase();
        
        PlayerData data = playerDataManager.get(target);
        if (data == null) {
            sender.sendMessage("No data for player: " + target.getName());
            return true;
        }
        DebugData debugData = data.getComponent(DebugData.class);
        if (debugData == null) {
            sender.sendMessage("No debug data for player: " + target.getName());
            return true;
        }

        if (typeStr.equals("ALL")) {
            switch (action) {
                case "enable" -> {
                    boolean changed = false;
                    for (DebugType type : DebugType.values()) {
                        if (!debugData.isDebugEnabled(type)) {
                            debugData.enableDebug(type);
                            changed = true;
                        }
                    }
                    if (changed) {
                        sender.sendMessage("Enabled all debug types for " + target.getName() + ".");
                        target.sendMessage("All debug types have been enabled for you.");
                    } else {
                        sender.sendMessage("All debug types are already enabled for " + target.getName() + ".");
                    }
                }
                case "disable" -> {
                    boolean changed = false;
                    for (DebugType type : DebugType.values()) {
                        if (debugData.isDebugEnabled(type)) {
                            debugData.disableDebug(type);
                            changed = true;
                        }
                    }
                    if (changed) {
                        sender.sendMessage("Disabled all debug types for " + target.getName() + ".");
                        target.sendMessage("All debug types have been disabled for you.");
                    } else {
                        sender.sendMessage("All debug types are already disabled for " + target.getName() + ".");
                    }
                }
                default -> {
                    sender.sendMessage("Usage: " + getUsage());
                    sender.sendMessage("Valid types: " + getValidTypes());
                }
            }
            return true;
        }

        DebugType type;
        try {
            type = DebugType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Unknown debug type: " + typeStr + ". Valid types: " + getValidTypes());
            return true;
        }
        switch (action) {
            case "enable" -> {
                if (debugData.isDebugEnabled(type)) {
                    sender.sendMessage("Debug type " + type + " is already enabled for " + target.getName() + ".");
                } else {
                    debugData.enableDebug(type);
                    sender.sendMessage("Enabled debug type " + type + " for " + target.getName() + ".");
                    target.sendMessage("Debug type " + type + " has been enabled for you.");
                }
            }
            case "disable" -> {
                if (!debugData.isDebugEnabled(type)) {
                    sender.sendMessage("Debug type " + type + " is already disabled for " + target.getName() + ".");
                } else {
                    debugData.disableDebug(type);
                    sender.sendMessage("Disabled debug type " + type + " for " + target.getName() + ".");
                    target.sendMessage("Debug type " + type + " has been disabled for you.");
                }
            }
            default -> {
                sender.sendMessage("Usage: " + getUsage());
                sender.sendMessage("Valid types: " + getValidTypes());
            }
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
        if (args.length == 3) {
            // FIX: Use mutable list
            List<String> types = new ArrayList<>();
            for (DebugType type : DebugType.values()) {
                types.add(type.name());
            }
            types.add("ALL");
            return types.stream()
                    .filter(t -> t.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }

    private String getValidTypes() {
        return String.join(", ",
            Arrays.stream(DebugType.values())
                .map(Enum::name)
                .toArray(String[]::new)
        ) + ", ALL";
    }
}