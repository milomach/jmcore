/*package com.jmcore.core.command.sub.aj_test;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.aj.*;
import com.jmcore.core.aj.posing.AJRigFrameUtil;
import com.jmcore.core.aj.rig_instance.AJRigCleanupUtil;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.AJRigRemoveUtil;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;
import com.jmcore.core.aj.rig_instance.AJRigSetupUtil;
import com.jmcore.core.aj.rig_instance.AJRigSummonUtil;
import com.jmcore.core.command.*;
import com.jmcore.core.util.QuaternionUtil;
import com.jmcore.core.util.display_utils.BillboardUtil;
import com.jmcore.core.aj.entity_state.AJRigSetBoneStateUtil;
import com.jmcore.core.aj.entity_state.AJRigApplyBoneStateUtil;

import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SubCommandInfo
public class AJTestSubCommand extends AbstractSubCommand {
    static {
        CommandRegistry.register(new AJTestSubCommand());
    }

    private AJRigManager ajRigManager;
    private Plugin plugin;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
        this.plugin = provider.get(Plugin.class);
    }

    @Override
    public String getName() { return "ajtest"; }
    @Override
    public String getDescription() { return "Test AJ rig system for a player."; }
    @Override
    public String getUsage() {
        return "/jmcore ajtest <playername> <setup/summon/remove/cleanup/setoffset/set_frame/apply_frame/play/stop/visibility/billboard/animation> ...";
    }

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

        String action = args[1].toLowerCase();
        String exportNamespace = "blueprint";
        String internalId = "blueprint";
        String animationName = "loop";
        
        // --- SETUP ---
        if (action.equals("setup")) {
            // Only allow one rig instance per player for this test command
            if (ajRigManager.getRig(target, internalId) != null) {
                sender.sendMessage("AJ rig already set up for " + target.getName());
                return true;
            }
            AJRigInstance rig = AJRigSetupUtil.setupRigInstance(
                AJRigScope.PLAYER,
                target,
                exportNamespace,
                internalId
            );
            ajRigManager.registerRig(target, internalId, rig);
            sender.sendMessage("AJ rig set up for " + target.getName());
            return true;
        }
        
        // --- SUMMON ---
        if (action.equals("summon")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /jmcore ajtest <playername> summon <bone|bone_tree|root> [bone_name]");
                return true;
            }
            AJRigInstance rig = ajRigManager.getRig(target, internalId);
            if (rig == null) {
                sender.sendMessage("No AJ rig set up for " + target.getName() + ". Use setup first.");
                return true;
            }
            Location loc = target.getLocation().clone();
            loc.setYaw(0f);
            loc.setPitch(0f);

            String summonType = args[2].toLowerCase();
            if (summonType.equals("root")) {
                AJRigSummonUtil.summonRootEntity(rig, loc, 0f, 0f, plugin);
                sender.sendMessage("AJ rig root entity summoned for " + target.getName());
                // Mount the root entity to the target player (if not already)
                UUID rootUUID = rig.getRootEntityUUID();
                if (rootUUID != null) {
                    Entity rootEntity = Bukkit.getEntity(rootUUID);
                    if (rootEntity != null && !target.getPassengers().contains(rootEntity)) {
                        target.addPassenger(rootEntity);
                    }
                }
                return true;
            } else if (summonType.equals("bone")) {
                if (args.length < 4) {
                    sender.sendMessage("Specify bone name for bone.");
                    return true;
                }
                String boneName = args[3];
                if (!rig.getBoneNames().contains(boneName)) {
                    sender.sendMessage("Bone not found in rig: " + boneName);
                    return true;
                }
                AJRigSummonUtil.summonBoneEntities(rig, boneName, loc, 0f, 0f, plugin);
                sender.sendMessage("AJ rig bone '" + boneName + "' summoned for " + target.getName());
                // Mount the root entity to the target player (if not already)
                UUID rootUUID = rig.getRootEntityUUID();
                if (rootUUID != null) {
                    Entity rootEntity = Bukkit.getEntity(rootUUID);
                    if (rootEntity != null && !target.getPassengers().contains(rootEntity)) {
                        target.addPassenger(rootEntity);
                    }
                }
                return true;
            } else if (summonType.equals("bone_tree")) {
                if (args.length < 4) {
                    sender.sendMessage("Specify bone name for bone_tree.");
                    return true;
                }
                String boneName = args[3];
                Set<String> boneNames = RigEntityUtil.getBoneTreeByName(rig, boneName);
                if (boneNames.isEmpty()) {
                    sender.sendMessage("Bone tree not found for bone: " + boneName);
                    return true;
                }
                AJRigSummonUtil.summonBoneEntities(rig, boneNames, loc, 0f, 0f, plugin);
                sender.sendMessage("AJ rig bone tree for '" + boneName + "' summoned for " + target.getName());
                // Mount the root entity to the target player (if not already)
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

        // --- REMOVE ---
        if (action.equals("remove")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /jmcore ajtest <playername> remove <bone|bone_tree|root> [bone_name]");
                return true;
            }
            AJRigInstance rig = ajRigManager.getRig(target, internalId);
            if (rig == null) {
                sender.sendMessage("No AJ rig found for " + target.getName());
                return true;
            }
            String removeType = args[2].toLowerCase();
            if (removeType.equals("root")) {
                AJRigRemoveUtil.removeRootEntity(rig);
                sender.sendMessage("AJ rig root entity and all bones removed for " + target.getName());
                return true;
            } else if (removeType.equals("bone")) {
                if (args.length < 4) {
                    sender.sendMessage("Specify bone name for bone.");
                    return true;
                }
                String boneName = args[3];
                UUID boneUUID = RigEntityUtil.getBoneEntityUUIDByName(rig, boneName);
                if (boneUUID == null) {
                    sender.sendMessage("Bone not found or not initialized: " + boneName);
                    return true;
                }
                AJRigRemoveUtil.removeBoneEntities(rig, boneUUID);
                sender.sendMessage("AJ rig bone '" + boneName + "' removed for " + target.getName());
                return true;
            } else if (removeType.equals("bone_tree")) {
                if (args.length < 4) {
                    sender.sendMessage("Specify bone name for bone_tree.");
                    return true;
                }
                String boneName = args[3];
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

        // --- CLEANUP (removes entities and persistent state, unregisters rig) ---
        if (action.equals("cleanup")) {
            AJRigInstance rig = ajRigManager.getRig(target, internalId);
            if (rig == null) {
                sender.sendMessage("No AJ rig found for " + target.getName());
                return true;
            }
            AJRigCleanupUtil.cleanupRig(rig, ajRigManager, AJRigScope.PLAYER, target);
            sender.sendMessage("AJ rig fully cleaned up for " + target.getName());
            return true;
        }

        // --- SET OFFSET ---
        if (action.equals("setoffset")) {
            if (args.length < 5) {
                sender.sendMessage("Usage: /jmcore ajtest <playername> setoffset <translation|rotation|scale> <axis> <value>");
                return true;
            }
            String type = args[2].toLowerCase();
            String axis = args[3].toLowerCase();
            float value;
            try {
                value = Float.parseFloat(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid offset value.");
                return true;
            }
            AJRigInstance rig = ajRigManager.getRig(target, internalId);
            if (rig == null) {
                sender.sendMessage("No AJ rig found for " + target.getName());
                return true;
            }
            AJRigOffsetManager offsetManager = AJRigOffsetManager.get(); // Assuming singleton or static accessor

            // --- Translation ---
            if (type.equals("translation")) {
                String sourceId = switch (axis) {
                    case "x" -> "trans_x_test";
                    case "y" -> "trans_y_test";
                    case "z" -> "trans_z_test";
                    default -> null;
                };
                if (sourceId == null) {
                    sender.sendMessage("Translation axis must be x, y, or z.");
                    return true;
                }
                offsetManager.addTranslationSource(rig, sourceId);
                Vector3f vec = new Vector3f(0, 0, 0);
                switch (axis) {
                    case "x" -> vec.x = value;
                    case "y" -> vec.y = value;
                    case "z" -> vec.z = value;
                }
                offsetManager.setTranslationSource(rig, sourceId, vec);
                sender.sendMessage("Translation offset source '" + sourceId + "' set to " + vec + " for " + target.getName());
                return true;
            }
            // --- Scale ---
            else if (type.equals("scale")) {
                String sourceId = switch (axis) {
                    case "x" -> "scale_x_test";
                    case "y" -> "scale_y_test";
                    case "z" -> "scale_z_test";
                    default -> null;
                };
                if (sourceId == null) {
                    sender.sendMessage("Scale axis must be x, y, or z.");
                    return true;
                }
                offsetManager.addScaleSource(rig, sourceId);
                Vector3f vec = new Vector3f(1, 1, 1);
                switch (axis) {
                    case "x" -> vec.x = value;
                    case "y" -> vec.y = value;
                    case "z" -> vec.z = value;
                }
                offsetManager.setScaleSource(rig, sourceId, vec);
                sender.sendMessage("Scale offset source '" + sourceId + "' set to " + vec + " for " + target.getName());
                return true;
            }
            // --- Rotation ---
            else if (type.equals("rotation")) {
                String sourceId;
                int order;
                Quaternionf quat;
                switch (axis) {
                    case "yaw" -> {
                        sourceId = "rot_y_test";
                        order = 1;
                        quat = QuaternionUtil.fromYawDegrees(value);
                    }
                    case "pitch" -> {
                        sourceId = "rot_x_test";
                        order = 2;
                        quat = QuaternionUtil.fromPitchDegrees(value);
                    }
                    case "roll" -> {
                        sourceId = "rot_z_test";
                        order = 3;
                        quat = QuaternionUtil.fromRollDegrees(value);
                    }
                    default -> {
                        sender.sendMessage("Rotation axis must be pitch, yaw, or roll.");
                        return true;
                    }
                }
                offsetManager.addRotationSource(rig, sourceId, order);
                offsetManager.setRotationSource(rig, sourceId, quat);
                sender.sendMessage("Rotation offset source '" + sourceId + "' set to " + axis + "=" + value + " for " + target.getName());
                return true;
            } else {
                sender.sendMessage("Offset type must be translation, rotation, or scale.");
                return true;
            }
        }

        // --- SET FRAME ---
        if (action.equals("set_frame")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /jmcore ajtest <playername> set_frame <frame>");
                return true;
            }
            int frame;
            try {
                frame = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid frame value.");
                return true;
            }
            AJRigInstance rig = ajRigManager.getRig(target, internalId);
            if (rig == null) {
                sender.sendMessage("No AJ rig found for " + target.getName());
                return true;
            }
            StringBuilder debug = new StringBuilder();
            boolean applied = AJRigFrameUtil.applyFrameToAllBones(rig, animationName, frame, debug);
            sender.sendMessage("Set frame " + frame + " for " + target.getName() + (applied ? "." : " (no bones applied)"));
            if (debug.length() > 0) {
                sender.sendMessage(debug.toString());
            }
            return true;
        }

        // --- Per-player visibility management (uses state system)---
        if (action.equals("visibility")) {
            if (args.length < 5) {
                sender.sendMessage("Usage:\n"
                    + "/jmcore ajtest <playername> visibility default <hide/show> <full>\n"
                    + "/jmcore ajtest <playername> visibility default <hide/show> <tree/bone> <bone_name>\n"
                    + "/jmcore ajtest <playername> visibility override <hide/show> <playername> <add/remove> <full>\n"
                    + "/jmcore ajtest <playername> visibility override <hide/show> <playername> <add/remove> <tree/bone> <bone_name>");
                return true;
            }

            AJRigInstance rig = ajRigManager.getRig(target, "blueprint");
            if (rig == null) {
                sender.sendMessage("No AJ rig found for " + target.getName());
                return true;
            }

            String visType = args[2].toLowerCase();

            // --- Default visibility ---
            if (visType.equals("default")) {
                String hideShow = args[3].toLowerCase();
                boolean defaultVisible = hideShow.equals("show");
                String part = args[4].toLowerCase();

                Set<String> boneNames;
                if (part.equals("full")) {
                    boneNames = rig.getBoneNames();
                } else if ((part.equals("tree") || part.equals("bone")) && args.length >= 6) {
                    String boneName = args[5];
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
                // Apply states to all affected bones
                Set<UUID> uuids = new HashSet<>();
                for (String name : boneNames) {
                    UUID uuid = RigEntityUtil.getBoneEntityUUIDByName(rig, name);
                    if (uuid != null) uuids.add(uuid);
                }
                AJRigApplyBoneStateUtil.applyBoneStates(rig, uuids, plugin);

                sender.sendMessage("Set default visibility to " + hideShow + " for " + part +
                    ((part.equals("bone") || part.equals("tree")) && args.length >= 6 ? " " + args[5] : "") + ".");
                return true;
            }

            // --- Override visibility ---
            if (visType.equals("override")) {
                // Accept 7 args for full, 8 for tree/bone
                if (args.length < 7) {
                    sender.sendMessage("Usage: /jmcore ajtest <playername> visibility override <hide/show> <playername> <add/remove> <full>\n"
                        + "/jmcore ajtest <playername> visibility override <hide/show> <playername> <add/remove> <tree/bone> <bone_name>");
                    return true;
                }
                String hideShow = args[3].toLowerCase();
                boolean isShow = hideShow.equals("show");
                String overridePlayerName = args[4];
                Player overridePlayer = Bukkit.getPlayer(overridePlayerName);
                if (overridePlayer == null) {
                    sender.sendMessage("Player not found: " + overridePlayerName);
                    return true;
                }
                String addRemove = args[5].toLowerCase();
                boolean isAdd = addRemove.equals("add");
                String part = args[6].toLowerCase();

                Set<String> boneNames;
                if (part.equals("full")) {
                    boneNames = rig.getBoneNames();
                } else if ((part.equals("tree") || part.equals("bone")) && args.length >= 8) {
                    String boneName = args[7];
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
                    // Show override
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
                    // Hide override
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

                // Apply states to all affected bones
                Set<UUID> uuids = new HashSet<>();
                for (String name : boneNames) {
                    UUID uuid = RigEntityUtil.getBoneEntityUUIDByName(rig, name);
                    if (uuid != null) uuids.add(uuid);
                }
                AJRigApplyBoneStateUtil.applyBoneStates(rig, uuids, plugin);

                sender.sendMessage((isAdd ? "Added " : "Removed ") + overridePlayerName + " to " + (isShow ? "show" : "hide") +
                    " set for " + part + ((part.equals("bone") || part.equals("tree")) && args.length >= 8 ? " " + args[7] : "") + ".");
                return true;
            }

            sender.sendMessage("Unknown visibility type: " + visType + ". Use default or override.");
            return true;
        }

        // --- BILLBOARD ---
        if (action.equals("billboard")) {
            if (args.length < 4) {
                sender.sendMessage("Usage: /jmcore ajtest <playername> billboard <fixed|vertical|horizontal|center> <full/tree/bone> <bone_name>");
                return true;
            }
            String modeStr = args[2].toLowerCase();
            String part = args[3].toLowerCase();
            String boneName = null;
            if ((part.equals("tree") || part.equals("bone"))) {
                if (args.length < 5) {
                    sender.sendMessage("Specify bone name for " + part + ".");
                    return true;
                }
                boneName = args[4];
            }

            AJRigInstance rig = ajRigManager.getRig(target, internalId);
            if (rig == null) {
                sender.sendMessage("No AJ rig found for " + target.getName());
                return true;
            }

            BillboardUtil.BillboardMode mode = BillboardUtil.BillboardMode.fromString(modeStr);
            if (mode == null) {
                sender.sendMessage("Unknown billboard mode: " + modeStr);
                return true;
            }

            if (part.equals("full")) {
                // Get entity set for billboard
                Set<Entity> allEntities = new HashSet<>();
                for (UUID uuid : rig.getBoneEntityUUIDs()) {
                    Entity e = Bukkit.getEntity(uuid);
                    if (e != null) allEntities.add(e);
                }
                BillboardUtil.setBillboardMode(allEntities, mode);
            } else if (part.equals("tree")) {
                // Get entity set for billboard
                if (boneName == null) {
                    sender.sendMessage("Specify bone name for tree.");
                    return true;
                }
                Set<String> boneNames = RigEntityUtil.getBoneTreeByName(rig, boneName);
                Set<Entity> boneEntities = new HashSet<>();
                for (String name : boneNames) {
                    Entity e = RigEntityUtil.getBoneEntityByName(rig, name);
                    if (e != null) boneEntities.add(e);
                }
                BillboardUtil.setBillboardMode(boneEntities, mode);
            } else if (part.equals("bone")) {
                // Get entity for billboard
                Entity bone = RigEntityUtil.getBoneEntityByName(rig, boneName);
                if (boneName == null) {
                    sender.sendMessage("Specify bone name for bone.");
                    return true;
                }
                BillboardUtil.setBillboardMode(bone, mode);
            } else {
                sender.sendMessage("Unknown billboard part: " + part);
                return true;
            }

            sender.sendMessage("Set billboard mode " + modeStr + " for " + target.getName() +
                " part=" + part + (boneName != null ? " bone=" + boneName : ""));
            return true;
        }

        // --- Animation ---
        if (action.equals("animation")) {
            AJRigInstance rig = ajRigManager.getRig(target, internalId);
            if (rig == null) {
                sender.sendMessage("No AJ rig found for " + target.getName());
                return true;
            }

            String animationAction = args[2].toLowerCase();
            switch (animationAction) {
                case "set_frame": {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /jmcore ajtest <playername> animation set_frame <frame>");
                        return true;
                    }
                    int frame;
                    try {
                        frame = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid frame value.");
                        return true;
                    }
                    boolean set = rig.setCurrentFrame(animationName, frame);
                    if (set) {
                        sender.sendMessage("Set current frame to " + frame + " for animation '" + animationName + "' for " + target.getName());
                    } else {
                        sender.sendMessage("Cannot set frame while animation is playing or setting.");
                    }
                    return true;
                }
                case "apply_frame": {
                    boolean applied = AJRigAnimationUtil.applyAnimationFrame(rig, animationName, plugin);
                    sender.sendMessage("Applied current frame (" + rig.getCurrentFrame(animationName) + ") for animation '" + animationName + "' for " + target.getName() +
                        (applied ? "." : " (cannot apply while playing or setting)"));
                    return true;
                }
                case "stop": {
                    boolean stopped = AJRigAnimationUtil.stopAnimation(rig, animationName, plugin);
                    sender.sendMessage("Stopped animation '" + animationName + "' for " + target.getName() +
                        (stopped ? "." : " (not playing)"));
                    return true;
                }
                case "play": {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /jmcore ajtest <playername> animation play <loop|reset|hold>");
                        return true;
                    }
                    String playbackModeStr = args[3].toLowerCase();
                    AJRigAnimationUtil.PlaybackOption playbackOption;
                    switch (playbackModeStr) {
                        case "loop": playbackOption = AJRigAnimationUtil.PlaybackOption.LOOP; break;
                        case "reset": playbackOption = AJRigAnimationUtil.PlaybackOption.RESET; break;
                        case "hold": playbackOption = AJRigAnimationUtil.PlaybackOption.HOLD; break;
                        default:
                            sender.sendMessage("Unknown playback mode: " + playbackModeStr + ". Use loop, reset, or hold.");
                            return true;
                    }
                    boolean started = AJRigAnimationUtil.playAnimation(rig, animationName, playbackOption, plugin);
                    sender.sendMessage("Started playing animation '" + animationName + "' (" + playbackModeStr + ") for " + target.getName() +
                        (started ? "." : " (already playing)"));
                    return true;
                }
                default:
                    sender.sendMessage("Unknown animation action: " + animationAction + ". Use set_frame, apply_frame, play, or stop.");
                    return true;
            }
        }

        sender.sendMessage("Unknown action: " + action);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return List.of("setup", "summon", "remove", "cleanup", "setoffset", "set_frame", "visibility", "billboard", "animation");
        }
        if (args.length == 3 && (args[1].equalsIgnoreCase("summon") || args[1].equalsIgnoreCase("remove"))) {
            return List.of("bone", "bone_tree", "root");
        }
        if (args.length == 4 && (args[1].equalsIgnoreCase("summon") || args[1].equalsIgnoreCase("remove"))) {
            String type = args[2].toLowerCase();
            if (type.equals("bone") || type.equals("bone_tree")) {
                // Suggest bone names from the rig instance if available
                if (sender instanceof Player player) {
                    AJRigInstance rig = ajRigManager.getRig(player, "blueprint");
                    if (rig != null) {
                        return new ArrayList<>(rig.getBoneNames());
                    }
                }
                // Fallback: suggest generic bone names
                return List.of("<bone_name>");
            }
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("setoffset")) {
            return List.of("translation", "rotation", "scale");
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("setoffset")) {
            String type = args[2].toLowerCase();
            if (type.equals("translation") || type.equals("scale")) {
                return List.of("x", "y", "z");
            } else if (type.equals("rotation")) {
                return List.of("pitch", "yaw", "roll");
            }
        }
        if (args.length == 5 && args[1].equalsIgnoreCase("setoffset")) {
            return List.of("<value>");
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("set_frame")) {
            return List.of("<frame>");
        }
        // --- Visibility ---
        if (args.length == 3 && args[1].equalsIgnoreCase("visibility")) {
            return List.of("default", "override");
        }
        // Default visibility: /jmcore ajtest <playername> visibility default <hide/show> <full/tree/bone> <bone_name>
        if (args.length == 4 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("default")) {
            return List.of("hide", "show");
        }
        if (args.length == 5 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("default")) {
            return List.of("full", "tree", "bone");
        }
        if (args.length == 6 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("default")) {
            String part = args[4].toLowerCase();
            if (part.equals("tree") || part.equals("bone")) {
                if (sender instanceof Player player) {
                    AJRigInstance rig = ajRigManager.getRig(player, "blueprint");
                    if (rig != null) {
                        return new ArrayList<>(rig.getBoneNames());
                    }
                }
                return List.of("<bone_name>");
            }
            return List.of();
        }
        // Override visibility: /jmcore ajtest <playername> visibility override <hide/show> <playername> <add/remove> <full/tree/bone> <bone_name>
        if (args.length == 4 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("override")) {
            return List.of("hide", "show");
        }
        if (args.length == 5 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("override")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 6 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("override")) {
            return List.of("add", "remove");
        }
        if (args.length == 7 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("override")) {
            return List.of("full", "tree", "bone");
        }
        if (args.length == 8 && args[1].equalsIgnoreCase("visibility") && args[2].equalsIgnoreCase("override")) {
            String part = args[6].toLowerCase();
            if (part.equals("tree") || part.equals("bone")) {
                if (sender instanceof Player player) {
                    AJRigInstance rig = ajRigManager.getRig(player, "blueprint");
                    if (rig != null) {
                        return new ArrayList<>(rig.getBoneNames());
                    }
                }
                return List.of("<bone_name>");
            }
            return List.of();
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("billboard")) {
            return List.of("fixed", "vertical", "horizontal", "center");
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("billboard")) {
            return List.of("full", "tree", "bone");
        }
        if (args.length == 5 && args[1].equalsIgnoreCase("billboard") &&
                (args[3].equalsIgnoreCase("tree") || args[3].equalsIgnoreCase("bone"))) {
            return List.of("<bone_name>");
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("animation")) {
            return List.of("set_frame", "apply_frame", "play", "stop");
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("animation")) {
            String animationAction = args[2].toLowerCase();
            if (animationAction.equals("set_frame")) {
                return List.of("<frame>");
            }
            if (animationAction.equals("play")) {
                return List.of("loop", "reset", "hold");
            }
        }
        return List.of();
    }
}
*/