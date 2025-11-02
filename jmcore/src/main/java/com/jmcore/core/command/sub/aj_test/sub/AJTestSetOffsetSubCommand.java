package com.jmcore.core.command.sub.aj_test.sub;

import com.jmcore.core.aj.rig_instance.AJOffsetSource;
import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.command.*;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.List;

public class AJTestSetOffsetSubCommand extends AbstractSubCommand {
    private static final String TEST_SOURCE_ID = "ajtest_offset";
    private AJRigManager ajRigManager;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
    }

    @Override
    public String getName() { return "offset"; }
    @Override
    public String getDescription() { return "Manage offset sources for a rig."; }
    @Override
    public String getUsage() {
        return "/jmcore ajtest offset <playername> source <create|remove>\n"
             + "/jmcore ajtest offset <playername> set <rotation|translation|scale> <axis> <value>";
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage:\n" + getUsage());
            return true;
        }
        Player target = org.bukkit.Bukkit.getPlayer(args[0]);
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

        String action = args[1].toLowerCase();

        if (action.equals("source")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /jmcore ajtest offset <playername> source <create|remove>");
                return true;
            }
            String subAction = args[2].toLowerCase();
            switch (subAction) {
                case "create": {
                    if (rig.getOffsetSource(TEST_SOURCE_ID) != null) {
                        sender.sendMessage("Offset source '" + TEST_SOURCE_ID + "' already exists for this rig.");
                        return true;
                    }
                    AJOffsetSource src = rig.createOffsetSource(TEST_SOURCE_ID, 10);
                    if (src != null) {
                        // By default, include all bones for demonstration
                        src.setIncludedBones(rig.getBoneNames());
                        sender.sendMessage("Offset source '" + TEST_SOURCE_ID + "' created for " + target.getName() + ".");
                    } else {
                        sender.sendMessage("Failed to create offset source (already exists?).");
                    }
                    return true;
                }
                case "remove": {
                    if (rig.getOffsetSource(TEST_SOURCE_ID) == null) {
                        sender.sendMessage("No offset source '" + TEST_SOURCE_ID + "' exists for this rig.");
                        return true;
                    }
                    rig.removeOffsetSource(TEST_SOURCE_ID);
                    sender.sendMessage("Offset source '" + TEST_SOURCE_ID + "' removed for " + target.getName() + ".");
                    return true;
                }
                default:
                    sender.sendMessage("Unknown source action: " + subAction + ". Use create or remove.");
                    return true;
            }
        }

        if (action.equals("set")) {
            if (args.length < 5) {
                sender.sendMessage("Usage: /jmcore ajtest offset <playername> set <rotation|translation|scale> <axis> <value>");
                return true;
            }
            AJOffsetSource src = rig.getOffsetSource(TEST_SOURCE_ID);
            if (src == null) {
                sender.sendMessage("No offset source '" + TEST_SOURCE_ID + "' exists for this rig. Use 'source create' first.");
                return true;
            }
            String type = args[2].toLowerCase();
            String axis = args[3].toLowerCase();
            float value;
            try {
                value = Float.parseFloat(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid value for axis.");
                return true;
            }

            if (type.equals("translation")) {
                Vector3f translation = src.getTranslation();
                switch (axis) {
                    case "x" -> translation.x = value;
                    case "y" -> translation.y = value;
                    case "z" -> translation.z = value;
                    default -> {
                        sender.sendMessage("Translation axis must be x, y, or z.");
                        return true;
                    }
                }
                src.setTranslation(translation);
                sender.sendMessage("Set translation offset " + axis + "=" + value + " for source '" + TEST_SOURCE_ID + "'.");
                return true;
            } else if (type.equals("scale")) {
                Vector3f scale = src.getScale();
                switch (axis) {
                    case "x" -> scale.x = value;
                    case "y" -> scale.y = value;
                    case "z" -> scale.z = value;
                    default -> {
                        sender.sendMessage("Scale axis must be x, y, or z.");
                        return true;
                    }
                }
                src.setScale(scale);
                sender.sendMessage("Set scale offset " + axis + "=" + value + " for source '" + TEST_SOURCE_ID + "'.");
                return true;
            } else if (type.equals("rotation")) {
                // Get current rotation as Euler angles (in degrees)
                Quaternionf currentQuat = src.getRotation();
                Vector3f angles = new Vector3f();
                currentQuat.getEulerAnglesXYZ(angles); // returns radians

                // Convert to degrees for user-friendly input/output
                float pitch = (float) Math.toDegrees(angles.x);
                float yaw = (float) Math.toDegrees(angles.y);
                float roll = (float) Math.toDegrees(angles.z);

                // Update only the specified axis
                switch (axis) {
                    case "yaw" -> yaw = value;
                    case "pitch" -> pitch = value;
                    case "roll" -> roll = value;
                    default -> {
                        sender.sendMessage("Rotation axis must be pitch, yaw, or roll.");
                        return true;
                    }
                }

                // Rebuild quaternion from updated Euler angles (XYZ order)
                Quaternionf newQuat = new Quaternionf()
                    .rotateXYZ((float) Math.toRadians(pitch), (float) Math.toRadians(yaw), (float) Math.toRadians(roll));
                src.setRotation(newQuat);
                sender.sendMessage("Set rotation offset " + axis + "=" + value + " for source '" + TEST_SOURCE_ID + "'. (pitch=" + pitch + ", yaw=" + yaw + ", roll=" + roll + ")");
                return true;
            } else {
                sender.sendMessage("Offset type must be translation, rotation, or scale.");
                return true;
            }
        }

        sender.sendMessage("Unknown offset action: " + action + ". Use source or set.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return List.of("source", "set");
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("source")) {
            return List.of("create", "remove");
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
            return List.of("rotation", "translation", "scale");
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            String type = args[2].toLowerCase();
            if (type.equals("translation") || type.equals("scale")) {
                return List.of("x", "y", "z");
            } else if (type.equals("rotation")) {
                return List.of("pitch", "yaw", "roll");
            }
        }
        if (args.length == 5 && args[1].equalsIgnoreCase("set")) {
            return List.of("<value>");
        }
        return List.of();
    }
}