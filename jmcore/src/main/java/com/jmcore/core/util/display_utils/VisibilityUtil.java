package com.jmcore.core.util.display_utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class VisibilityUtil {

    /**
     * Set the default visibility for an entity (affects new viewers).
     * Does NOT update visibility for any current players.
     */
    public static void setDefaultVisibility(Entity entity, boolean visibleByDefault) {
        entity.setVisibleByDefault(visibleByDefault);
    }

    /**
     * Set the default visibility for a set of entities (affects new viewers).
     * Does NOT update visibility for any current players.
     */
    public static void setDefaultVisibility(Set<Entity> entities, boolean visibleByDefault) {
        for (Entity e : entities) {
            setDefaultVisibility(e, visibleByDefault);
        }
    }

    /**
     * Show or hide a single entity for a set of players.
     */
    public static void setVisibilityForPlayers(Entity entity, Plugin plugin, Set<Player> players, boolean show) {
        for (Player p : players) {
            if (show) {
                p.showEntity(plugin, entity);
            } else {
                p.hideEntity(plugin, entity);
            }
        }
    }

    /**
     * Show or hide a set of entities for a set of players.
     */
    public static void setVisibilityForPlayers(Set<Entity> entities, Plugin plugin, Set<Player> players, boolean show) {
        for (Entity e : entities) {
            setVisibilityForPlayers(e, plugin, players, show);
        }
    }
}