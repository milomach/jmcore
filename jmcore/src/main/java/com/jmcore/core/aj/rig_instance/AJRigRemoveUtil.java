package com.jmcore.core.aj.rig_instance;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Set;
import java.util.UUID;

/**
 * Utility for removing root and display entities for an AJ rig instance.
 * Supports removing the root entity and any subset of display entities by UUID.
 * For each type, only removes entities that are actually tracked as that type in the rig.
 */
public class AJRigRemoveUtil {

    /**
     * Removes the root entity for the rig.
     * Also removes all display entities of all types.
     */
    public static void removeRootEntity(AJRigInstance rig) {
        // Remove root entity
        if (rig.getRootEntityUUID() != null) {
            Entity root = Bukkit.getEntity(rig.getRootEntityUUID());
            if (root != null && !root.isDead()) {
                root.remove();
            }
            rig.setRootEntityUUID(null);
        }
        // Remove all display entities of all types
        removeBoneEntities(rig, rig.getBoneEntityUUIDs());
        removeItemDisplayEntities(rig, rig.getItemDisplayEntityUUIDs());
        removeBlockDisplayEntities(rig, rig.getBlockDisplayEntityUUIDs());
        removeTextDisplayEntities(rig, rig.getTextDisplayEntityUUIDs());
    }

    /**
     * Removes bone entities for the given set of UUIDs.
     * Filters out any UUIDs that do not correspond to bones of the rig.
     * Updates the set of bone entity UUIDs for the rig.
     */
    public static void removeBoneEntities(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> boneNames = RigEntityUtil.getBoneNamesFromUUIDs(rig, uuids);
        for (String boneName : boneNames) {
            AJRigInstance.BoneInfo boneInfo = rig.getBoneInfo(boneName);
            if (boneInfo != null && boneInfo.entityUUID != null) {
                Entity e = Bukkit.getEntity(boneInfo.entityUUID);
                if (e != null && !e.isDead()) {
                    e.remove();
                }
                rig.removeBoneEntityUUID(boneInfo.entityUUID);
                boneInfo.entityUUID = null;
            }
        }
    }

    /**
     * Removes item display entities for the given set of UUIDs.
     * Filters out any UUIDs that do not correspond to item displays of the rig.
     * Updates the set of item display entity UUIDs for the rig.
     */
    public static void removeItemDisplayEntities(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> itemDisplayNames = RigEntityUtil.getItemDisplayNamesFromUUIDs(rig, uuids);
        for (String name : itemDisplayNames) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null && info.entityUUID != null) {
                Entity e = Bukkit.getEntity(info.entityUUID);
                if (e != null && !e.isDead()) {
                    e.remove();
                }
                rig.removeItemDisplayEntityUUID(info.entityUUID);
                info.entityUUID = null;
            }
        }
    }

    /**
     * Removes block display entities for the given set of UUIDs.
     * Filters out any UUIDs that do not correspond to block displays of the rig.
     * Updates the set of block display entity UUIDs for the rig.
     */
    public static void removeBlockDisplayEntities(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> blockDisplayNames = RigEntityUtil.getBlockDisplayNamesFromUUIDs(rig, uuids);
        for (String name : blockDisplayNames) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null && info.entityUUID != null) {
                Entity e = Bukkit.getEntity(info.entityUUID);
                if (e != null && !e.isDead()) {
                    e.remove();
                }
                rig.removeBlockDisplayEntityUUID(info.entityUUID);
                info.entityUUID = null;
            }
        }
    }

    /**
     * Removes text display entities for the given set of UUIDs.
     * Filters out any UUIDs that do not correspond to text displays of the rig.
     * Updates the set of text display entity UUIDs for the rig.
     */
    public static void removeTextDisplayEntities(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> textDisplayNames = RigEntityUtil.getTextDisplayNamesFromUUIDs(rig, uuids);
        for (String name : textDisplayNames) {
            AJRigInstance.TextDisplayInfo info = rig.getTextDisplayInfo(name);
            if (info != null && info.entityUUID != null) {
                Entity e = Bukkit.getEntity(info.entityUUID);
                if (e != null && !e.isDead()) {
                    e.remove();
                }
                rig.removeTextDisplayEntityUUID(info.entityUUID);
                info.entityUUID = null;
            }
        }
    }

    // --- Overloads for removing a single entity by UUID for each type ---

    public static void removeBoneEntities(AJRigInstance rig, UUID uuid) {
        removeBoneEntities(rig, Set.of(uuid));
    }
    public static void removeItemDisplayEntities(AJRigInstance rig, UUID uuid) {
        removeItemDisplayEntities(rig, Set.of(uuid));
    }
    public static void removeBlockDisplayEntities(AJRigInstance rig, UUID uuid) {
        removeBlockDisplayEntities(rig, Set.of(uuid));
    }
    public static void removeTextDisplayEntities(AJRigInstance rig, UUID uuid) {
        removeTextDisplayEntities(rig, Set.of(uuid));
    }
}