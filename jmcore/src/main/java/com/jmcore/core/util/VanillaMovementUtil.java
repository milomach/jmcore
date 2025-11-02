package com.jmcore.core.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;

public class VanillaMovementUtil {
    public static void lockVanillaMovement(Player player) {
        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        player.setAllowFlight(false);
        
        // Set jump strength to 0
        Server server = player.getServer();
        ConsoleCommandSender console = server.getConsoleSender();
        server.dispatchCommand(console, 
            String.format("attribute %s minecraft:jump_strength base set 0", player.getName()));
    }

    public static void unlockVanillaMovement(Player player) {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE || 
                            player.getGameMode() == GameMode.SPECTATOR);
        
        // Reset jump strength to default
        Server server = player.getServer();
        ConsoleCommandSender console = server.getConsoleSender();
        server.dispatchCommand(console,
            String.format("attribute %s minecraft:jump_strength base set 0.42", player.getName()));
    }
}