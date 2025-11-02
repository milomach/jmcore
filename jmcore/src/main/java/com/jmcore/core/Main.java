package com.jmcore.core;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.data.AJBlockDisplayData;
import com.jmcore.core.aj.data.AJBoneData;
import com.jmcore.core.aj.data.AJDefaultPoseData;
import com.jmcore.core.aj.data.AJFrameData;
import com.jmcore.core.aj.data.AJItemDisplayData;
import com.jmcore.core.aj.data.AJLocatorData;
import com.jmcore.core.aj.data.AJRootData;
import com.jmcore.core.aj.data.AJTextDisplayData;
import com.jmcore.core.aj.data.AJVariantData;
import com.jmcore.core.aj.posing.PoseTicker;
import com.jmcore.core.aj.rig_instance.AJRigCleanupUtil;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.AJRigInstance.AJRigScope;
import com.jmcore.core.command.*;
import com.jmcore.core.cursor.HeadRotationTracker;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.input.AnvilInputPoller;
import com.jmcore.core.input.AttackInteractInputPoller;
import com.jmcore.core.input.MovementInputPoller;
import com.jmcore.core.input.listeners.DropItemInputListener;
import com.jmcore.core.input.listeners.InventoryClickInputListener;
import com.jmcore.core.input.listeners.ItemHeldInputListener;
import com.jmcore.core.input.listeners.JumpInputListener;
import com.jmcore.core.input.listeners.SneakInputListener;
import com.jmcore.core.input.listeners.SprintInputListener;
import com.jmcore.core.input.listeners.SwapHandInputListener;
import com.jmcore.core.menus.anvilmenu.AnvilMenuDummyItemMaintenance;
import com.jmcore.core.menus.anvilmenu.ExampleAnvilMenuCloseListener;
import com.jmcore.core.player.PlayerListener;
import com.jmcore.core.player.state.managers.CursorCleanup;
import com.jmcore.core.player.state.managers.CursorMaintenance;
import com.jmcore.core.player.state.managers.MovementCleanup;
import com.jmcore.core.player_movement.PlayerMovementSystem;
import com.jmcore.core.velocity.VelocityManager;

import org.bukkit.plugin.Plugin;

import java.util.Collection;

public final class Main extends JavaPlugin implements DependencyProvider {
    private HeadRotationTracker tracker;
    private PlayerDataManager playerDataManager;
    private AJRigManager ajRigManager;
    private VelocityManager velocityManager;
    private MovementInputPoller movementInputPoller;
    private AttackInteractInputPoller attackInteractInputPoller;
    private CursorMaintenance cursorMaintenance;
    private CursorCleanup cursorCleanup;
    private AnvilInputPoller anvilInputPoller;
    private PlayerMovementSystem playerMovementSystem;
    private PoseTicker poseTicker;

    @Override
    public void onEnable() {
        getLogger().info("JMCore has been enabled!");

        // Register player data components (triggers static registration)
        try {
            Class.forName("com.jmcore.core.data.component.AnvilInputData");
            Class.forName("com.jmcore.core.data.component.AnvilMenuData");
            Class.forName("com.jmcore.core.data.component.AttackInteractInputData");
            Class.forName("com.jmcore.core.data.component.CursorData");
            Class.forName("com.jmcore.core.data.component.DebugData");
            Class.forName("com.jmcore.core.data.component.GeneralData");
            Class.forName("com.jmcore.core.data.component.MovementInputData");
            Class.forName("com.jmcore.core.data.component.PlayerStateData");
            Class.forName("com.jmcore.core.player_movement.data.MovementConfigData");
            Class.forName("com.jmcore.core.player_movement.data.OnGroundData");
            Class.forName("com.jmcore.core.player_movement.data.InAirData");
            Class.forName("com.jmcore.core.player_movement.data.InLavaData");
            Class.forName("com.jmcore.core.player_movement.data.InWaterData");
            // Add more as you create new components
        } catch (ClassNotFoundException e) {
            getLogger().warning("Failed to load player data components: " + e.getMessage());
        }

        playerDataManager = new PlayerDataManager();
        ajRigManager = new AJRigManager();
        new com.jmcore.core.input.InputDebugListener(playerDataManager);
        cursorMaintenance = new CursorMaintenance(this, playerDataManager);
        cursorCleanup = new CursorCleanup(playerDataManager, cursorMaintenance);
        tracker = new HeadRotationTracker(this, playerDataManager, cursorMaintenance);
        tracker.runTaskTimer(this, 1, 1); // Run every tick

        // Register velocity manager
        velocityManager = new VelocityManager(this);
        velocityManager.start();

        // Register pose system (runs every tick)
        poseTicker = new PoseTicker(ajRigManager, this);
        poseTicker.runTaskTimer(this, 0L, 1L);

        // Register player movement system (runs every tick)
        playerMovementSystem = new PlayerMovementSystem(playerDataManager, velocityManager, this);
        playerMovementSystem.runTaskTimer(this, 1, 1);
        
        // Register command
        getCommand("jmcore").setExecutor(new RootCommand());
        getCommand("jmcore").setTabCompleter(new RootCommand());
        
        // Set dependency provider before loading subcommands
        CommandRegistry.setDependencyProvider(this);
        
        // Register subcommands (triggers static registration)
        try {
            Class.forName("com.jmcore.core.command.sub.GUICursorSubCommand");
            Class.forName("com.jmcore.core.command.sub.GUIDebugSubCommand");
            Class.forName("com.jmcore.core.command.sub.AnvilInputMenuSubCommand");
            Class.forName("com.jmcore.core.command.sub.aj_test.AJTestSubCommand");
            Class.forName("com.jmcore.core.command.sub.PlayerMovementSubCommand");
            // Add more as you create new subcommands
        } catch (ClassNotFoundException e) {
            getLogger().warning("Failed to load subcommands: " + e.getMessage());
        }
        
        // Load all AJ data types at startup
        AJBoneData.loadAllBoneData();
        AJItemDisplayData.loadAllItemDisplayData();
        AJBlockDisplayData.loadAllBlockDisplayData();
        AJTextDisplayData.loadAllTextDisplayData();
        AJFrameData.loadAllAnimations();
        AJLocatorData.loadAllLocatorData();
        AJRootData.loadAllRootData();
        AJVariantData.loadAllVariantData();
        AJDefaultPoseData.loadAllDefaultPoses();
        
        // Register player event listener (pass tracker)
        getServer().getPluginManager().registerEvents(new PlayerListener(playerDataManager, tracker, cursorCleanup, ajRigManager, this), this);

        // Register movement input poller
        movementInputPoller = new MovementInputPoller(playerDataManager);
        movementInputPoller.runTaskTimer(this, 1, 1); // Run every tick
        
        // Register attack interact input poller
        attackInteractInputPoller = new AttackInteractInputPoller(playerDataManager);
        attackInteractInputPoller.runTaskTimer(this, 1, 1); // every tick

        // Register anvil input poller
        anvilInputPoller = new AnvilInputPoller(playerDataManager);
        anvilInputPoller.runTaskTimer(this, 1, 1);

        // Resister various input listeners
        getServer().getPluginManager().registerEvents(new SneakInputListener(), this);
        getServer().getPluginManager().registerEvents(new SprintInputListener(), this);
        getServer().getPluginManager().registerEvents(new SwapHandInputListener(), this);
        getServer().getPluginManager().registerEvents(new DropItemInputListener(), this);
        getServer().getPluginManager().registerEvents(new ItemHeldInputListener(), this);
        getServer().getPluginManager().registerEvents(new JumpInputListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickInputListener(), this);

        // Register anvil input menu example listeners
        getServer().getPluginManager().registerEvents(new ExampleAnvilMenuCloseListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new AnvilMenuDummyItemMaintenance(playerDataManager), this);
    }

    @Override
    public void onDisable() {
        if (tracker != null) tracker.cancel();
        if (movementInputPoller != null) movementInputPoller.cancel();
        if (attackInteractInputPoller != null) attackInteractInputPoller.cancel();
        if (anvilInputPoller != null) anvilInputPoller.cancel();
        if (poseTicker != null) {
            poseTicker.cancel();
        }

        // TEMPORARY: Clear all player data to avoid memory leaks on reload/stop
        if (playerDataManager != null) {
            playerDataManager.clearAll();
        }

        // Remove all player rigs and their entities (including locators)
        if (ajRigManager != null) {
            // Clean up all player rigs
            for (Player player : getServer().getOnlinePlayers()) {
                Collection<AJRigInstance> rigs = ajRigManager.getAllPlayerRigs();
                for (AJRigInstance rig : rigs) {
                    // Pass all required arguments
                    AJRigCleanupUtil.cleanupRig(rig, ajRigManager, AJRigScope.PLAYER, player);
                }
                ajRigManager.removeAllRigsForPlayer(player);
            }
            // Clean up all global rigs
            Collection<AJRigInstance> globalRigs = ajRigManager.getAllGlobalRigs();
            for (AJRigInstance rig : globalRigs) {
                // Pass null for player
                AJRigCleanupUtil.cleanupRig(rig, ajRigManager, AJRigScope.GLOBAL, null);
            }
            ajRigManager.clearAllRigs();
        }
        // Clean up all tracked players' player state
        for (Player player : getServer().getOnlinePlayers()) {
            cursorCleanup.cleanup(player);
            MovementCleanup.cleanup(player);
        }
        // Remove all player data (not persistent)
        playerDataManager.getAll().clear();

        getLogger().info("GUI has been disabled.");
    }

    // DependencyProvider implementation for subcommand injection
    @Override
    public <T> T get(Class<T> type) {
        if (type == HeadRotationTracker.class) return type.cast(tracker);
        if (type == PlayerDataManager.class) return type.cast(playerDataManager);
        if (type == CursorMaintenance.class) return type.cast(cursorMaintenance);
        if (type == CursorCleanup.class) return type.cast(cursorCleanup);
        if (type == Plugin.class) return type.cast(this);
        if (type == AJRigManager.class) return type.cast(ajRigManager);
        if (type == PlayerMovementSystem.class) return type.cast(playerMovementSystem);
        // ... add more as needed ...
        return null;
    }
}