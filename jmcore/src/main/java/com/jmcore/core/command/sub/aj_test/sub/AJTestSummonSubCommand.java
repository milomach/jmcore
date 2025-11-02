package com.jmcore.core.command.sub.aj_test.sub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.AJRigSummonUtil;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;
import com.jmcore.core.command.*;

import java.util.Set;
import java.util.UUID;

/**
 * Summons AJ rig entities for a player.
 */
public class AJTestSummonSubCommand extends AbstractSubCommand {
    private AJRigManager ajRigManager;
    private Plugin plugin;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
        this.plugin = provider.get(Plugin.class);
    }

    @Override
    public String getName() { return "summon"; }
    @Override
    public String getDescription() { return "Summon AJ rig entities for a player."; }
    @Override
    public String getUsage() { return "/jmcore ajtest summon <playername> <bone|bone_tree|root> [bone_name]"; }

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
            sender.sendMessage("No AJ rig set up for " + target.getName() + ". Use setup first.");
            return true;
        }
        Location loc = target.getLocation().clone();
        loc.setYaw(0f);
        loc.setPitch(0f);

        if (args.length < 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        String summonType = args[1].toLowerCase();
        if (summonType.equals("root")) {
            AJRigSummonUtil.summonRootEntity(rig, loc, 0f, 0f, plugin);
            sender.sendMessage("AJ rig root entity summoned for " + target.getName());
            UUID rootUUID = rig.getRootEntityUUID();
            if (rootUUID != null) {
                Entity rootEntity = Bukkit.getEntity(rootUUID);
                if (rootEntity != null && !target.getPassengers().contains(rootEntity)) {
                    target.addPassenger(rootEntity);
                }
            }
            return true;
        } else if (summonType.equals("bone")) {
            if (args.length < 3) {
                sender.sendMessage("Specify bone name for bone.");
                return true;
            }
            String boneName = args[2];
            if (!rig.getBoneNames().contains(boneName)) {
                sender.sendMessage("Bone not found in rig: " + boneName);
                return true;
            }
            AJRigSummonUtil.summonBoneEntities(rig, boneName, loc, 0f, 0f, plugin);
            sender.sendMessage("AJ rig bone '" + boneName + "' summoned for " + target.getName());
            UUID rootUUID = rig.getRootEntityUUID();
            if (rootUUID != null) {
                Entity rootEntity = Bukkit.getEntity(rootUUID);
                if (rootEntity != null && !target.getPassengers().contains(rootEntity)) {
                    target.addPassenger(rootEntity);
                }
            }
            return true;
        } else if (summonType.equals("bone_tree")) {
            if (args.length < 3) {
                sender.sendMessage("Specify bone name for bone_tree.");
                return true;
            }
            String boneName = args[2];
            Set<String> boneNames = RigEntityUtil.getBoneTreeByName(rig, boneName);
            if (boneNames.isEmpty()) {
                sender.sendMessage("Bone tree not found for bone: " + boneName);
                return true;
            }
            AJRigSummonUtil.summonBoneEntities(rig, boneNames, loc, 0f, 0f, plugin);
            sender.sendMessage("AJ rig bone tree for '" + boneName + "' summoned for " + target.getName());
            UUID rootUUID = rig.getRootEntityUUID();
            if (rootUUID != null) {
                Entity rootEntity = Bukkit.getEntity(rootUUID);
                if (rootEntity != null && !target.getPassengers().contains(rootEntity)) {
                    target.addPassenger(rootEntity);
                }
            }
            return true;
        } else {
            sender.sendMessage("Unknown summon type: " + summonType + ". Use bone, bone_tree, or root.");
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