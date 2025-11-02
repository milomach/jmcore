package com.jmcore.core.player.state.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Utility for setting a player's camera distance attribute.
 */
public class CameraDistanceUtil {
    public static void setCameraDistance(Player player, double value) {
        String cmd = String.format("attribute %s minecraft:camera_distance base set %s", player.getName(), value);
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, cmd);
    }
}