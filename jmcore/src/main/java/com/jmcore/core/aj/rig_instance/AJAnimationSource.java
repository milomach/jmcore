package com.jmcore.core.aj.rig_instance;

import java.util.HashSet;
import java.util.Set;

import com.jmcore.core.aj.data.AJFrameData;

/**
 * Represents an animation source for an AJ rig.
 * By default, animation sources are disabled and have no animation name set.
 */
public class AJAnimationSource {
    private final AJRigInstance rig;
    private final String sourceId;
    private int order;
    private boolean enabled = false;

    private String animationName = null;
    private EndBehavior endBehavior = EndBehavior.HOLD;
    private boolean playing = false;
    private int currentFrame = 0;

    private Set<String> includedBones = new HashSet<>();
    private Set<String> includedItemDisplays = new HashSet<>();
    private Set<String> includedBlockDisplays = new HashSet<>();
    private Set<String> includedTextDisplays = new HashSet<>();
    private Set<String> includedLocators = new HashSet<>();

    public enum EndBehavior {
        RESET, // Rewind to first frame and stop
        HOLD,  // Stop at last frame
        LOOP   // Repeat from first frame
    }

    public AJAnimationSource(AJRigInstance rig, String sourceId, int order) {
        this.rig = rig;
        this.sourceId = sourceId;
        this.order = order;
        updateIncludedEntities();
    }

    // --- Getters and setters ---
    public AJRigInstance getRig() { return rig; }
    public String getSourceId() { return sourceId; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        rig.notifyAnimationSourceChanged();
    }

    public String getAnimationName() { return animationName; }
    public void setAnimationName(String animationName) {
        this.animationName = animationName;
        updateIncludedEntities();
    }

    public EndBehavior getEndBehavior() { return endBehavior; }
    public void setEndBehavior(EndBehavior endBehavior) { this.endBehavior = endBehavior; }

    public boolean isPlaying() { return playing; }
    public void setPlaying(boolean playing) { this.playing = playing; }

    public int getCurrentFrame() { return currentFrame; }
    public void setCurrentFrame(int currentFrame) { this.currentFrame = currentFrame; }

    public Set<String> getIncludedBones() { return includedBones; }
    public void setIncludedBones(Set<String> includedBones) {
        this.includedBones = new HashSet<>(includedBones);
        rig.notifyAnimationSourceChanged();
    }
    public Set<String> getIncludedItemDisplays() { return includedItemDisplays; }
    public void setIncludedItemDisplays(Set<String> includedItemDisplays) {
        this.includedItemDisplays = new HashSet<>(includedItemDisplays);
        rig.notifyAnimationSourceChanged();
    }
    public Set<String> getIncludedBlockDisplays() { return includedBlockDisplays; }
    public void setIncludedBlockDisplays(Set<String> includedBlockDisplays) {
        this.includedBlockDisplays = new HashSet<>(includedBlockDisplays);
        rig.notifyAnimationSourceChanged();
    }
    public Set<String> getIncludedTextDisplays() { return includedTextDisplays; }
    public void setIncludedTextDisplays(Set<String> includedTextDisplays) {
        this.includedTextDisplays = new HashSet<>(includedTextDisplays);
        rig.notifyAnimationSourceChanged();
    }
    public Set<String> getIncludedLocators() { return includedLocators; }
    public void setIncludedLocators(Set<String> includedLocators) {
        this.includedLocators = new HashSet<>(includedLocators);
        rig.notifyAnimationSourceChanged();
    }

    // --- Utility ---
    public void updateIncludedEntities() {
        if (animationName == null) {
            includedBones.clear();
            includedItemDisplays.clear();
            includedBlockDisplays.clear();
            includedTextDisplays.clear();
            includedLocators.clear();
            System.out.println("[AJAnimationSource] updateIncludedEntities -> source=" + sourceId + " anim=<null> cleared included sets");
            return;
        }
        String ns = rig.getExportNamespace();
        String anim = animationName;
        includedBones = new HashSet<>(AJFrameData.getBonesForFrame(ns, anim, 0));
        includedItemDisplays = new HashSet<>(AJFrameData.getItemDisplaysForFrame(ns, anim, 0));
        includedBlockDisplays = new HashSet<>(AJFrameData.getBlockDisplaysForFrame(ns, anim, 0));
        includedTextDisplays = new HashSet<>(AJFrameData.getTextDisplaysForFrame(ns, anim, 0));
        includedLocators = new HashSet<>(AJFrameData.getLocatorsForFrame(ns, anim, 0));

        // Debug output to help confirm behavior at runtime
        try {
            System.out.println("[AJAnimationSource] updateIncludedEntities -> source=" + sourceId +
                " anim=" + animationName +
                " bones=" + includedBones.size() +
                " items=" + includedItemDisplays.size() +
                " blocks=" + includedBlockDisplays.size() +
                " texts=" + includedTextDisplays.size() +
                " locators=" + includedLocators.size() +
                " sampleBone=" + (includedBones.isEmpty() ? "<none>" : includedBones.iterator().next()));
        } catch (Throwable t) {
            // ignore logging errors
        }
        rig.notifyAnimationSourceChanged();
    }
}