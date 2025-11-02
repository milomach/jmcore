package com.jmcore.core.aj.rig_instance;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import com.jmcore.core.aj.data.AJBoneData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility for looking up and filtering rig entities and names for all supported types.
 */
public final class RigEntityUtil {
    private RigEntityUtil() {}

    // --- Filtering by name ---

    /**
     * Filters a set of bone names to only those present in the rig instance.
     */
    public static Set<String> filterBoneNames(AJRigInstance rig, Set<String> boneNames) {
        return boneNames.stream().filter(rig.getBoneNames()::contains).collect(Collectors.toSet());
    }

    /**
     * Filters a set of item display names to only those present in the rig instance.
     */
    public static Set<String> filterItemDisplayNames(AJRigInstance rig, Set<String> itemDisplayNames) {
        return itemDisplayNames.stream().filter(rig.getItemDisplayNames()::contains).collect(Collectors.toSet());
    }

    /**
     * Filters a set of block display names to only those present in the rig instance.
     */
    public static Set<String> filterBlockDisplayNames(AJRigInstance rig, Set<String> blockDisplayNames) {
        return blockDisplayNames.stream().filter(rig.getBlockDisplayNames()::contains).collect(Collectors.toSet());
    }

    /**
     * Filters a set of text display names to only those present in the rig instance.
     */
    public static Set<String> filterTextDisplayNames(AJRigInstance rig, Set<String> textDisplayNames) {
        return textDisplayNames.stream().filter(rig.getTextDisplayNames()::contains).collect(Collectors.toSet());
    }

    // --- Name lookup from entity ---

    /**
     * Gets the bone name for a given entity by searching BoneInfo for a matching UUID.
     */
    public static String getBoneNameFromEntity(Entity entity, AJRigInstance rig) {
        if (entity == null || rig == null) return null;
        UUID uuid = entity.getUniqueId();
        for (AJRigInstance.BoneInfo info : rig.getAllBoneInfos()) {
            if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                return info.boneName;
            }
        }
        return null;
    }

    /**
     * Gets the item display name for a given entity by searching ItemDisplayInfo for a matching UUID.
     */
    public static String getItemDisplayNameFromEntity(Entity entity, AJRigInstance rig) {
        if (entity == null || rig == null) return null;
        UUID uuid = entity.getUniqueId();
        for (AJRigInstance.ItemDisplayInfo info : rig.getAllItemDisplayInfos()) {
            if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                return info.itemDisplayName;
            }
        }
        return null;
    }

    /**
     * Gets the block display name for a given entity by searching BlockDisplayInfo for a matching UUID.
     */
    public static String getBlockDisplayNameFromEntity(Entity entity, AJRigInstance rig) {
        if (entity == null || rig == null) return null;
        UUID uuid = entity.getUniqueId();
        for (AJRigInstance.BlockDisplayInfo info : rig.getAllBlockDisplayInfos()) {
            if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                return info.blockDisplayName;
            }
        }
        return null;
    }

    /**
     * Gets the text display name for a given entity by searching TextDisplayInfo for a matching UUID.
     */
    public static String getTextDisplayNameFromEntity(Entity entity, AJRigInstance rig) {
        if (entity == null || rig == null) return null;
        UUID uuid = entity.getUniqueId();
        for (AJRigInstance.TextDisplayInfo info : rig.getAllTextDisplayInfos()) {
            if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                return info.textDisplayName;
            }
        }
        return null;
    }

    /**
     * Gets the locator name for a given entity by searching LocatorInfo for a matching UUID.
     */
    public static String getLocatorNameFromEntity(Entity entity, AJRigInstance rig) {
        if (entity == null || rig == null) return null;
        UUID uuid = entity.getUniqueId();
        for (AJRigInstance.LocatorInfo info : rig.getAllLocatorInfos()) {
            if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                return info.locatorName;
            }
        }
        return null;
    }

    // --- Entity/UUID lookup by name ---

    /**
     * Gets the bone entity UUID for a given bone name using BoneInfo.
     */
    public static UUID getBoneEntityUUIDByName(AJRigInstance rig, String boneName) {
        AJRigInstance.BoneInfo info = rig.getBoneInfo(boneName);
        return (info != null) ? info.entityUUID : null;
    }

    /**
     * Gets the item display entity UUID for a given item display name using ItemDisplayInfo.
     */
    public static UUID getItemDisplayEntityUUIDByName(AJRigInstance rig, String name) {
        AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
        return (info != null) ? info.entityUUID : null;
    }

    /**
     * Gets the block display entity UUID for a given block display name using BlockDisplayInfo.
     */
    public static UUID getBlockDisplayEntityUUIDByName(AJRigInstance rig, String name) {
        AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
        return (info != null) ? info.entityUUID : null;
    }

    /**
     * Gets the text display entity UUID for a given text display name using TextDisplayInfo.
     */
    public static UUID getTextDisplayEntityUUIDByName(AJRigInstance rig, String name) {
        AJRigInstance.TextDisplayInfo info = rig.getTextDisplayInfo(name);
        return (info != null) ? info.entityUUID : null;
    }

    /**
     * Gets the locator entity UUID for a given locator name using LocatorInfo.
     */
    public static UUID getLocatorEntityUUIDByName(AJRigInstance rig, String locatorName) {
        AJRigInstance.LocatorInfo info = rig.getLocatorInfo(locatorName);
        return (info != null) ? info.entityUUID : null;
    }

    /**
     * Returns the bone entity for the given bone name.
     */
    public static Entity getBoneEntityByName(AJRigInstance rig, String boneName) {
        UUID uuid = getBoneEntityUUIDByName(rig, boneName);
        return uuid != null ? Bukkit.getEntity(uuid) : null;
    }

    /**
     * Returns the item display entity for the given item display name.
     */
    public static Entity getItemDisplayEntityByName(AJRigInstance rig, String name) {
        UUID uuid = getItemDisplayEntityUUIDByName(rig, name);
        return uuid != null ? Bukkit.getEntity(uuid) : null;
    }

    /**
     * Returns the block display entity for the given block display name.
     */
    public static Entity getBlockDisplayEntityByName(AJRigInstance rig, String name) {
        UUID uuid = getBlockDisplayEntityUUIDByName(rig, name);
        return uuid != null ? Bukkit.getEntity(uuid) : null;
    }

    /**
     * Returns the text display entity for the given text display name.
     */
    public static Entity getTextDisplayEntityByName(AJRigInstance rig, String name) {
        UUID uuid = getTextDisplayEntityUUIDByName(rig, name);
        return uuid != null ? Bukkit.getEntity(uuid) : null;
    }

    /**
     * Returns the locator entity for the given locator name.
     */
    public static Entity getLocatorEntityByName(AJRigInstance rig, String locatorName) {
        UUID uuid = getLocatorEntityUUIDByName(rig, locatorName);
        return uuid != null ? Bukkit.getEntity(uuid) : null;
    }

    // --- Name lookup from UUID sets ---

    /**
     * Returns a set of bone names for the given set of UUIDs, filtered to only those that are bone entities of the rig.
     */
    public static Set<String> getBoneNamesFromUUIDs(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> result = new HashSet<>();
        for (UUID uuid : uuids) {
            for (AJRigInstance.BoneInfo info : rig.getAllBoneInfos()) {
                if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                    result.add(info.boneName);
                }
            }
        }
        return result;
    }

    /**
     * Returns a set of item display names for the given set of UUIDs, filtered to only those that are item display entities of the rig.
     */
    public static Set<String> getItemDisplayNamesFromUUIDs(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> result = new HashSet<>();
        for (UUID uuid : uuids) {
            for (AJRigInstance.ItemDisplayInfo info : rig.getAllItemDisplayInfos()) {
                if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                    result.add(info.itemDisplayName);
                }
            }
        }
        return result;
    }

    /**
     * Returns a set of block display names for the given set of UUIDs, filtered to only those that are block display entities of the rig.
     */
    public static Set<String> getBlockDisplayNamesFromUUIDs(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> result = new HashSet<>();
        for (UUID uuid : uuids) {
            for (AJRigInstance.BlockDisplayInfo info : rig.getAllBlockDisplayInfos()) {
                if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                    result.add(info.blockDisplayName);
                }
            }
        }
        return result;
    }

    /**
     * Returns a set of text display names for the given set of UUIDs, filtered to only those that are text display entities of the rig.
     */
    public static Set<String> getTextDisplayNamesFromUUIDs(AJRigInstance rig, Set<UUID> uuids) {
        Set<String> result = new HashSet<>();
        for (UUID uuid : uuids) {
            for (AJRigInstance.TextDisplayInfo info : rig.getAllTextDisplayInfos()) {
                if (info.entityUUID != null && info.entityUUID.equals(uuid)) {
                    result.add(info.textDisplayName);
                }
            }
        }
        return result;
    }

    // --- Bone tree support (for bones only) ---

    /**
     * Returns the set of bone names in the tree for the given base bone name.
     * This uses AJBoneData tags for the export namespace to determine the tree.
     * The base bone is always included.
     */
    public static Set<String> getBoneTreeByName(AJRigInstance rig, String baseBoneName) {
        Set<String> result = new HashSet<>();
        String exportNamespace = rig.getExportNamespace();
        Set<String> allBoneNames = AJBoneData.getAllBoneNames(exportNamespace);
        String treeTag = "aj." + exportNamespace + ".bone." + baseBoneName + ".tree";
        for (String boneName : allBoneNames) {
            AJBoneData.BoneData boneData = AJBoneData.getBoneData(exportNamespace, boneName);
            if (boneData == null) continue;
            if (boneName.equals(baseBoneName) || boneData.tags.contains(treeTag)) {
                result.add(boneName);
            }
        }
        return result;
    }
}