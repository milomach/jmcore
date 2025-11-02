package com.jmcore.core.command.sub.aj_test.sub;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.AJRigSetupUtil;
import com.jmcore.core.aj.rig_instance.AJRigInstance.AJRigScope;
import com.jmcore.core.command.*;

/**
 * Sets up an AJ rig for a player.
 */
public class AJTestSetupSubCommand extends AbstractSubCommand {
    private AJRigManager ajRigManager;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.ajRigManager = provider.get(AJRigManager.class);
    }

    @Override
    public String getName() { return "setup"; }
    @Override
    public String getDescription() { return "Setup AJ rig for a player."; }
    @Override
    public String getUsage() { return "/jmcore ajtest setup <playername>"; }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        Player target = org.bukkit.Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found: " + args[0]);
            return true;
        }
        String exportNamespace = "blueprint";
        String internalId = "blueprint";
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

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return java.util.Collections.emptyList();
    }
}