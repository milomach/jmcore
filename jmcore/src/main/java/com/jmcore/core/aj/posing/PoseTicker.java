package com.jmcore.core.aj.posing;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.*;

import java.util.*;

/**
 * PoseTicker is the main BukkitRunnable that drives the pose ticker system.
 * It runs every tick and processes all rigs (player and global), updating their pose state and applying transforms.
 * 
 * The ticker only runs pose logic if there is at least one rig with an animation source.
 * For each rig, it performs the following steps:
 *   1. Prepares pose state for the tick (clears previous state, sets up for new calculations).
 *   2. Processes all offset sources for the rig, accumulating offset transforms.
 *   3. Processes all animation sources for the rig, accumulating animation transforms and advancing frames.
 *   4. Batches and applies the final calculated transforms to entities and locators.
 * 
 * This class is started once on plugin enable and runs until plugin disable.
 */
public class PoseTicker extends BukkitRunnable {
    private final AJRigManager rigManager;
    private final Plugin plugin;

    public PoseTicker(AJRigManager rigManager, Plugin plugin) {
        this.rigManager = rigManager;
        this.plugin = plugin;
    }

    /**
     * Called every tick.
     * Processes all rigs and applies pose logic if there is at least one rig with a summoned entity.
     */
    @Override
    public void run() {
        // Collect all player and global rigs into a single collection
        Collection<AJRigInstance> allRigs = new ArrayList<>(rigManager.getAllPlayerRigs());
        allRigs.addAll(rigManager.getAllGlobalRigs());

        // Only run pose logic if at least one rig exists and has a summoned entity.
        // Check the rig's entity UUID sets (bones, item displays, block displays, text displays).
        boolean anySummoned = allRigs.stream().anyMatch(rig ->
                !rig.getBoneEntityUUIDs().isEmpty() ||
                !rig.getItemDisplayEntityUUIDs().isEmpty() ||
                !rig.getBlockDisplayEntityUUIDs().isEmpty() ||
                !rig.getTextDisplayEntityUUIDs().isEmpty()
        );
        if (!anySummoned) return;

        // For each rig, run the steps of the pose ticker
        for (AJRigInstance rig : allRigs) {
            // Step 1: Prepare pose state for this tick (clear previous, set up current)
            PoseStateManager.prepareForTick(rig);
            // Step 2: Process all offset sources (accumulate offset transforms)
            PoseStateManager.processOffsetSources(rig);
            // Step 3: Process all animation sources (accumulate animation transforms, advance frames)
            PoseStateManager.processAnimationSources(rig);
            // Step 4: Batch and apply final transforms to entities/locators
            PoseStateManager.batchApplyTransforms(rig, plugin);
        }
    }

    /**
     * Utility to start the pose ticker as a repeating Bukkit task (redundant, because this is done directly already).
    */
    public static void startTicker(AJRigManager rigManager, Plugin plugin) {
        new PoseTicker(rigManager, plugin).runTaskTimer(plugin, 0L, 1L);
    }
}