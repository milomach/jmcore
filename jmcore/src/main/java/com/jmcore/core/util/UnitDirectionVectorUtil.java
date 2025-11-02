package com.jmcore.core.util;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Utility for calculating unit direction vectors (forward, backward, left, right, up, down)
 * and all valid combinations (no opposite pairs) for both local (relative to entity rotation)
 * and global (world axes) directions.
 */
public class UnitDirectionVectorUtil {

    /**
     * Enum for local (relative to entity rotation) directions.
     */
    public enum LocalDirection {
        FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN
    }

    /**
     * Enum for global (world axes) directions.
     */
    public enum GlobalDirection {
        NORTH, SOUTH, EAST, WEST, UP, DOWN
    }

    /**
     * Enum for all valid local direction combinations (no opposite pairs).
     * Each value is a set of LocalDirection.
     */
    public enum LocalDirectionCombination {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN,
        FORWARD_LEFT,
        FORWARD_RIGHT,
        BACKWARD_LEFT,
        BACKWARD_RIGHT,
        FORWARD_UP,
        FORWARD_DOWN,
        BACKWARD_UP,
        BACKWARD_DOWN,
        LEFT_UP,
        LEFT_DOWN,
        RIGHT_UP,
        RIGHT_DOWN,
        FORWARD_LEFT_UP,
        FORWARD_LEFT_DOWN,
        FORWARD_RIGHT_UP,
        FORWARD_RIGHT_DOWN,
        BACKWARD_LEFT_UP,
        BACKWARD_LEFT_DOWN,
        BACKWARD_RIGHT_UP,
        BACKWARD_RIGHT_DOWN;

        /**
         * Returns the set of LocalDirection for this combination.
         */
        public Set<LocalDirection> getDirections() {
            String[] parts = this.name().split("_");
            Set<LocalDirection> set = EnumSet.noneOf(LocalDirection.class);
            for (String part : parts) {
                set.add(LocalDirection.valueOf(part));
            }
            return set;
        }
    }

    /**
     * Enum for all valid global direction combinations (no opposite pairs).
     * Each value is a set of GlobalDirection.
     */
    public enum GlobalDirectionCombination {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        UP,
        DOWN,
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_EAST,
        SOUTH_WEST,
        NORTH_UP,
        NORTH_DOWN,
        SOUTH_UP,
        SOUTH_DOWN,
        EAST_UP,
        EAST_DOWN,
        WEST_UP,
        WEST_DOWN,
        NORTH_EAST_UP,
        NORTH_EAST_DOWN,
        NORTH_WEST_UP,
        NORTH_WEST_DOWN,
        SOUTH_EAST_UP,
        SOUTH_EAST_DOWN,
        SOUTH_WEST_UP,
        SOUTH_WEST_DOWN;

        /**
         * Returns the set of GlobalDirection for this combination.
         */
        public Set<GlobalDirection> getDirections() {
            String[] parts = this.name().split("_");
            Set<GlobalDirection> set = EnumSet.noneOf(GlobalDirection.class);
            for (String part : parts) {
                set.add(GlobalDirection.valueOf(part));
            }
            return set;
        }
    }

    // --- Hardcoded global direction unit vectors ---
    public static final Map<GlobalDirection, Vector> GLOBAL_UNIT_VECTORS = Map.of(
            GlobalDirection.NORTH, new Vector(0, 0, -1),
            GlobalDirection.SOUTH, new Vector(0, 0, 1),
            GlobalDirection.EAST,  new Vector(1, 0, 0),
            GlobalDirection.WEST,  new Vector(-1, 0, 0),
            GlobalDirection.UP,    new Vector(0, 1, 0),
            GlobalDirection.DOWN,  new Vector(0, -1, 0)
    );

    // --- Hardcoded global combination unit vectors (normalized) ---
    public static final Map<GlobalDirectionCombination, Vector> GLOBAL_COMBINATION_UNIT_VECTORS = buildGlobalCombinationVectors();

    private static Map<GlobalDirectionCombination, Vector> buildGlobalCombinationVectors() {
        Map<GlobalDirectionCombination, Vector> map = new EnumMap<>(GlobalDirectionCombination.class);
        for (GlobalDirectionCombination combo : GlobalDirectionCombination.values()) {
            Vector sum = new Vector(0, 0, 0);
            for (GlobalDirection dir : combo.getDirections()) {
                sum.add(GLOBAL_UNIT_VECTORS.get(dir));
            }
            map.put(combo, sum.normalize());
        }
        return Collections.unmodifiableMap(map);
    }

    // --- Local direction calculation ---

    /**
     * Returns the unit vector for a local direction, relative to the entity's rotation.
     */
    public static Vector getLocalUnitVector(Entity entity, LocalDirection dir) {
        switch (dir) {
            case FORWARD:  return getForwardVector(entity);
            case BACKWARD: return getForwardVector(entity).multiply(-1);
            case LEFT:     return getRightVector(entity).multiply(-1);
            case RIGHT:    return getRightVector(entity);
            case UP:       return new Vector(0, 1, 0);
            case DOWN:     return new Vector(0, -1, 0);
            default: throw new IllegalArgumentException("Unknown direction: " + dir);
        }
    }

    /**
     * Returns the normalized sum of the unit vectors for the given local direction combination.
     */
    public static Vector getLocalCombinationUnitVector(Entity entity, LocalDirectionCombination combo) {
        Vector sum = new Vector(0, 0, 0);
        for (LocalDirection dir : combo.getDirections()) {
            sum.add(getLocalUnitVector(entity, dir));
        }
        return sum.normalize();
    }

    /**
     * Returns the hardcoded unit vector for a global direction.
     */
    public static Vector getGlobalUnitVector(GlobalDirection dir) {
        return GLOBAL_UNIT_VECTORS.get(dir).clone();
    }

    /**
     * Returns the hardcoded normalized sum for a global direction combination.
     */
    public static Vector getGlobalCombinationUnitVector(GlobalDirectionCombination combo) {
        return GLOBAL_COMBINATION_UNIT_VECTORS.get(combo).clone();
    }

    // --- Internal helpers for local axes ---

    /**
     * Returns the entity's forward unit vector (relative to yaw only).
     * Ignores pitch and roll.
     */
    public static Vector getForwardVector(Entity entity) {
        float yaw = (float) Math.toRadians(entity.getLocation().getYaw());
        double x = -Math.sin(yaw);
        double y = 0;
        double z = Math.cos(yaw);
        return new Vector(x, y, z).normalize();
    }

    /**
     * Returns the entity's right unit vector (relative to yaw only).
     * Ignores pitch and roll.
     */
    public static Vector getRightVector(Entity entity) {
        float yaw = (float) Math.toRadians(entity.getLocation().getYaw() + 90);
        double x = -Math.sin(yaw);
        double y = 0;
        double z = Math.cos(yaw);
        return new Vector(x, y, z).normalize();
    }

    // --- Utility: List all valid local/global combinations ---

    /**
     * Returns all valid local direction combinations (no opposite pairs).
     */
    public static List<LocalDirectionCombination> getAllLocalCombinations() {
        return Arrays.asList(LocalDirectionCombination.values());
    }

    /**
     * Returns all valid global direction combinations (no opposite pairs).
     */
    public static List<GlobalDirectionCombination> getAllGlobalCombinations() {
        return Arrays.asList(GlobalDirectionCombination.values());
    }
}