package com.jmcore.core.command.sub.aj_test.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;
import com.jmcore.core.command.*;
import com.jmcore.core.util.display_utils.BillboardUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Sets billboard mode for AJ rig entities.
 */
public class AJTestBillboardSubCommand extends AbstractSubCommand {
    private AJRigManager ajRigManager;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
    }

    @Override
    public String getName() { return "billboard"; }
    @Override
    public String getDescription() { return "Set billboard mode for AJ rig entities."; }
    @Override
    public String getUsage() {
        return "/jmcore ajtest billboard <playername> <fixed|vertical|horizontal|center> <full/tree/bone> <bone_name>";
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
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

        String modeStr = args[1].toLowerCase();
        String part = args[2].toLowerCase();
        String boneName = null;
        if ((part.equals("tree") || part.equals("bone"))) {
            if (args.length < 4) {
                sender.sendMessage("Specify bone name for " + part + ".");
                return true;
            }
            boneName = args[3];
        }

        BillboardUtil.BillboardMode mode = BillboardUtil.BillboardMode.fromString(modeStr);
        if (mode == null) {
            sender.sendMessage("Unknown billboard mode: " + modeStr);
            return true;
        }

        if (part.equals("full")) {
            Set<Entity> allEntities = new HashSet<>();
            for (UUID uuid : rig.getBoneEntityUUIDs()) {
                Entity e = Bukkit.getEntity(uuid);
                if (e != null) allEntities.add(e);
            }
            BillboardUtil.setBillboardMode(allEntities, mode);
        } else if (part.equals("tree")) {
            Set<String> boneNames = RigEntityUtil.getBoneTreeByName(rig, boneName);
            Set<Entity> boneEntities = new HashSet<>();
            for (String name : boneNames) {
                Entity e = RigEntityUtil.getBoneEntityByName(rig, name);
                if (e != null) boneEntities.add(e);
            }
            BillboardUtil.setBillboardMode(boneEntities, mode);
        } else if (part.equals("bone")) {
            Entity bone = RigEntityUtil.getBoneEntityByName(rig, boneName);
            BillboardUtil.setBillboardMode(bone, mode);
        } else {
            sender.sendMessage("Unknown billboard part: " + part);
            return true;
        }

        sender.sendMessage("Set billboard mode " + modeStr + " for " + target.getName() +
            " part=" + part + (boneName != null ? " bone=" + boneName : ""));
        return true;
    }

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return java.util.List.of("fixed", "vertical", "horizontal", "center");
        }
        if (args.length == 3) {
            return java.util.List.of("full", "tree", "bone");
        }
        if (args.length == 4 && (args[2].equalsIgnoreCase("tree") || args[2].equalsIgnoreCase("bone"))) {
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
}