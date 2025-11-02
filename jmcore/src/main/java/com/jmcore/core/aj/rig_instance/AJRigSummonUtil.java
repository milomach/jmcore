package com.jmcore.core.aj.rig_instance;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.aj.data.*;
import com.jmcore.core.aj.data.AJBoneData.BoneData;
import com.jmcore.core.aj.entity_state.AJRigApplyBlockDisplayStateUtil;
import com.jmcore.core.aj.entity_state.AJRigApplyBoneStateUtil;
import com.jmcore.core.aj.entity_state.AJRigApplyItemDisplayStateUtil;
import com.jmcore.core.aj.entity_state.AJRigApplyTextDisplayStateUtil;
import com.jmcore.core.util.display_utils.CullingUtil;
import com.jmcore.core.util.display_utils.block.BlockUtil;
import com.jmcore.core.util.display_utils.item.ModelUtil;
import com.jmcore.core.util.display_utils.text.TextUtil;

import java.util.Set;

public class AJRigSummonUtil {

    /**
     * Summons the root entity for the rig at the specified location/yaw/pitch.
     * Does nothing if already summoned.
     */
    public static void summonRootEntity(AJRigInstance rig, Location location, float yaw, float pitch, Plugin plugin) {
        if (rig.getRootEntityUUID() != null && Bukkit.getEntity(rig.getRootEntityUUID()) != null) return;

        Location rootLoc = location.clone();
        rootLoc.setYaw(yaw);
        rootLoc.setPitch(pitch);
        ItemDisplay root = (ItemDisplay) location.getWorld().spawn(rootLoc, ItemDisplay.class);
        rig.setRootEntityUUID(root.getUniqueId());

        // Give all tags from AJRootData
        AJRootData.RootData rootData = AJRootData.getRootData(rig.getExportNamespace());
        if (rootData != null) {
            for (String tag : rootData.getTags()) {
                root.addScoreboardTag(tag);
            }
        }
        root.teleport(rootLoc);
    }

    /**
     * Summons bone entities for the given set of bone names.
     * Follows the same robust logic as before.
     */
    public static void summonBoneEntities(AJRigInstance rig, Set<String> boneNames, Location location, float yaw, float pitch, Plugin plugin) {
        Set<String> validBoneNames = RigEntityUtil.filterBoneNames(rig, boneNames);
        if (validBoneNames.isEmpty()) return;

        if (rig.getRootEntityUUID() == null || Bukkit.getEntity(rig.getRootEntityUUID()) == null) {
            summonRootEntity(rig, location, yaw, pitch, plugin);
        }
        ItemDisplay root = (ItemDisplay) Bukkit.getEntity(rig.getRootEntityUUID());
        Location rootLoc = location.clone();
        rootLoc.setYaw(yaw);
        rootLoc.setPitch(pitch);

        for (String boneName : validBoneNames) {
            AJRigInstance.BoneInfo boneInfo = rig.getBoneInfo(boneName);
            if (boneInfo == null) continue;
            if (boneInfo.entityUUID != null && Bukkit.getEntity(boneInfo.entityUUID) != null) continue;

            BoneData boneData = AJBoneData.getBoneData(rig.getExportNamespace(), boneName);
            if (boneData == null) continue;

            ItemDisplay boneEntity = (ItemDisplay) location.getWorld().spawn(rootLoc, ItemDisplay.class);
            rig.addBoneEntityUUID(boneEntity.getUniqueId());
            boneInfo.entityUUID = boneEntity.getUniqueId();

            // Set item
            Material mat = Material.matchMaterial(boneData.item);
            if (mat == null) mat = Material.STICK;
            boneEntity.setItemStack(new ItemStack(mat));

            // Set item model
            if (boneData.itemModelPath != null && !boneData.itemModelPath.isEmpty()) {
                ModelUtil.setItemModel(boneEntity, boneData.itemModelPath);
            }

            // Set bounding box
            CullingUtil.setDisplayWidth(boneEntity, boneData.boundingBoxWidth);
            CullingUtil.setDisplayHeight(boneEntity, boneData.boundingBoxHeight);

            // Add tags
            for (String tag : boneData.tags) {
                boneEntity.addScoreboardTag(tag);
            }

            // Mark all bone entity states as not applied and apply bone states
            AJRigApplyBoneStateUtil.markAllStatesNotApplied(boneInfo);
            AJRigApplyBoneStateUtil.applyBoneStates(rig, Set.of(boneEntity.getUniqueId()), plugin);

            // Make bone entity ride the root
            root.addPassenger(boneEntity);
        }
    }

    public static void summonBoneEntities(AJRigInstance rig, String boneName, Location location, float yaw, float pitch, Plugin plugin) {
        summonBoneEntities(rig, Set.of(boneName), location, yaw, pitch, plugin);
    }

    /**
     * Summons item display entities for the given set of item display names.
     * Follows the same robust logic as bones, with item display differences.
     */
    public static void summonItemDisplayEntities(AJRigInstance rig, Set<String> itemDisplayNames, Location location, float yaw, float pitch, Plugin plugin) {
        Set<String> validNames = RigEntityUtil.filterItemDisplayNames(rig, itemDisplayNames);
        if (validNames.isEmpty()) return;

        if (rig.getRootEntityUUID() == null || Bukkit.getEntity(rig.getRootEntityUUID()) == null) {
            summonRootEntity(rig, location, yaw, pitch, plugin);
        }
        ItemDisplay root = (ItemDisplay) Bukkit.getEntity(rig.getRootEntityUUID());
        Location rootLoc = location.clone();
        rootLoc.setYaw(yaw);
        rootLoc.setPitch(pitch);

        for (String name : validNames) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info == null) continue;
            if (info.entityUUID != null && Bukkit.getEntity(info.entityUUID) != null) continue;

            AJItemDisplayData.ItemDisplayData data = AJItemDisplayData.getItemDisplayData(rig.getExportNamespace(), name);
            if (data == null) continue;

            ItemDisplay entity = (ItemDisplay) location.getWorld().spawn(rootLoc, ItemDisplay.class);
            rig.addItemDisplayEntityUUID(entity.getUniqueId());
            info.entityUUID = entity.getUniqueId();

            // Set item
            Material mat = Material.matchMaterial(data.item);
            if (mat == null) mat = Material.STICK;
            entity.setItemStack(new ItemStack(mat));

            // Set bounding box
            CullingUtil.setDisplayWidth(entity, data.boundingBoxWidth);
            CullingUtil.setDisplayHeight(entity, data.boundingBoxHeight);

            // Add tags
            for (String tag : data.tags) {
                entity.addScoreboardTag(tag);
            }

            // Mark all states as not applied and apply states
            AJRigApplyItemDisplayStateUtil.markAllStatesNotApplied(info);
            AJRigApplyItemDisplayStateUtil.applyItemDisplayStates(rig, Set.of(entity.getUniqueId()), plugin);

            root.addPassenger(entity);
        }
    }

    public static void summonItemDisplayEntities(AJRigInstance rig, String name, Location location, float yaw, float pitch, Plugin plugin) {
        summonItemDisplayEntities(rig, Set.of(name), location, yaw, pitch, plugin);
    }

    /**
     * Summons block display entities for the given set of block display names.
     * Uses BlockDisplay entity and sets block using BlockUtil.
     */
    public static void summonBlockDisplayEntities(AJRigInstance rig, Set<String> blockDisplayNames, Location location, float yaw, float pitch, Plugin plugin) {
        Set<String> validNames = RigEntityUtil.filterBlockDisplayNames(rig, blockDisplayNames);
        if (validNames.isEmpty()) return;

        if (rig.getRootEntityUUID() == null || Bukkit.getEntity(rig.getRootEntityUUID()) == null) {
            summonRootEntity(rig, location, yaw, pitch, plugin);
        }
        ItemDisplay root = (ItemDisplay) Bukkit.getEntity(rig.getRootEntityUUID());
        Location rootLoc = location.clone();
        rootLoc.setYaw(yaw);
        rootLoc.setPitch(pitch);

        for (String name : validNames) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info == null) continue;
            if (info.entityUUID != null && Bukkit.getEntity(info.entityUUID) != null) continue;

            AJBlockDisplayData.BlockDisplayData data = AJBlockDisplayData.getBlockDisplayData(rig.getExportNamespace(), name);
            if (data == null) continue;

            BlockDisplay entity = (BlockDisplay) location.getWorld().spawn(rootLoc, BlockDisplay.class);
            rig.addBlockDisplayEntityUUID(entity.getUniqueId());
            info.entityUUID = entity.getUniqueId();

            // Set block using BlockUtil
            BlockUtil.setBlock(entity, data.block);

            // Set bounding box
            CullingUtil.setDisplayWidth(entity, data.boundingBoxWidth);
            CullingUtil.setDisplayHeight(entity, data.boundingBoxHeight);

            // Add tags
            for (String tag : data.tags) {
                entity.addScoreboardTag(tag);
            }

            // Mark all states as not applied and apply states
            AJRigApplyBlockDisplayStateUtil.markAllStatesNotApplied(info);
            AJRigApplyBlockDisplayStateUtil.applyBlockDisplayStates(rig, Set.of(entity.getUniqueId()), plugin);

            root.addPassenger(entity);
        }
    }

    public static void summonBlockDisplayEntities(AJRigInstance rig, String name, Location location, float yaw, float pitch, Plugin plugin) {
        summonBlockDisplayEntities(rig, Set.of(name), location, yaw, pitch, plugin);
    }

    /**
     * Summons text display entities for the given set of text display names.
     * Uses TextDisplay entity and sets text using TextUtil.
     */
    public static void summonTextDisplayEntities(AJRigInstance rig, Set<String> textDisplayNames, Location location, float yaw, float pitch, Plugin plugin) {
        Set<String> validNames = RigEntityUtil.filterTextDisplayNames(rig, textDisplayNames);
        if (validNames.isEmpty()) return;

        if (rig.getRootEntityUUID() == null || Bukkit.getEntity(rig.getRootEntityUUID()) == null) {
            summonRootEntity(rig, location, yaw, pitch, plugin);
        }
        ItemDisplay root = (ItemDisplay) Bukkit.getEntity(rig.getRootEntityUUID());
        Location rootLoc = location.clone();
        rootLoc.setYaw(yaw);
        rootLoc.setPitch(pitch);

        for (String name : validNames) {
            AJRigInstance.TextDisplayInfo info = rig.getTextDisplayInfo(name);
            if (info == null) continue;
            if (info.entityUUID != null && Bukkit.getEntity(info.entityUUID) != null) continue;

            AJTextDisplayData.TextDisplayData data = AJTextDisplayData.getTextDisplayData(rig.getExportNamespace(), name);
            if (data == null) continue;

            TextDisplay entity = (TextDisplay) location.getWorld().spawn(rootLoc, TextDisplay.class);
            rig.addTextDisplayEntityUUID(entity.getUniqueId());
            info.entityUUID = entity.getUniqueId();

            // Set text using TextUtil
            TextUtil.setText(entity, data.text);

            // Set bounding box
            CullingUtil.setDisplayWidth(entity, data.boundingBoxWidth);
            CullingUtil.setDisplayHeight(entity, data.boundingBoxHeight);

            // Add tags
            for (String tag : data.tags) {
                entity.addScoreboardTag(tag);
            }

            // Mark all states as not applied and apply states
            AJRigApplyTextDisplayStateUtil.markAllStatesNotApplied(info);
            AJRigApplyTextDisplayStateUtil.applyTextDisplayStates(rig, Set.of(entity.getUniqueId()), plugin);

            root.addPassenger(entity);
        }
    }

    public static void summonTextDisplayEntities(AJRigInstance rig, String name, Location location, float yaw, float pitch, Plugin plugin) {
        summonTextDisplayEntities(rig, Set.of(name), location, yaw, pitch, plugin);
    }
}