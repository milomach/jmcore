/*
package com.jmcore.core.aj.posing;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;
import org.joml.Vector2f;
import org.joml.Quaternionf;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.data.AJFrameData;
import com.jmcore.core.aj.rig_instance.AJRigInstance;

import java.util.Collection;
*/

/**
 * LocatorPositioner runs every tick and updates the calculated position and rotation
 * for each locator in all rigs, storing the result in LocatorInfo.
 * No entity logic is included.
 */
/*
public class LocatorPositioner extends BukkitRunnable {
    private final AJRigManager rigManager;
    private final Plugin plugin;

    public LocatorPositioner(AJRigManager rigManager, Plugin plugin) {
        this.rigManager = rigManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        StringBuilder debug = new StringBuilder();
        debug.append("[LocatorPositioner] Tick start\n");
        Collection<AJRigInstance> allRigs = rigManager.getAllPlayerRigs();
        allRigs.addAll(rigManager.getAllGlobalRigs());
        debug.append("[LocatorPositioner] Total rigs: ").append(allRigs.size()).append("\n");
        for (AJRigInstance rig : allRigs) {
            updateLocatorsForRig(rig, debug);
        }
        debug.append("[LocatorPositioner] Tick end\n");
        plugin.getLogger().info(debug.toString());
    }

    private void updateLocatorsForRig(AJRigInstance rig, StringBuilder debug) {
        Location rootLoc = null;
        if (rig.getRootEntityUUID() != null) {
            var rootEntity = Bukkit.getEntity(rig.getRootEntityUUID());
            if (rootEntity != null) {
                rootLoc = rootEntity.getLocation();
            }
        }
        if (rootLoc == null) {
            debug.append("[LocatorPositioner] Root entity not found for rig ").append(rig.getInternalId()).append("\n");
            return;
        }
        Vector3f offsetTranslation = rig.getTranslationOffset();
        Quaternionf offsetRotation = rig.getRotationOffset();

        boolean changed = false;
        Location prevRootLoc = rig.getPreviousRootLocation();
        Vector3f prevOffsetTrans = rig.getPreviousOffsetTranslation();
        Quaternionf prevOffsetRot = rig.getPreviousOffsetRotation();

        if (prevRootLoc == null ||
            !locationsEqual(rootLoc, prevRootLoc) ||
            !vectorsEqual(offsetTranslation, prevOffsetTrans) ||
            !quaternionsEqual(offsetRotation, prevOffsetRot)) {
            changed = true;
        }

        debug.append("[LocatorPositioner] Rig: ").append(rig.getInternalId())
             .append(" | Locators: ").append(rig.getLocatorNames().size()).append("\n");
        debug.append("[LocatorPositioner] Root location: ").append(rootLoc).append("\n");
        debug.append("[LocatorPositioner] Offset translation: ").append(offsetTranslation).append("\n");
        debug.append("[LocatorPositioner] Offset rotation: ").append(offsetRotation).append("\n");
        debug.append("[LocatorPositioner] Changed: ").append(changed).append("\n");

        if (!changed) {
            debug.append("[LocatorPositioner] No change detected, skipping locator update.\n");
            return;
        }

        // --- Update locator positions/rotations using animation source ---
        AJRigInstance.AnimationSource src = rig.getActiveAnimationSource();
        if (src == null) {
            debug.append("[LocatorPositioner] No animation source present, skipping.\n");
            return;
        }
        String animationName = src.animationName;
        int frame = src.currentFrame;

        for (String locatorName : src.includedLocators) {
            AJRigInstance.LocatorInfo locatorInfo = rig.getLocatorInfo(locatorName);
            if (locatorInfo == null) {
                debug.append("[LocatorPositioner] LocatorInfo missing for locator ").append(locatorName).append("\n");
                continue;
            }
            AJFrameData.LocatorFrameData frameData = AJFrameData.getLocatorFrameData(
                rig.getExportNamespace(), animationName, frame, locatorName
            );
            if (frameData == null) {
                debug.append("[LocatorPositioner] No frame data for locator '").append(locatorName)
                     .append("' animation: ").append(animationName)
                     .append(" frame: ").append(frame).append("\n");
                continue;
            }

            // --- Calculate position and rotation ---
            Vector3f frameTranslation = new Vector3f(frameData.position);
            Vector2f frameRotation = new Vector2f(frameData.rotation);

            // Start with root location
            Vector3f base = new Vector3f((float) rootLoc.getX(), (float) rootLoc.getY(), (float) rootLoc.getZ());

            // Apply offset translation
            base.add(offsetTranslation);

            // Build root rotation quaternion from Bukkit yaw/pitch
            Quaternionf rootRotation = quaternionFromYawPitch(rootLoc.getYaw(), rootLoc.getPitch());

            // Combine root rotation and offset rotation
            Quaternionf combinedRotation = new Quaternionf(rootRotation).mul(offsetRotation);

            // Rotate frame translation by combined rotation
            Vector3f rotatedFrameTranslation = new Vector3f(frameTranslation);
            rotatedFrameTranslation.rotate(combinedRotation);

            // Add rotated frame translation to base
            base.add(rotatedFrameTranslation);

            // Calculate locator yaw/pitch (root + frame)
            float locatorYaw = rootLoc.getYaw() + frameRotation.x;
            float locatorPitch = rootLoc.getPitch() + frameRotation.y;

            // Store in LocatorInfo
            locatorInfo.currentPosition = new Vector3f(base);
            locatorInfo.currentYaw = locatorYaw;
            locatorInfo.currentPitch = locatorPitch;

            debug.append("[LocatorPositioner] Locator '").append(locatorName)
                 .append("' position: ").append(base)
                 .append(" yaw: ").append(locatorYaw)
                 .append(" pitch: ").append(locatorPitch).append("\n");
        }

        // Update previous values
        rig.setPreviousRootLocation(rootLoc);
        rig.setPreviousOffsetTranslation(offsetTranslation);
        rig.setPreviousOffsetRotation(offsetRotation);
    }

    // --- Utility methods for comparison ---
    private boolean locationsEqual(Location a, Location b) {
        if (a == null || b == null) return false;
        return floatEqual((float)a.getX(), (float)b.getX()) &&
               floatEqual((float)a.getY(), (float)b.getY()) &&
               floatEqual((float)a.getZ(), (float)b.getZ()) &&
               floatEqual(a.getYaw(), b.getYaw()) &&
               floatEqual(a.getPitch(), b.getPitch());
    }

    private boolean vectorsEqual(Vector3f a, Vector3f b) {
        if (a == null || b == null) return false;
        return floatEqual(a.x, b.x) && floatEqual(a.y, b.y) && floatEqual(a.z, b.z);
    }

    private boolean quaternionsEqual(Quaternionf a, Quaternionf b) {
        if (a == null || b == null) return false;
        return floatEqual(a.x, b.x) && floatEqual(a.y, b.y) &&
               floatEqual(a.z, b.z) && floatEqual(a.w, b.w);
    }

    private boolean floatEqual(float a, float b) {
        return Math.abs(a - b) < 0.0001f;
    }

    // --- Utility to build a quaternion from Bukkit yaw/pitch ---
    private Quaternionf quaternionFromYawPitch(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        Quaternionf q = new Quaternionf();
        q.rotateYXZ(yawRad, pitchRad, 0);
        return q;
    }
}
*/