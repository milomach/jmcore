package com.jmcore.core.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.AnvilView;

import com.jmcore.core.command.*;
import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.AnvilInputData;
import com.jmcore.core.data.component.AnvilMenuData;
import com.jmcore.core.menus.anvilmenu.AnvilDummyItemUtil;

import net.kyori.adventure.text.Component;

@SubCommandInfo
public class AnvilInputMenuSubCommand extends AbstractSubCommand {
    static {
        CommandRegistry.register(new AnvilInputMenuSubCommand());
    }

    private PlayerDataManager playerDataManager;

    @Override
    public void injectDependencies(DependencyProvider provider) {
        this.playerDataManager = provider.get(PlayerDataManager.class);
    }

    @Override
    public String getName() { return "anvilinputmenu"; }
    @Override
    public String getDescription() { return "Open or close an anvil input menu for a player."; }
    @Override
    public String getUsage() { return "/jmcore anvilinputmenu <playername> <open|close>"; }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gui.anvilinputmenu")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[0]);
        if (player == null || !player.isOnline()) {
            sender.sendMessage("Player not found or not online: " + args[0]);
            return true;
        }

        PlayerData data = playerDataManager.get(player);
        if (data == null) {
            sender.sendMessage("No PlayerData found for " + player.getName());
            return true;
        }
        
        AnvilMenuData anvilMenu = data.getComponent(AnvilMenuData.class);
        if (anvilMenu == null) {
            sender.sendMessage("No anvil menu data for player: " + player.getName());
            return true;
        }

        AnvilInputData anvilInput = data.getComponent(AnvilInputData.class);
        if (anvilInput == null) {
            sender.sendMessage("No anvil input data for player: " + player.getName());
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "open" -> {
                InventoryView view = MenuType.ANVIL.create(player, Component.text(""));
                player.openInventory(view);
                anvilMenu.setExampleAnvilMenuView(view);
                anvilMenu.setAnvilMenuOpened(true);

                // Fill the anvil menu slots with dummy items immediately
                Inventory inv = view.getTopInventory();
                inv.setItem(0, AnvilDummyItemUtil.createDummyItem(0, ""));
                inv.setItem(1, AnvilDummyItemUtil.createDummyItem(1, ""));
                inv.setItem(2, AnvilDummyItemUtil.createDummyItem(2, ""));

                // Store the callback in PlayerData
                anvilMenu.setAnvilMenuCallback(anvilView -> {
                    String rename = anvilView.getRenameText();
                    anvilInput.setLastAnvilInput(rename);
                    // You can also trigger an event or message here if needed
                });

                sender.sendMessage("Opened anvil input menu for " + player.getName());
            }

            case "close" -> {
                InventoryView open = player.getOpenInventory();
                if (open instanceof AnvilView) {
                    anvilMenu.setAnvilMenuCallback(null);
                    player.closeInventory();
                    anvilMenu.clearExampleAnvilMenuView();
                    anvilMenu.setAnvilMenuOpened(false);
                    sender.sendMessage("Closed anvil input menu for " + player.getName());
                } else {
                    sender.sendMessage(player.getName() + " does not have an anvil menu open.");
                }
            }

            default -> sender.sendMessage("Usage: " + getUsage());
        }

        return true;
    }

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2) {
            return java.util.Arrays.asList("open", "close");
        }
        return java.util.Collections.emptyList();
    }
}