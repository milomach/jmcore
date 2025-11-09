package com.jmcore.core.aj.posing;

import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jmcore.core.aj.data.AJDefaultPoseData;
import com.jmcore.core.aj.data.AJFrameData;
import com.jmcore.core.aj.rig_instance.*;

import org.joml.Vector2f;

import java.util.*;

/**
 * PoseStateManager is the central system for calculating and applying pose transformations for all entities and locators in all AJ rigs, every tick.
 * It builds pose state from scratch each tick.
 * This is based on the current state of all animation sources (or default pose source if none are present) along with offset sources.
 * No pose data is accumulated across ticks; everything is recalculated.
 */
public class PoseStateManager {
    // Stores per-rig pose state for the current and previous tick.
    // Key: AJRigInstance, Value: RigPoseState (contains all pose data for that rig)
    private static final Map<AJRigInstance, RigPoseState> rigPoseStates = new HashMap<>();

    // --- Step 1: Prepare for tick ---
    /**
     * Called at the start of each tick for a rig.
     * Moves current pose state to previous, then clears current state so it can be rebuilt from sources.
     */
    public static void prepareForTick(AJRigInstance rig) {
        RigPoseState state = rigPoseStates.computeIfAbsent(rig, k -> new RigPoseState());
        state.moveToPreviousAndClear();
    }

    // --- Step 2: Process offset sources ---
    /**
     * Processes all enabled offset sources for a rig, in order.
     * For each included entity/locator, accumulates offset transforms (translation, rotation, scale).
     * This is additive: multiple offset sources can contribute to the same entity/locator.
     * 
     * For animated offset sources, applies the animation's bone transform to all included entities,
     * and the animation's locator transform to all included locators.
     */
    public static void processOffsetSources(AJRigInstance rig) {
        RigPoseState state = rigPoseStates.get(rig);
        List<AJOffsetSource> sources = new ArrayList<>(rig.getAllOffsetSources());
        sources.sort(Comparator.comparingInt(AJOffsetSource::getOrder));
        for (AJOffsetSource src : sources) {
            if (!src.isEnabled()) continue;

            if (src.getOffsetMode() == AJOffsetSource.OffsetMode.ANIMATED) {
                String ns = src.getExportNamespace();
                String anim = src.getAnimationName();
                int frame = src.getCurrentFrame();

                // --- Find the bone and locator for this animation (should be exactly one of each) ---
                Set<String> animBones = AJFrameData.getBonesForFrame(ns, anim, frame);
                Set<String> animLocators = AJFrameData.getLocatorsForFrame(ns, anim, frame);

                // Get the bone transform (if present)
                Vector3f boneTranslation = new Vector3f(0, 0, 0);
                Quaternionf boneRotation = new Quaternionf().identity();
                Vector3f boneScale = new Vector3f(1, 1, 1);
                if (!animBones.isEmpty()) {
                    String animBone = animBones.iterator().next();
                    AJFrameData.BoneFrameData boneFrame = AJFrameData.getBoneFrameData(ns, anim, frame, animBone);
                    if (boneFrame != null) {
                        boneTranslation.set(boneFrame.translation);
                        boneRotation.set(boneFrame.rotation);
                        boneScale.set(boneFrame.scale);
                    }
                }

                // Get the locator transform (if present)
                Vector3f locatorTranslation = new Vector3f(0, 0, 0);
                Quaternionf locatorRotation = new Quaternionf().identity();
                if (!animLocators.isEmpty()) {
                    String animLocator = animLocators.iterator().next();
                    AJFrameData.LocatorFrameData locatorFrame = AJFrameData.getLocatorFrameData(ns, anim, frame, animLocator);
                    if (locatorFrame != null) {
                        locatorTranslation.set(locatorFrame.position);
                        // Locator rotation is a Vector2f (ry, rx), but PoseStateManager expects Quaternionf
                        // You may need to convert Vector2f (yaw, pitch) to Quaternionf here
                        float ry = locatorFrame.rotation.x;
                        float rx = locatorFrame.rotation.y;
                        locatorRotation.identity()
                            .rotateY((float)Math.toRadians(ry))
                            .rotateX((float)Math.toRadians(rx));
                    }
                }

                // Apply the bone transform to all included bones, item displays, block displays, and text displays
                for (String bone : src.getIncludedBones()) {
                    state.includeEntity(bone, PoseEntityType.BONE);
                    state.accumulateOffset(bone, PoseEntityType.BONE, boneTranslation, boneRotation, boneScale);
                }
                for (String item : src.getIncludedItemDisplays()) {
                    state.includeEntity(item, PoseEntityType.ITEM_DISPLAY);
                    state.accumulateOffset(item, PoseEntityType.ITEM_DISPLAY, boneTranslation, boneRotation, boneScale);
                }
                for (String block : src.getIncludedBlockDisplays()) {
                    state.includeEntity(block, PoseEntityType.BLOCK_DISPLAY);
                    state.accumulateOffset(block, PoseEntityType.BLOCK_DISPLAY, boneTranslation, boneRotation, boneScale);
                }
                for (String text : src.getIncludedTextDisplays()) {
                    state.includeEntity(text, PoseEntityType.TEXT_DISPLAY);
                    state.accumulateOffset(text, PoseEntityType.TEXT_DISPLAY, boneTranslation, boneRotation, boneScale);
                }
                // Apply the locator transform to all included locators
                for (String locator : src.getIncludedLocators()) {
                    state.includeLocator(locator);
                    state.accumulateLocatorOffset(locator, locatorTranslation, locatorRotation);
                }

                // Advance the frame for this animated offset source
                int frameCount = AJFrameData.getFrameCount(ns, anim);
                src.advanceFrame(frameCount);

            } else {
                // --- Static offset mode: accumulate static transform as before ---
                for (String bone : src.getIncludedBones()) {
                    state.includeEntity(bone, PoseEntityType.BONE);
                    state.accumulateOffset(bone, PoseEntityType.BONE, src.getTranslation(), src.getRotation(), src.getScale());
                }
                for (String item : src.getIncludedItemDisplays()) {
                    state.includeEntity(item, PoseEntityType.ITEM_DISPLAY);
                    state.accumulateOffset(item, PoseEntityType.ITEM_DISPLAY, src.getTranslation(), src.getRotation(), src.getScale());
                }
                for (String block : src.getIncludedBlockDisplays()) {
                    state.includeEntity(block, PoseEntityType.BLOCK_DISPLAY);
                    state.accumulateOffset(block, PoseEntityType.BLOCK_DISPLAY, src.getTranslation(), src.getRotation(), src.getScale());
                }
                for (String text : src.getIncludedTextDisplays()) {
                    state.includeEntity(text, PoseEntityType.TEXT_DISPLAY);
                    state.accumulateOffset(text, PoseEntityType.TEXT_DISPLAY, src.getTranslation(), src.getRotation(), src.getScale());
                }
                for (String locator : src.getIncludedLocators()) {
                    state.includeLocator(locator);
                    state.accumulateLocatorOffset(locator, src.getTranslation(), src.getRotation());
                }
            }
        }
    }

    // --- Step 3: Process animation sources (or default pose source if none present) ---
    /**
     * Processes all enabled animation sources for a rig, in order.
     * Handles frame advancement and accumulation of animation transforms.
     * For each included entity/locator, accumulates animation transforms (translation, rotation, scale) from the current frame.
     */
    public static void processAnimationSources(AJRigInstance rig) {
        RigPoseState state = rigPoseStates.get(rig);

        // TEMP DEBUG: print summary of sources for this rig
        try {
            long enabledAnimCount = rig.getAllAnimationSources().stream().filter(AJAnimationSource::isEnabled).count();
            long offsetCount = rig.getAllOffsetSources().size();
            System.out.println("[PoseStateManager] processAnimationSources -> rig=" + rig.getInternalId() + " ns=" + rig.getExportNamespace() +
                " enabledAnimSources=" + enabledAnimCount + " offsetSources=" + offsetCount);
            for (AJAnimationSource s : rig.getAllAnimationSources()) {
                if (s == null) continue;
                System.out.println("  animSource: id=" + s.getSourceId() + " enabled=" + s.isEnabled() + " anim=" + s.getAnimationName() +
                    " includedBones=" + s.getIncludedBones().size() + " includedItems=" + s.getIncludedItemDisplays().size());
            }
        } catch (Throwable t) {
            // ignore
        }

        // 1) User animation sources
        List<AJAnimationSource> sources = new ArrayList<>(rig.getAllAnimationSources());
        sources.sort(Comparator.comparingInt(AJAnimationSource::getOrder));
        for (AJAnimationSource src : sources) {
            if (!src.isEnabled()) continue;

            // Ensure entities/locators are present in state
            for (String bone : src.getIncludedBones()) state.includeEntity(bone, PoseEntityType.BONE);
            for (String item : src.getIncludedItemDisplays()) state.includeEntity(item, PoseEntityType.ITEM_DISPLAY);
            for (String block : src.getIncludedBlockDisplays()) state.includeEntity(block, PoseEntityType.BLOCK_DISPLAY);
            for (String text : src.getIncludedTextDisplays()) state.includeEntity(text, PoseEntityType.TEXT_DISPLAY);
            for (String locator : src.getIncludedLocators()) state.includeLocator(locator);

            // Accumulate frame data for each included entity/locator
            int accumulations = 0;
            for (String bone : src.getIncludedBones()) {
                AJFrameData.BoneFrameData frame = AJFrameData.getBoneFrameData(rig.getExportNamespace(), src.getAnimationName(), src.getCurrentFrame(), bone);
                if (frame != null) { state.accumulateAnimation(bone, PoseEntityType.BONE, frame.translation, frame.rotation, frame.scale); accumulations++; }
            }
            for (String item : src.getIncludedItemDisplays()) {
                AJFrameData.ItemDisplayFrameData frame = AJFrameData.getItemDisplayFrameData(rig.getExportNamespace(), src.getAnimationName(), src.getCurrentFrame(), item);
                if (frame != null) { state.accumulateAnimation(item, PoseEntityType.ITEM_DISPLAY, frame.translation, frame.rotation, frame.scale); accumulations++; }
            }
            for (String block : src.getIncludedBlockDisplays()) {
                AJFrameData.BlockDisplayFrameData frame = AJFrameData.getBlockDisplayFrameData(rig.getExportNamespace(), src.getAnimationName(), src.getCurrentFrame(), block);
                if (frame != null) { state.accumulateAnimation(block, PoseEntityType.BLOCK_DISPLAY, frame.translation, frame.rotation, frame.scale); accumulations++; }
            }
            for (String text : src.getIncludedTextDisplays()) {
                AJFrameData.TextDisplayFrameData frame = AJFrameData.getTextDisplayFrameData(rig.getExportNamespace(), src.getAnimationName(), src.getCurrentFrame(), text);
                if (frame != null) { state.accumulateAnimation(text, PoseEntityType.TEXT_DISPLAY, frame.translation, frame.rotation, frame.scale); accumulations++; }
            }
            for (String locator : src.getIncludedLocators()) {
                AJFrameData.LocatorFrameData frame = AJFrameData.getLocatorFrameData(rig.getExportNamespace(), src.getAnimationName(), src.getCurrentFrame(), locator);
                if (frame != null) { state.accumulateLocatorAnimation(locator, frame.position, frame.rotation); accumulations++; }
            }

            // TEMP DEBUG: show how many accumulations (frame contributions) this source produced this tick
            try {
                System.out.println("[PoseStateManager] animation source applied -> rig=" + rig.getInternalId() + " src=" + src.getSourceId() +
                    " anim=" + src.getAnimationName() + " frame=" + src.getCurrentFrame() + " contributions=" + accumulations);
            } catch (Throwable t) {}

            // Advance frames if playing (unchanged logic)
            int frameCount = AJFrameData.getFrameCount(rig.getExportNamespace(), src.getAnimationName());
            src.advanceFrame(frameCount);
        }

        // TEMP DEBUG: before default pose processing, how many entity poses exist (from user animations/offsets)
        try {
            RigPoseState st = state;
            System.out.println("[PoseStateManager] rigState before default pose -> rig=" + rig.getInternalId() +
                " entityPoseCount=" + (st == null ? 0 : st.currentEntityPoses.size()) +
                " locatorPoseCount=" + (st == null ? 0 : st.currentLocatorPoses.size()));
        } catch (Throwable t) {}

        // 2) Dedicated default-pose source (single, rig-managed)
        AJDefaultPoseSource defaultSrc = rig.getDefaultPoseSource();
        if (defaultSrc != null && defaultSrc.isEnabled()) {
            String ns = rig.getExportNamespace();

            // Include default entities/locators in state (they may be disjoint from user animation sources)
            for (String bone : defaultSrc.getIncludedBones()) state.includeEntity(bone, PoseEntityType.BONE);
            for (String item : defaultSrc.getIncludedItemDisplays()) state.includeEntity(item, PoseEntityType.ITEM_DISPLAY);
            for (String block : defaultSrc.getIncludedBlockDisplays()) state.includeEntity(block, PoseEntityType.BLOCK_DISPLAY);
            for (String text : defaultSrc.getIncludedTextDisplays()) state.includeEntity(text, PoseEntityType.TEXT_DISPLAY);
            for (String locator : defaultSrc.getIncludedLocators()) state.includeLocator(locator);

            // Accumulate default-pose transforms into the animation accumulators.
            int defaultContribs = 0;
            for (String bone : defaultSrc.getIncludedBones()) {
                AJDefaultPoseData.DefaultBonePose p = AJDefaultPoseData.getBonePose(ns, bone);
                if (p != null) { state.accumulateAnimation(bone, PoseEntityType.BONE, p.translation, p.rotation, p.scale); defaultContribs++; }
            }
            for (String item : defaultSrc.getIncludedItemDisplays()) {
                AJDefaultPoseData.DefaultDisplayPose p = AJDefaultPoseData.getItemDisplayPose(ns, item);
                if (p != null) { state.accumulateAnimation(item, PoseEntityType.ITEM_DISPLAY, p.translation, p.rotation, p.scale); defaultContribs++; }
            }
            for (String block : defaultSrc.getIncludedBlockDisplays()) {
                AJDefaultPoseData.DefaultDisplayPose p = AJDefaultPoseData.getBlockDisplayPose(ns, block);
                if (p != null) { state.accumulateAnimation(block, PoseEntityType.BLOCK_DISPLAY, p.translation, p.rotation, p.scale); defaultContribs++; }
            }
            for (String text : defaultSrc.getIncludedTextDisplays()) {
                AJDefaultPoseData.DefaultDisplayPose p = AJDefaultPoseData.getTextDisplayPose(ns, text);
                if (p != null) { state.accumulateAnimation(text, PoseEntityType.TEXT_DISPLAY, p.translation, p.rotation, p.scale); defaultContribs++; }
            }
            for (String locator : defaultSrc.getIncludedLocators()) {
                AJDefaultPoseData.DefaultLocatorPose p = AJDefaultPoseData.getLocatorPose(ns, locator);
                if (p != null) { state.accumulateLocatorAnimation(locator, p.position, p.rotation); defaultContribs++; }
            }

            // TEMP DEBUG: show how many default pose contributions were applied this tick
            try {
                System.out.println("[PoseStateManager] default pose applied -> rig=" + rig.getInternalId() + " defaultContributions=" + defaultContribs +
                    " defaultIncludedBones=" + defaultSrc.getIncludedBones().size());
            } catch (Throwable t) {}
        }
    }

    // --- Step 4: Batch apply transforms ---
    /**
     * Applies the final calculated transformation matrix to each summoned entity in the rig.
     * Only applies if the matrix has changed since the previous tick.
     * Also stores locator pose data for use by other systems.
     */
    public static void batchApplyTransforms(AJRigInstance rig, Plugin plugin) {
        RigPoseState state = rigPoseStates.get(rig);

        // For each included entity, apply the calculated transformation if the entity is summoned and the matrix changed
        for (Map.Entry<PoseEntityKey, PoseEntityPose> entry : state.currentEntityPoses.entrySet()) {
            PoseEntityKey key = entry.getKey();
            PoseEntityPose pose = entry.getValue();
            PoseEntityPose prev = state.previousEntityPoses.get(key);

            // Get the entity UUID for this key (bone/item/block/text display)
            UUID uuid = getEntityUUID(rig, key);
            if (uuid == null) continue;
            Entity e = Bukkit.getEntity(uuid);
            if (e == null || e.isDead()) continue; // Only process summoned entities
            
            // Build the final transformation matrix from offset and animation transforms
            Matrix4f mat = buildFinalMatrix(pose.offsetTranslation, pose.offsetRotation, pose.offsetScale,
                                            pose.animTranslation, pose.animRotation, pose.animScale);

            // Only apply if changed since last tick
            boolean changed = (prev == null) || !mat.equals(prev.finalMatrix);
            if (changed && e instanceof Display display) {
                display.setTransformationMatrix(mat);
            }
            pose.finalMatrix = mat;
        }

        // For each included locator, store the calculated pose (for use by other systems)
        for (Map.Entry<String, LocatorPose> entry : state.currentLocatorPoses.entrySet()) {
            String locator = entry.getKey();
            LocatorPose pose = entry.getValue();
            // Store or update pose as needed (for now, just store)
            // TODO: Implement robust locator pose logic
        }
    }

    // --- Matrix composition logic ---
    /**
     * Composes the final transformation matrix for an entity from offset and animation transforms.
     */
    private static Matrix4f buildFinalMatrix(Vector3f offsetTranslation, Quaternionf offsetRotation, Vector3f scaleMultiplier,
                                             Vector3f frameTranslation, Quaternionf frameRotation, Vector3f frameScale) {
        // Step 1: Final rotation = offsetRotation * frameRotation
        Quaternionf finalRotation = new Quaternionf(offsetRotation).mul(frameRotation);

        // Step 2: Rotate offsetTranslation by inverse of final rotation
        Vector3f finalOffsetTranslation = new Vector3f(offsetTranslation);
        finalOffsetTranslation.rotate(new Quaternionf(finalRotation).invert());

        // Step 3: Apply scale multiplier to frame translation
        Vector3f finalFrameTranslation = new Vector3f(frameTranslation);
        finalFrameTranslation.mul(scaleMultiplier);

        // Step 4: Add final offset and frame translations
        Vector3f finalTranslation = new Vector3f(finalOffsetTranslation).add(finalFrameTranslation);

        // Step 5: Combine frame scale and scale multiplier for final scale
        Vector3f finalScale = new Vector3f(frameScale).mul(scaleMultiplier);

        // Step 6: Build the matrix (rotation, then translation, then scale)
        return new Matrix4f().rotate(finalRotation).translate(finalTranslation).scale(finalScale);
    }

    /**
     * Gets the entity UUID for a given pose entity key (bone/item/block/text display).
     * Returns null if the info object is missing or the entity is not summoned.
     */
    private static UUID getEntityUUID(AJRigInstance rig, PoseEntityKey key) {
        switch (key.type) {
            case BONE: {
                var info = rig.getBoneInfo(key.name);
                return info != null ? info.entityUUID : null;
            }
            case ITEM_DISPLAY: {
                var info = rig.getItemDisplayInfo(key.name);
                return info != null ? info.entityUUID : null;
            }
            case BLOCK_DISPLAY: {
                var info = rig.getBlockDisplayInfo(key.name);
                return info != null ? info.entityUUID : null;
            }
            case TEXT_DISPLAY: {
                var info = rig.getTextDisplayInfo(key.name);
                return info != null ? info.entityUUID : null;
            }
            default:
                return null;
        }
    }

    // --- Pose state classes ---

    /**
     * Enum for entity types that can be posed.
     */
    public enum PoseEntityType { BONE, ITEM_DISPLAY, BLOCK_DISPLAY, TEXT_DISPLAY }

    /**
     * Key for identifying a pose entity (by name and type).
     * Used for maps in RigPoseState.
     */
    public static class PoseEntityKey {
        public final String name;
        public final PoseEntityType type;
        public PoseEntityKey(String name, PoseEntityType type) { this.name = name; this.type = type; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PoseEntityKey)) return false;
            PoseEntityKey that = (PoseEntityKey) o;
            return Objects.equals(name, that.name) && type == that.type;
        }
        @Override public int hashCode() { return Objects.hash(name, type); }
    }

    /**
     * Stores all pose transform components for a single entity for the current tick.
     * Includes offset and animation transforms, and the final matrix.
     */
    public static class PoseEntityPose {
        public Vector3f offsetTranslation = new Vector3f(0,0,0); // Sum of all offset translations
        public Quaternionf offsetRotation = new Quaternionf().identity(); // Product of all offset rotations
        public Vector3f offsetScale = new Vector3f(1,1,1); // Product of all offset scales
        public Vector3f animTranslation = new Vector3f(0,0,0); // Sum of all animation translations
        public Quaternionf animRotation = new Quaternionf().identity(); // Product of all animation rotations
        public Vector3f animScale = new Vector3f(1,1,1); // Product of all animation scales
        public Matrix4f finalMatrix = null; // Final matrix applied to entity
    }

    /**
     * Stores all pose transform components for a single locator for the current tick.
     * Includes offset and animation transforms.
     * Locator pose logic is currently a placeholder.
     */
    public static class LocatorPose {
        public Vector3f offsetTranslation = new Vector3f(0,0,0);
        public Quaternionf offsetRotation = new Quaternionf().identity();
        public Vector3f animTranslation = new Vector3f(0,0,0);
        public Vector3f animRotation = new Vector3f(0,0,0); // Placeholder for locator rotation
        // Will extend as needed for robust locator logic
    }

    /**
     * Stores all pose state for a single rig for the current and previous tick.
     * Used to detect changes and batch apply transforms.
     */
    public static class RigPoseState {
        // Maps of pose entity key to pose data for current and previous tick
        public final Map<PoseEntityKey, PoseEntityPose> currentEntityPoses = new HashMap<>();
        public final Map<PoseEntityKey, PoseEntityPose> previousEntityPoses = new HashMap<>();
        // Maps of locator name to pose data for current and previous tick
        public final Map<String, LocatorPose> currentLocatorPoses = new HashMap<>();
        public final Map<String, LocatorPose> previousLocatorPoses = new HashMap<>();

        /**
         * Moves current pose state to previous, then clears current state.
         * Called at the start of each tick.
         */
        public void moveToPreviousAndClear() {
            previousEntityPoses.clear();
            previousEntityPoses.putAll(currentEntityPoses);
            currentEntityPoses.clear();
            previousLocatorPoses.clear();
            previousLocatorPoses.putAll(currentLocatorPoses);
            currentLocatorPoses.clear();
        }

        /**
         * Ensures an entity is present in the current pose state.
         * If not present, creates a new PoseEntityPose for it.
         */
        public void includeEntity(String name, PoseEntityType type) {
            currentEntityPoses.computeIfAbsent(new PoseEntityKey(name, type), k -> new PoseEntityPose());
        }

        /**
         * Ensures a locator is present in the current pose state.
         * If not present, creates a new LocatorPose for it.
         */
        public void includeLocator(String name) {
            currentLocatorPoses.computeIfAbsent(name, k -> new LocatorPose());
        }

        /**
         * Accumulates offset transforms for an entity.
         * Adds translation, multiplies rotation and scale.
         * Called for each offset source that includes the entity.
         */
        public void accumulateOffset(String name, PoseEntityType type, Vector3f translation, Quaternionf rotation, Vector3f scale) {
            PoseEntityPose pose = currentEntityPoses.get(new PoseEntityKey(name, type));
            if (pose != null) {
                pose.offsetTranslation.add(translation);
                pose.offsetRotation.mul(rotation);
                pose.offsetScale.mul(scale);
            }
        }

        /**
         * Accumulates animation transforms for an entity.
         * Adds translation, multiplies rotation and scale.
         * Called for each animation source that includes the entity.
         */
        public void accumulateAnimation(String name, PoseEntityType type, Vector3f translation, Quaternionf rotation, Vector3f scale) {
            PoseEntityPose pose = currentEntityPoses.get(new PoseEntityKey(name, type));
            if (pose != null) {
                pose.animTranslation.add(translation);
                pose.animRotation.mul(rotation);
                pose.animScale.mul(scale);
            }
        }

        /**
         * Accumulates offset transforms for a locator.
         * Adds translation, multiplies rotation.
         * Called for each offset source that includes the locator.
         */
        public void accumulateLocatorOffset(String name, Vector3f translation, Quaternionf rotation) {
            LocatorPose pose = currentLocatorPoses.get(name);
            if (pose != null) {
                pose.offsetTranslation.add(translation);
                pose.offsetRotation.mul(rotation);
            }
        }

        /**
         * Accumulates animation transforms for a locator.
         * Adds translation. Rotation is a placeholder.
         * Called for each animation source that includes the locator.
         */
        public void accumulateLocatorAnimation(String name, Vector3f translation, Vector2f rotation) {
            LocatorPose pose = currentLocatorPoses.get(name);
            if (pose != null) {
                pose.animTranslation.add(translation);
                // TODO: Handle locator rotation (currently not implemented)
            }
        }
    }
}