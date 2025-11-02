package com.jmcore.core.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;

public class GetEntitiesUtil {

    /**
     * Returns all entities from the provided UUID sets that have all of the specified tags.
     * @param uuidSets a collection of UUID collections (e.g. List<Set<UUID>>)
     * @param tags tags that must all be present on the entity
     */
    public static Set<Entity> getEntitiesByTags(Collection<? extends Collection<UUID>> uuidSets, String... tags) {
        Set<Entity> result = new HashSet<>();
        Set<String> tagSet = new HashSet<>(Arrays.asList(tags));
        for (Collection<UUID> uuids : uuidSets) {
            for (UUID uuid : uuids) {
                Entity e = Bukkit.getEntity(uuid);
                if (e != null && e.getScoreboardTags().containsAll(tagSet)) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    /**
     * Returns all entities from the provided UUID sets that are of one of the specified types.
     * @param uuidSets a collection of UUID collections (e.g. List<Set<UUID>>)
     * @param types entity types to match
     */
    public static Set<Entity> getEntitiesByType(Collection<? extends Collection<UUID>> uuidSets, EntityType... types) {
        Set<Entity> result = new HashSet<>();
        Set<EntityType> typeSet = new HashSet<>(Arrays.asList(types));
        for (Collection<UUID> uuids : uuidSets) {
            for (UUID uuid : uuids) {
                Entity e = Bukkit.getEntity(uuid);
                if (e != null && typeSet.contains(e.getType())) {
                    result.add(e);
                }
            }
        }
        return result;
    }
}