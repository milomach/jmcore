package com.jmcore.core.aj.rig_instance;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

import com.jmcore.core.aj.data.AnimatedOffsetRegistry;

/**
 * AJOffsetSource
 *
 * Supports both static and animated offset modes.
 * Animated offsets use animation data from a specified export namespace and animation name,
 * and support frame advancement and end behavior like animation sources.
 */
public class AJOffsetSource {
    private final AJRigInstance rig;
    private final String sourceId;
    private int order;
    private boolean enabled = true;

    // --- Offset transform (static mode) ---
    private Quaternionf rotation = new Quaternionf().identity();
    private Vector3f translation = new Vector3f(0, 0, 0);
    private Vector3f scale = new Vector3f(1, 1, 1);

    // --- Included sets ---
    private Set<String> includedBones = new HashSet<>();
    private Set<String> includedItemDisplays = new HashSet<>();
    private Set<String> includedBlockDisplays = new HashSet<>();
    private Set<String> includedTextDisplays = new HashSet<>();
    private Set<String> includedLocators = new HashSet<>();

    // --- Animated offset fields ---
    public enum OffsetMode { STATIC, ANIMATED }
    private OffsetMode offsetMode = OffsetMode.STATIC;

    // The export namespace for the animated offset (was previously called "blueprint")
    private String exportNamespace = null;
    // The animation name for the animated offset
    private String animationName = null;
    private EndBehavior endBehavior = EndBehavior.HOLD;
    private boolean playing = false;
    private int currentFrame = 0;

    public enum EndBehavior {
        RESET, // Rewind to first frame and stop
        HOLD,  // Stop at last frame
        LOOP   // Repeat from first frame
    }

    public AJOffsetSource(AJRigInstance rig, String sourceId, int order) {
        this.rig = rig;
        this.sourceId = sourceId;
        this.order = order;
    }

    // --- Getters and setters ---
    public AJRigInstance getRig() { return rig; }
    public String getSourceId() { return sourceId; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Quaternionf getRotation() { return new Quaternionf(rotation); }
    public void setRotation(Quaternionf rotation) { this.rotation.set(rotation); }
    public Vector3f getTranslation() { return new Vector3f(translation); }
    public void setTranslation(Vector3f translation) { this.translation.set(translation); }
    public Vector3f getScale() { return new Vector3f(scale); }
    public void setScale(Vector3f scale) { this.scale.set(scale); }

    public Set<String> getIncludedBones() { return includedBones; }
    public void setIncludedBones(Set<String> includedBones) { this.includedBones = new HashSet<>(includedBones); }
    public Set<String> getIncludedItemDisplays() { return includedItemDisplays; }
    public void setIncludedItemDisplays(Set<String> includedItemDisplays) { this.includedItemDisplays = new HashSet<>(includedItemDisplays); }
    public Set<String> getIncludedBlockDisplays() { return includedBlockDisplays; }
    public void setIncludedBlockDisplays(Set<String> includedBlockDisplays) { this.includedBlockDisplays = new HashSet<>(includedBlockDisplays); }
    public Set<String> getIncludedTextDisplays() { return includedTextDisplays; }
    public void setIncludedTextDisplays(Set<String> includedTextDisplays) { this.includedTextDisplays = new HashSet<>(includedTextDisplays); }
    public Set<String> getIncludedLocators() { return includedLocators; }
    public void setIncludedLocators(Set<String> includedLocators) { this.includedLocators = new HashSet<>(includedLocators); }

    // --- Offset mode ---
    public OffsetMode getOffsetMode() { return offsetMode; }
    public void setOffsetMode(OffsetMode mode) { this.offsetMode = mode; }

    // --- Animated offset controls ---
    public String getExportNamespace() { return exportNamespace; }
    public String getAnimationName() { return animationName; }
    public EndBehavior getEndBehavior() { return endBehavior; }
    public boolean isPlaying() { return playing; }
    public int getCurrentFrame() { return currentFrame; }

    /**
     * Sets the animated offset animation, if valid for the given export namespace.
     * Returns true if set, false if invalid.
     * This is the only way to set animationName and exportNamespace.
     */
    public boolean setAnimatedOffset(String exportNamespace, String animationName) {
        if (AnimatedOffsetRegistry.isValidAnimatedOffset(exportNamespace, animationName)) {
            this.exportNamespace = exportNamespace;
            this.animationName = animationName;
            this.currentFrame = 0;
            return true;
        }
        return false;
    }

    public void setEndBehavior(EndBehavior behavior) { this.endBehavior = behavior; }
    public void setPlaying(boolean playing) { this.playing = playing; }
    public void setCurrentFrame(int frame) { this.currentFrame = frame; }

    /**
     * Advances the current frame if playing, using the end behavior.
     * Returns true if the frame was advanced, false if not playing or at end.
     * Requires the caller to provide the frame count for the current animation.
     */
    public boolean advanceFrame(int frameCount) {
        if (!playing || frameCount <= 0) return false;
        if (currentFrame < frameCount - 1) {
            currentFrame++;
            return true;
        } else {
            // Handle end behavior
            switch (endBehavior) {
                case HOLD:
                    currentFrame = frameCount - 1;
                    playing = false;
                    break;
                case RESET:
                    currentFrame = 0;
                    playing = false;
                    break;
                case LOOP:
                    currentFrame = 0;
                    break;
            }
            return false;
        }
    }

    /**
     * Stops playback and resets to frame 0.
     */
    public void stop() {
        playing = false;
        currentFrame = 0;
    }

    /**
     * Starts playback from the current frame.
     */
    public void play() {
        playing = true;
    }
}