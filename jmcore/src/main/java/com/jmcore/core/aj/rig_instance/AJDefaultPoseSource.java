package com.jmcore.core.aj.rig_instance;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * AJDefaultPoseSource
 *
 * Dedicated, managed default-pose source.
 *
 * Responsibilities:
 * - Maintain the set of entities / locators that should receive the default pose for this rig.
 * - Be non-modifiable by external callers (mutators are intentionally no-ops).
 * - Provide read-only accessors for included sets so the pose pipeline can use them.
 *
 * Inclusion policy:
 * - The default pose includes any entity/locator that is NOT included in any other enabled animation source.
 * - The rig manages when updateInclusions() is called (AJRigInstance.notifyAnimationSourceChanged()).
 *
 * Important:
 * - This class does NOT store transform data. The transforms are read from
 *   [`AJDefaultPoseData`](src/main/java/com/jmcore/core/aj/data/AJDefaultPoseData.java) at tick time.
 */
public final class AJDefaultPoseSource {
    private final AJRigInstance rig;

    // Exposed id for debugging/reference
    private final String sourceId = "default_pose";

    // Always considered enabled for application purposes; external code cannot toggle
    private final boolean enabled = true;

    // Included sets are computed by updateInclusions() and exposed read-only
    private Set<String> includedBones = new HashSet<>();
    private Set<String> includedItemDisplays = new HashSet<>();
    private Set<String> includedBlockDisplays = new HashSet<>();
    private Set<String> includedTextDisplays = new HashSet<>();
    private Set<String> includedLocators = new HashSet<>();

    public AJDefaultPoseSource(AJRigInstance rig) {
        this.rig = rig;
    }

    // --- Read-only accessors (return immutable views) ---
    public String getSourceId() { return sourceId; }
    public boolean isEnabled() { return enabled; }

    public Set<String> getIncludedBones() { return Collections.unmodifiableSet(includedBones); }
    public Set<String> getIncludedItemDisplays() { return Collections.unmodifiableSet(includedItemDisplays); }
    public Set<String> getIncludedBlockDisplays() { return Collections.unmodifiableSet(includedBlockDisplays); }
    public Set<String> getIncludedTextDisplays() { return Collections.unmodifiableSet(includedTextDisplays); }
    public Set<String> getIncludedLocators() { return Collections.unmodifiableSet(includedLocators); }

    // --- Mutators are intentionally no-ops to prevent external modification ---
    public void setIncludedBones(Set<String> bones) {}
    public void setIncludedItemDisplays(Set<String> items) {}
    public void setIncludedBlockDisplays(Set<String> blocks) {}
    public void setIncludedTextDisplays(Set<String> texts) {}
    public void setIncludedLocators(Set<String> locs) {}

    /**
     * Recompute included sets for the default pose.
     *
     * The logic:
     *  - Gather all names for each entity type available in the rig.
     *  - Gather all names used by enabled animation sources (rig.getAllAnimationSources()).
     *  - Default included = allNames \ usedNames
     *
     * This method is fast and safe to call whenever animation sources change.
     */
    void updateInclusions() {
        // --- All available names from the rig (export namespace specific) ---
        Set<String> allBones = new HashSet<>(rig.getBoneNames());
        Set<String> allItems = new HashSet<>(rig.getItemDisplayNames());
        Set<String> allBlocks = new HashSet<>(rig.getBlockDisplayNames());
        Set<String> allTexts = new HashSet<>(rig.getTextDisplayNames());
        Set<String> allLocators = new HashSet<>(rig.getLocatorNames());

        // --- Names used by other enabled animation sources ---
        Set<String> usedBones = new HashSet<>();
        Set<String> usedItems = new HashSet<>();
        Set<String> usedBlocks = new HashSet<>();
        Set<String> usedTexts = new HashSet<>();
        Set<String> usedLocators = new HashSet<>();

        for (AJAnimationSource src : rig.getAllAnimationSources()) {
            if (src == null || !src.isEnabled()) continue;
            usedBones.addAll(src.getIncludedBones());
            usedItems.addAll(src.getIncludedItemDisplays());
            usedBlocks.addAll(src.getIncludedBlockDisplays());
            usedTexts.addAll(src.getIncludedTextDisplays());
            usedLocators.addAll(src.getIncludedLocators());
        }

        // Set default included sets = all - used
        includedBones = difference(allBones, usedBones);
        includedItemDisplays = difference(allItems, usedItems);
        includedBlockDisplays = difference(allBlocks, usedBlocks);
        includedTextDisplays = difference(allTexts, usedTexts);
        includedLocators = difference(allLocators, usedLocators);

        // TEMP DEBUG: print counts and small samples to help trace logic
        try {
            System.out.println("[AJDefaultPoseSource] updateInclusions -> rig=" + rig.getInternalId() + " ns=" + rig.getExportNamespace());
            System.out.println("  allBones=" + allBones.size() + " usedBones=" + usedBones.size() + " defaultIncludedBones=" + includedBones.size() +
                " sampleUsed=" + (usedBones.isEmpty() ? "<none>" : usedBones.iterator().next()) +
                " sampleIncluded=" + (includedBones.isEmpty() ? "<none>" : includedBones.iterator().next()));
            System.out.println("  allItems=" + allItems.size() + " usedItems=" + usedItems.size() + " defaultIncludedItems=" + includedItemDisplays.size());
            System.out.println("  allBlocks=" + allBlocks.size() + " usedBlocks=" + usedBlocks.size() + " defaultIncludedBlocks=" + includedBlockDisplays.size());
            System.out.println("  allTexts=" + allTexts.size() + " usedTexts=" + usedTexts.size() + " defaultIncludedTexts=" + includedTextDisplays.size());
            System.out.println("  allLocators=" + allLocators.size() + " usedLocators=" + usedLocators.size() + " defaultIncludedLocators=" + includedLocators.size());
        } catch (Throwable t) {
            // keep safe
        }
    }

    // Simple set difference helper
    private static Set<String> difference(Set<String> all, Set<String> used) {
        Set<String> out = new HashSet<>(all);
        out.removeAll(used);
        return out;
    }
}