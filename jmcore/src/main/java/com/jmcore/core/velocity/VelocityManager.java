package com.jmcore.core.velocity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages internal velocity sources for entities.
 * Allows multiple sources to contribute velocity vectors for any entity each tick.
 * Each tick, all sources for each entity are summed and applied via setVelocity.
 * For Player entities, uses player.setVelocity for best client sync.
 * Now supports hybrid control: if all sources set a component to null, the entity's current velocity is used for that component.
 */
public class VelocityManager {

    // Map of entity UUID -> (source key -> NullableVector)
    private final Map<UUID, Map<String, NullableVector>> velocitySources = new ConcurrentHashMap<>();

    // Used to schedule the velocity application each tick
    private VelocityApplierTask velocityTask;

    private final Plugin plugin;

    public VelocityManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers or updates a velocity vector for a given entity and source.
     * Use NullableVector to allow hybrid control (null = use current velocity for that component).
     *
     * @param entity The target entity.
     * @param sourceKey A unique string identifying the velocity source (e.g. "physics", "customJump").
     * @param velocity The velocity vector to contribute for this tick (use NullableVector).
     */
    public void setVelocitySource(Entity entity, String sourceKey, NullableVector velocity) {
        velocitySources
            .computeIfAbsent(entity.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(sourceKey, velocity);
    }

    /**
     * Overload for backward compatibility: treat as fully controlled vector.
     */
    public void setVelocitySource(Entity entity, String sourceKey, Vector velocity) {
        setVelocitySource(entity, sourceKey, NullableVector.fromVector(velocity));
    }

    /**
     * Removes a velocity source for an entity.
     * Call this if a source no longer wants to contribute velocity.
     *
     * @param entity The target entity.
     * @param sourceKey The unique source key.
     */
    public void removeVelocitySource(Entity entity, String sourceKey) {
        Map<String, NullableVector> sources = velocitySources.get(entity.getUniqueId());
        if (sources != null) {
            sources.remove(sourceKey);
            if (sources.isEmpty()) {
                velocitySources.remove(entity.getUniqueId());
            }
        }
    }

    /**
     * Clears all velocity sources for an entity.
     * Useful when an entity is removed or no longer needs velocity management.
     *
     * @param entity The target entity.
     */
    public void clearAllSourcesForEntity(Entity entity) {
        velocitySources.remove(entity.getUniqueId());
    }

    /**
     * Starts the velocity application task (runs every tick).
     * Should be called once, typically on plugin enable.
     */
    public void start() {
        if (velocityTask != null) return;
        velocityTask = new VelocityApplierTask();
        velocityTask.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Stops the velocity application task.
     * Should be called on plugin disable.
     */
    public void stop() {
        if (velocityTask != null) {
            velocityTask.cancel();
            velocityTask = null;
        }
    }

    /**
     * The task that applies summed velocities to all tracked entities each tick.
     * Uses player.setVelocity for Player entities for best client sync.
     * Hybrid logic: if all sources for a component are null, use entity's current velocity for that component.
     */
    private class VelocityApplierTask extends BukkitRunnable {
        @Override
        public void run() {
            for (UUID entityId : velocitySources.keySet()) {
                Entity entity = Bukkit.getEntity(entityId);
                if (entity == null || entity.isDead()) {
                    velocitySources.remove(entityId);
                    continue;
                }
                Map<String, NullableVector> sources = velocitySources.get(entityId);
                if (sources == null || sources.isEmpty()) continue;

                // Hybrid sum logic
                Double sumX = null, sumY = null, sumZ = null;
                boolean anyX = false, anyY = false, anyZ = false;
                boolean allXZero = true, allYZero = true, allZZero = true;

                StringBuilder debug = new StringBuilder();
                debug.append("[VelocityManager] Entity: ").append(entity.getName()).append(" (").append(entity.getUniqueId()).append(")\n");
                debug.append("  Sources:\n");

                for (Map.Entry<String, NullableVector> entry : sources.entrySet()) {
                    NullableVector v = entry.getValue();
                    debug.append("    ").append(entry.getKey()).append(": ");
                    debug.append("x=").append(v.x).append(", y=").append(v.y).append(", z=").append(v.z).append("\n");
                    if (v.x != null) {
                        anyX = true;
                        sumX = (sumX == null ? 0.0 : sumX) + v.x;
                        if (v.x != 0.0) allXZero = false;
                    }
                    if (v.y != null) {
                        anyY = true;
                        sumY = (sumY == null ? 0.0 : sumY) + v.y;
                        if (v.y != 0.0) allYZero = false;
                    }
                    if (v.z != null) {
                        anyZ = true;
                        sumZ = (sumZ == null ? 0.0 : sumZ) + v.z;
                        if (v.z != 0.0) allZZero = false;
                    }
                }

                Vector current = entity.getVelocity();

                boolean usedCurrentX = !anyX;
                boolean usedCurrentY = !anyY;
                boolean usedCurrentZ = !anyZ;

                double finalX = (anyX ? (allXZero ? 0.0 : sumX) : current.getX());
                double finalY = (anyY ? (allYZero ? 0.0 : sumY) : current.getY());
                double finalZ = (anyZ ? (allZZero ? 0.0 : sumZ) : current.getZ());

                Vector total = new Vector(finalX, finalY, finalZ);

                if (usedCurrentX || usedCurrentY || usedCurrentZ) {
                    debug.append("  Used current velocity for: ");
                    if (usedCurrentX) debug.append("X ");
                    if (usedCurrentY) debug.append("Y ");
                    if (usedCurrentZ) debug.append("Z ");
                    debug.append("\n");
                    debug.append("  Current velocity: x=").append(current.getX())
                        .append(", y=").append(current.getY())
                        .append(", z=").append(current.getZ()).append("\n");
                }
                debug.append("  Final velocity applied: x=").append(finalX)
                    .append(", y=").append(finalY)
                    .append(", z=").append(finalZ).append("\n");

                // Print debug for players only (to avoid spam for all entities)
                if (entity instanceof Player) {
                    Bukkit.getLogger().info(debug.toString());
                }

                // Use player.setVelocity for Player entities for best results
                if (entity instanceof Player player) {
                    player.setVelocity(total);
                } else {
                    entity.setVelocity(total);
                }
            }
        }
    }
}