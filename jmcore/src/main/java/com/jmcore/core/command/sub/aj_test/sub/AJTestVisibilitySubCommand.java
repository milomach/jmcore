package com.jmcore.core.command.sub.aj_test.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.entity_state.AJRigApplyBoneStateUtil;
import com.jmcore.core.aj.entity_state.AJRigSetBoneStateUtil;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;
import com.jmcore.core.command.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages per-player visibility for AJ rig bones.
 */
public class AJTestVisibilitySubCommand extends AbstractSubCommand {
    private AJRigManager ajRigManager;
    private Plugin plugin;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
        this.plugin = provider.get(Plugin.class);
    }

    @Override
    public String getName() { return "visibility"; }
    @Override
    public String getDescription() { return "Manage per-player visibility for AJ rig bones."; }
    @Override
    public String getUsage() {
        return "/jmcore ajtest visibility <playername> default <hide/show> <full>\n"
            + "/jmcore ajtest visibility <playername> default <hide/show> <tree/bone> <bone_name>\n"
            + "/jmcore ajtest visibility <playername> override <hide/show> <playername> <add/remove> <full>\n"
            + "/jmcore ajtest visibility <playername> override <hide/show> <playername> <add/remove> <tree/bone> <bone_name>";
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage:\n" + getUsage());
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found: " + args[0]);
            return true;
        }
        AJRigInstance rig = ajRigManager.getRig(target, "blueprint");
        if (rig == null) {
            sender.sendMessage("No AJ rig found for " + target.getName());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage:\n" + getUsage());
            return true;
        }

        String visType = args[1].toLowerCase();

        // --- Default visibility ---
        if (visType.equals("default")) {
            if (args.length < 4) {
                sender.sendMessage("Usage: " + getUsage());
                return true;
            }
            String hideShow = args[2].toLowerCase();
            boolean defaultVisible = hideShow.equals("show");
            String part = args[3].toLowerCase();

            Set<String> boneNames;
            if (part.equals("full")) {
                boneNames = rig.getBoneNames();
            } else if ((part.equals("tree") || part.equals("bone")) && args.length >= 5) {
                String boneName = args[4];
                if (part.equals("tree")) {
                    boneNames = RigEntityUtil.getBoneTreeByName(rig, boneName);
                } else {
                    boneNames = Set.of(boneName);
                }
            } else {
                sender.sendMessage("Unknown part or missing bone name: " + part + ". Use full, tree, or bone.");
                return true;
            }

            AJRigSetBoneStateUtil.setDefaultVisibility(rig, boneNames, defaultVisible);
            Set<UUID> uuids = new HashSet<>();
            for (String name : boneNames) {
                UUID uuid = RigEntityUtil.getBoneEntityUUIDByName(rig, name);
                if (uuid != null) uuids.add(uuid);
            }
            AJRigApplyBoneStateUtil.applyBoneStates(rig, uuids, plugin);

            sender.sendMessage("Set default visibility to " + hideShow + " for " + part +
                ((part.equals("bone") || part.equals("tree")) && args.length >= 5 ? " " + args[4] : "") + ".");
            return true;
        }

        // --- Override visibility ---
        if (visType.equals("override")) {
            if (args.length < 6) {
                sender.sendMessage("Usage: " + getUsage());
                return true;
            }
            String hideShow = args[2].toLowerCase();
            boolean isShow = hideShow.equals("show");
            String overridePlayerName = args[3];
            Player overridePlayer = Bukkit.getPlayer(overridePlayerName);
            if (overridePlayer == null) {
                sender.sendMessage("Player not found: " + overridePlayerName);
                return true;
            }
            String addRemove = args[4].toLowerCase();
            boolean isAdd = addRemove.equals("add");
            String part = args[5].toLowerCase();

            Set<String> boneNames;
            if (part.equals("full")) {
                boneNames = rig.getBoneNames();
            } else if ((part.equals("tree") || part.equals("bone")) && args.length >= 7) {
                String boneName = args[6];
                if (part.equals("tree")) {
                    boneNames = RigEntityUtil.getBoneTreeByName(rig, boneName);
                } else {
                    boneNames = Set.of(boneName);
                }
            } else {
                sender.sendMessage("Unknown part or missing bone name: " + part + ". Use full, tree, or bone.");
                return true;
            }

            if (isShow) {
                for (String name : boneNames) {
                    AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
                    if (info == null) continue;
                    if (isAdd) {
                        info.shownForPlayers.add(overridePlayer.getUniqueId());
                    } else {
                        info.shownForPlayers.remove(overridePlayer.getUniqueId());
                    }
                    AJRigApplyBoneStateUtil.markStateNotApplied(info, "shown_for_players");
                }
            } else {
                for (String name : boneNames) {
                    AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
                    if (info == null) continue;
                    if (isAdd) {
                        info.hiddenForPlayers.add(overridePlayer.getUniqueId());
                    } else {
                        info.hiddenForPlayers.remove(overridePlayer.getUniqueId());
                    }
                    AJRigApplyBoneStateUtil.markStateNotApplied(info, "hidden_for_players");
                }
            }

            Set<UUID> uuids = new HashSet<>();
            for (String name : boneNames) {
                UUID uuid = RigEntityUtil.getBoneEntityUUIDByName(rig, name);
                if (uuid != null) uuids.add(uuid);
            }
            AJRigApplyBoneStateUtil.applyBoneStates(rig, uuids, plugin);

            sender.sendMessage((isAdd ? "Added " : "Removed ") + overridePlayerName + " to " + (isShow ? "show" : "hide") +
                " set for " + part + ((part.equals("bone") || part.equals("tree")) && args.length >= 7 ? " " + args[6] : "") + ".");
            return true;
        }

        sender.sendMessage("Unknown visibility type: " + visType + ". Use default or override.");
        return true;
    }

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // args[0] = player, args[1] = default/override, etc.
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return java.util.List.of("default", "override");
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("default")) {
            return java.util.List.of("hide", "show");
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("default")) {
            return java.util.List.of("full", "tree", "bone");
        }
        if (args.length == 5 && args[1].equalsIgnoreCase("default")) {
            String part = args[3].toLowerCase();
            if (part.equals("tree") || part.equals("bone")) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player != null) {
                    AJRigInstance rig = ajRigManager.getRig(player, "blueprint");
                    if (rig != null) {
                        return new java.util.ArrayList<>(rig.getBoneNames());
                    }
                }
                return java.util.List.of("<bone_name>");
            }
            return java.util.Collections.emptyList();
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("override")) {
            return java.util.List.of("hide", "show");
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("override")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 5 && args[1].equalsIgnoreCase("override")) {
            return java.util.List.of("add", "remove");
        }
        if (args.length == 6 && args[1].equalsIgnoreCase("override")) {
            return java.util.List.of("full", "tree", "bone");
        }
        if (args.length == 7 && args[1].equalsIgnoreCase("override")) {
            String part = args[5].toLowerCase();
            if (part.equals("tree") || part.equals("bone")) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player != null) {
                    AJRigInstance rig = ajRigManager.getRig(player, "blueprint");
                    if (rig != null) {
                        return new java.util.ArrayList<>(rig.getBoneNames());
                    }
                }
                return java.util.List.of("<bone_name>");
            }
            return java.util.Collections.emptyList();
        }
        return java.util.Collections.emptyList();
    }
}