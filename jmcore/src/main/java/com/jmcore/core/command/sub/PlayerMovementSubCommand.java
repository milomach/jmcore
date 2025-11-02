package com.jmcore.core.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import com.jmcore.core.command.*;
import com.jmcore.core.player.state.managers.MovementCleanup;
import com.jmcore.core.player.state.managers.MovementSetup;
import com.jmcore.core.player_movement.PlayerMovementSystem;

import java.util.Arrays;
import java.util.List;

@SubCommandInfo
public class PlayerMovementSubCommand extends AbstractSubCommand {
    static {
        CommandRegistry.register(new PlayerMovementSubCommand());
    }

    private PlayerMovementSystem playerMovementSystem;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.playerMovementSystem = provider.get(PlayerMovementSystem.class);
    }

    @Override
    public String getName() { return "player_movement"; }
    @Override
    public String getDescription() { return "Enable or disable the player movement system for a player."; }
    @Override
    public String getUsage() { return "/jmcore player_movement <player_name> <enable|disable>"; }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found: " + args[0]);
            return true;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "enable" -> {
                MovementSetup.setup(target);
                playerMovementSystem.enableForPlayer(target);
                sender.sendMessage("Enabled custom movement for " + target.getName());
            }
            case "disable" -> {
                playerMovementSystem.disableForPlayer(target);
                MovementCleanup.cleanup(target);
                sender.sendMessage("Disabled custom movement for " + target.getName());
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
        return List.of();
    }
}