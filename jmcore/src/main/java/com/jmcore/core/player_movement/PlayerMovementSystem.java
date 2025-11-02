package com.jmcore.core.player_movement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.MovementInputData;
import com.jmcore.core.input.InputType;
import com.jmcore.core.player_movement.data.*;
import com.jmcore.core.player_movement.util.GroundDetectionUtil;
import com.jmcore.core.util.UnitDirectionVectorUtil;
import com.jmcore.core.velocity.NullableVector;
import com.jmcore.core.velocity.VelocityManager;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * Example custom player movement system using the internal velocity system.
 * - Handles per-direction velocity sources for forward, backward, left, right, and jump.
 * - Applies friction to directions with no input.
 * - Handles jump and gravity logic.
 */
public class PlayerMovementSystem extends BukkitRunnable {
    private final PlayerDataManager playerDataManager;
    private final VelocityManager velocityManager;
    private final Plugin plugin;
    private final Set<UUID> enabledPlayers = new HashSet<>();

    // Track per-player, per-direction horizontal velocities for friction
    private final Map<UUID, Map<InputType, Vector>> horizontalVelocities = new java.util.HashMap<>();
    // Track per-player vertical velocity for jump/gravity
    private final Map<UUID, Double> verticalVelocities = new java.util.HashMap<>();

    public PlayerMovementSystem(PlayerDataManager playerDataManager, VelocityManager velocityManager, Plugin plugin) {
        this.playerDataManager = playerDataManager;
        this.velocityManager = velocityManager;
        this.plugin = plugin;
    }

    public void enableForPlayer(Player player) {
        enabledPlayers.add(player.getUniqueId());
    }

    public void disableForPlayer(Player player) {
        enabledPlayers.remove(player.getUniqueId());
        velocityManager.clearAllSourcesForEntity(player);
        horizontalVelocities.remove(player.getUniqueId());
        verticalVelocities.remove(player.getUniqueId());
    }

    public boolean isEnabled(Player player) {
        return enabledPlayers.contains(player.getUniqueId());
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isEnabled(player)) continue;
            PlayerData data = playerDataManager.get(player);
            if (data == null) continue;

            MovementInputData input = data.getComponent(MovementInputData.class);
            MovementConfigData config = data.getComponent(MovementConfigData.class);
            OnGroundData onGround = data.getComponent(OnGroundData.class);
            InAirData inAir = data.getComponent(InAirData.class);

            if (input == null || config == null || onGround == null || inAir == null) continue;

            boolean onGroundState = GroundDetectionUtil.isOnGround(player);

            Bukkit.getLogger().info("[PlayerMovementSystem] " + player.getName() + " onGroundState=" + onGroundState);

            // --- Horizontal movement sources ---
            boolean forward = input.isInputPressed(InputType.FORWARD);
            boolean backward = input.isInputPressed(InputType.BACKWARD);
            boolean left = input.isInputPressed(InputType.LEFT);
            boolean right = input.isInputPressed(InputType.RIGHT);

            if (forward && backward) { forward = false; backward = false; }
            if (left && right) { left = false; right = false; }

            Map<InputType, Boolean> activeDirections = Map.of(
                InputType.FORWARD, forward,
                InputType.BACKWARD, backward,
                InputType.LEFT, left,
                InputType.RIGHT, right
            );

            Map<InputType, Vector> playerHVel = horizontalVelocities.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(InputType.class));

            for (InputType dir : new InputType[]{InputType.FORWARD, InputType.BACKWARD, InputType.LEFT, InputType.RIGHT}) {
                String velocitySourceKey = "move_" + dir.name().toLowerCase();
                boolean pressed = activeDirections.get(dir);

                if (pressed) {
                    double magnitude = 0.0;
                    boolean isDiagonal = (forward || backward) && (left || right);
                    if (onGroundState) {
                        if (dir == InputType.FORWARD) magnitude = config.getForwardMagnitude();
                        else if (dir == InputType.BACKWARD) magnitude = config.getBackwardMagnitude();
                        else magnitude = config.getStrafeMagnitude();

                        if (isDiagonal) magnitude *= config.getDiagonalFactor();

                        if (input.isInputPressed(InputType.SNEAK)) {
                            magnitude = config.getSneakMagnitude();
                        } else if (input.isInputPressed(InputType.SPRINT) && (dir == InputType.FORWARD || dir == InputType.LEFT || dir == InputType.RIGHT)) {
                            magnitude = config.getSprintMagnitude();
                        }
                    } else {
                        magnitude = config.getForwardMagnitude();
                        if (isDiagonal) magnitude *= config.getDiagonalFactor();
                    }

                    UnitDirectionVectorUtil.LocalDirection localDir = switch (dir) {
                        case FORWARD -> UnitDirectionVectorUtil.LocalDirection.FORWARD;
                        case BACKWARD -> UnitDirectionVectorUtil.LocalDirection.BACKWARD;
                        case LEFT -> UnitDirectionVectorUtil.LocalDirection.LEFT;
                        case RIGHT -> UnitDirectionVectorUtil.LocalDirection.RIGHT;
                        default -> throw new IllegalStateException();
                    };
                    Vector directionVec = UnitDirectionVectorUtil.getLocalUnitVector(player, localDir);

                    // Only set X and Z, Y is always null for horizontal movement
                    NullableVector horizontalVelocity = new NullableVector(
                        directionVec.getX() * magnitude,
                        null,
                        directionVec.getZ() * magnitude
                    );
                    playerHVel.put(dir, directionVec.multiply(magnitude));

                    velocityManager.setVelocitySource(player, velocitySourceKey, horizontalVelocity);
                } else {
                    Vector prevVel = playerHVel.get(dir);
                    if (prevVel != null && prevVel.lengthSquared() > 0.0001) {
                        double friction = onGround.getFriction();
                        Vector decayed = prevVel.clone().multiply(1.0 - friction);
                        if (decayed.lengthSquared() < 0.0001) {
                            decayed.zero();
                        }
                        playerHVel.put(dir, decayed);
                        // Y is always null for horizontal movement, even when decaying
                        NullableVector decayedHorizontal = new NullableVector(
                            decayed.getX(),
                            null,
                            decayed.getZ()
                        );
                        velocityManager.setVelocitySource(player, velocitySourceKey, decayedHorizontal);
                    } else {
                        // Y is always null for horizontal movement, even when zero
                        velocityManager.setVelocitySource(player, velocitySourceKey, new NullableVector(0.0, null, 0.0));
                    }
                }
            }

            // --- Vertical movement source (jump/gravity) ---
            String verticalSourceKey = "move_vertical";
            double verticalVel = verticalVelocities.getOrDefault(player.getUniqueId(), 0.0);
            boolean jumpPressed = input.isInputPressed(InputType.JUMP);

            if (onGroundState) {
                if (onGround.isJumping()) {
                    onGround.setJumping(false);
                }
                if (jumpPressed && !onGround.isJumping()) {
                    verticalVel = config.getJumpMagnitude();
                    onGround.setJumping(true);
                    Vector upVec = UnitDirectionVectorUtil.getLocalUnitVector(player, UnitDirectionVectorUtil.LocalDirection.UP);
                    Vector verticalVelocity = upVec.multiply(verticalVel);
                    Bukkit.getLogger().info("[PlayerMovementSystem] " + player.getName() + " JUMP: setting vertical velocity to " + verticalVelocity);
                    velocityManager.setVelocitySource(player, verticalSourceKey, verticalVelocity);
                } else {
                    Bukkit.getLogger().info("[PlayerMovementSystem] " + player.getName() + " ON GROUND: setting vertical velocity Y to null (hybrid mode)");
                    velocityManager.setVelocitySource(player, verticalSourceKey, new NullableVector(0.0, null, 0.0));
                }
            } else {
                verticalVel -= inAir.getGravity();
                Vector upVec = UnitDirectionVectorUtil.getLocalUnitVector(player, UnitDirectionVectorUtil.LocalDirection.UP);
                Vector verticalVelocity = upVec.multiply(verticalVel);
                Bukkit.getLogger().info("[PlayerMovementSystem] " + player.getName() + " IN AIR: setting vertical velocity to " + verticalVelocity);
                velocityManager.setVelocitySource(player, verticalSourceKey, verticalVelocity);
            }

            verticalVelocities.put(player.getUniqueId(), verticalVel);
        }
    }
}