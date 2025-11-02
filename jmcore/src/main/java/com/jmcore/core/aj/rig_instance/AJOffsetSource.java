package com.jmcore.core.aj.rig_instance;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class AJOffsetSource {
    private final AJRigInstance rig;
    private final String sourceId;
    private int order;
    private boolean enabled = true;

    private Quaternionf rotation = new Quaternionf().identity();
    private Vector3f translation = new Vector3f(0, 0, 0);
    private Vector3f scale = new Vector3f(1, 1, 1);

    private Set<String> includedBones = new HashSet<>();
    private Set<String> includedItemDisplays = new HashSet<>();
    private Set<String> includedBlockDisplays = new HashSet<>();
    private Set<String> includedTextDisplays = new HashSet<>();
    private Set<String> includedLocators = new HashSet<>();

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
}