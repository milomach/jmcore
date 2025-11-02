package com.jmcore.core.command.sub.aj_test.sub;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJAnimationSource;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.command.*;

import java.util.List;

public class AJTestAnimationSubCommand extends AbstractSubCommand {
    private static final String TEST_SOURCE_ID = "ajtest_source";
    private AJRigManager ajRigManager;
    private Plugin plugin;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
        this.plugin = provider.get(Plugin.class);
    }

    @Override
    public String getName() { return "animation"; }
    @Override
    public String getDescription() { return "Animation control for AJ rigs."; }
    @Override
    public String getUsage() {
        return "/jmcore ajtest animation <playername> source <create|remove>\n"
            + "/jmcore ajtest animation <playername> set_frame <frame>\n"
            + "/jmcore ajtest animation <playername> animation_name <name>\n"
            + "/jmcore ajtest animation <playername> playing <true|false>\n"
            + "/jmcore ajtest animation <playername> end_behavior <loop|reset|hold>";
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
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

        // --- Animation source management ---
        if (action.equals("source")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /jmcore ajtest animation <playername> source <create|remove>");
                return true;
            }
            String subAction = args[2].toLowerCase();
            switch (subAction) {
                case "create": {
                    if (rig.getAnimationSource(TEST_SOURCE_ID) != null) {
                        sender.sendMessage("Animation source '" + TEST_SOURCE_ID + "' already exists for this rig.");
                        return true;
                    }
                    AJAnimationSource src = rig.createAnimationSource(TEST_SOURCE_ID, 10);
                    if (src != null) {
                        src.setEnabled(true); // Enable the source by default
                        src.setAnimationName("loop"); // Set a default animation name
                        sender.sendMessage("Animation source '" + TEST_SOURCE_ID + "' created for " + target.getName() + ".");
                    } else {
                        sender.sendMessage("Failed to create animation source (already exists?).");
                    }
                    return true;
                }
                case "remove": {
                    if (rig.getAnimationSource(TEST_SOURCE_ID) == null) {
                        sender.sendMessage("No animation source '" + TEST_SOURCE_ID + "' exists for this rig.");
                        return true;
                    }
                    rig.removeAnimationSource(TEST_SOURCE_ID);
                    sender.sendMessage("Animation source '" + TEST_SOURCE_ID + "' removed for " + target.getName() + ".");
                    return true;
                }
                default:
                    sender.sendMessage("Unknown source action: " + subAction + ". Use create or remove.");
                    return true;
            }
        }

        AJAnimationSource src = rig.getAnimationSource(TEST_SOURCE_ID);
        if (src == null) {
            sender.sendMessage("No animation source '" + TEST_SOURCE_ID + "' exists for this rig. Use '/jmcore ajtest animation <playername> source create' first.");
            return true;
        }

        switch (action) {
            case "set_frame": {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /jmcore ajtest animation <playername> set_frame <frame>");
                    return true;
                }
                if (src.isPlaying()) {
                    sender.sendMessage("Cannot set frame while animation is playing.");
                    return true;
                }
                int frame;
                try {
                    frame = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid frame value.");
                    return true;
                }
                int frameCount = com.jmcore.core.aj.data.AJFrameData.getFrameCount(rig.getExportNamespace(), src.getAnimationName());
                if (frame < 0 || frame >= frameCount) {
                    sender.sendMessage("Frame out of range. Animation '" + src.getAnimationName() + "' has " + frameCount + " frames.");
                    return true;
                }
                src.setCurrentFrame(frame);
                sender.sendMessage("Set current frame to " + frame + " for animation '" + src.getAnimationName() + "' for " + target.getName());
                return true;
            }
            case "animation_name": {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /jmcore ajtest animation <playername> animation_name <name>");
                    return true;
                }
                String name = args[2];
                src.setAnimationName(name);
                sender.sendMessage("Set animation name to '" + name + "' for " + target.getName());
                return true;
            }
            case "playing": {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /jmcore ajtest animation <playername> playing <true|false>");
                    return true;
                }
                boolean playing;
                if (args[2].equalsIgnoreCase("true")) {
                    playing = true;
                } else if (args[2].equalsIgnoreCase("false")) {
                    playing = false;
                } else {
                    sender.sendMessage("Invalid value for playing. Use true or false.");
                    return true;
                }
                src.setPlaying(playing);
                sender.sendMessage("Set playing to " + playing + " for animation '" + src.getAnimationName() + "' for " + target.getName());
                return true;
            }
            case "end_behavior": {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /jmcore ajtest animation <playername> end_behavior <loop|reset|hold>");
                    return true;
                }
                String eb = args[2].toLowerCase();
                AJAnimationSource.EndBehavior endBehavior;
                switch (eb) {
                    case "loop": endBehavior = AJAnimationSource.EndBehavior.LOOP; break;
                    case "reset": endBehavior = AJAnimationSource.EndBehavior.RESET; break;
                    case "hold": endBehavior = AJAnimationSource.EndBehavior.HOLD; break;
                    default:
                        sender.sendMessage("Unknown end_behavior: " + eb + ". Use loop, reset, or hold.");
                        return true;
                }
                src.setEndBehavior(endBehavior);
                sender.sendMessage("Set end_behavior to " + eb + " for animation '" + src.getAnimationName() + "' for " + target.getName());
                return true;
            }
            default:
                sender.sendMessage("Unknown animation action: " + action + ". Use source, set_frame, animation_name, playing, or end_behavior.");
                return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return List.of("source", "set_frame", "animation_name", "playing", "end_behavior");
        }
        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            if (sub.equals("source")) {
                return List.of("create", "remove");
            }
            if (sub.equals("playing")) {
                return List.of("true", "false");
            }
            if (sub.equals("end_behavior")) {
                return List.of("loop", "reset", "hold");
            }
            if (sub.equals("set_frame")) {
                return List.of("<frame>");
            }
            if (sub.equals("animation_name")) {
                // Optionally suggest known animation names
                if (sender instanceof Player player) {
                    AJRigInstance rig = ajRigManager.getRig(player, "blueprint");
                    if (rig != null) {
                        return new java.util.ArrayList<>(com.jmcore.core.aj.data.AJFrameData.getAllAnimationNames(rig.getExportNamespace()));
                    }
                }
                return List.of("<animation_name>");
            }
        }
        return List.of();
    }
}