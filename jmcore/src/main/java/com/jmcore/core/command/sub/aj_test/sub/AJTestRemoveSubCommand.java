package com.jmcore.core.command.sub.aj_test.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.AJRigRemoveUtil;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;
import com.jmcore.core.command.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Removes AJ rig entities for a player.
 */
public class AJTestRemoveSubCommand extends AbstractSubCommand {
    private AJRigManager ajRigManager;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
    }

    @Override
    public String getName() { return "remove"; }
    @Override
    public String getDescription() { return "Remove AJ rig entities for a player."; }
    @Override
    public String getUsage() { return "/jmcore ajtest remove <playername> <bone|bone_tree|root> [bone_name]"; }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found: " + args[0]);
            return true;
        }
        String internalId = "blueprint";
        AJRigInstance rig = ajRigManager.getRig(target, internalId);
        if (rig == null) {
            sender.sendMessage("No AJ rig found for " + target.getName());
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        String removeType = args[1].toLowerCase();
        if (removeType.equals("root")) {
            AJRigRemoveUtil.removeRootEntity(rig);
            sender.sendMessage("AJ rig root entity and all bones removed for " + target.getName());
            return true;
        } else if (removeType.equals("bone")) {
            if (args.length < 3) {
                sender.sendMessage("Specify bone name for bone.");
                return true;
            }
            String boneName = args[2];
            UUID boneUUID = RigEntityUtil.getBoneEntityUUIDByName(rig, boneName);
            if (boneUUID == null) {
                sender.sendMessage("Bone not found or not initialized: " + boneName);
                return true;
            }
            AJRigRemoveUtil.removeBoneEntities(rig, boneUUID);
            sender.sendMessage("AJ rig bone '" + boneName + "' removed for " + target.getName());
            return true;
        } else if (removeType.equals("bone_tree")) {
            if (args.length < 3) {
                sender.sendMessage("Specify bone name for bone_tree.");
                return true;
            }
            String boneName = args[2];
            Set<String> boneNames = RigEntityUtil.getBoneTreeByName(rig, boneName);
            Set<UUID> uuids = new HashSet<>();
            for (String name : boneNames) {
                UUID uuid = RigEntityUtil.getBoneEntityUUIDByName(rig, name);
                if (uuid != null) uuids.add(uuid);
            }
            if (uuids.isEmpty()) {
                sender.sendMessage("Bone tree not found or not initialized for bone: " + boneName);
                return true;
            }
            AJRigRemoveUtil.removeBoneEntities(rig, uuids);
            sender.sendMessage("AJ rig bone tree for '" + boneName + "' removed for " + target.getName());
            return true;
        } else {
            sender.sendMessage("Unknown remove type: " + removeType + ". Use bone, bone_tree, or root.");
            return true;
        }
    }

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return java.util.List.of("bone", "bone_tree", "root");
        }
        if (args.length == 3 && (args[1].equalsIgnoreCase("bone") || args[1].equalsIgnoreCase("bone_tree"))) {
            if (sender instanceof Player player) {
                AJRigInstance rig = ajRigManager.getRig(player, "blueprint");
                if (rig != null) {
                    return new java.util.ArrayList<>(rig.getBoneNames());
                }
            }
            return java.util.List.of("<bone_name>");
        }
        return java.util.Collections.emptyList();
    }
}