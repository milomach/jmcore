package com.jmcore.core.aj.rig_instance;

import org.joml.Vector3f;

import com.jmcore.core.aj.data.AJBlockDisplayData;
import com.jmcore.core.aj.data.AJItemDisplayData;
import com.jmcore.core.aj.data.AJTextDisplayData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.Location;

import java.util.*;

public class AJRigInstance {
    // --- Scope ---
    public enum AJRigScope {
        PLAYER,
        GLOBAL
    }

    private final String exportNamespace;
    private final String internalId;
    private UUID rootEntityUUID = null;

    // --- Entity UUID sets for each type ---
    private final Set<UUID> boneEntityUUIDs = new HashSet<>();
    private final Set<UUID> itemDisplayEntityUUIDs = new HashSet<>();
    private final Set<UUID> blockDisplayEntityUUIDs = new HashSet<>();
    private final Set<UUID> textDisplayEntityUUIDs = new HashSet<>();

    // --- Info classes for each type ---
    public static class BoneInfo {
        public final String boneName;
        public UUID entityUUID = null;
        public Map<String, Boolean> appliedStates = new HashMap<>();

        // Interpolation for posing
        public Integer poseInterpDelay = 0;
        public Integer poseInterpDuration = 1;
        public Integer poseTeleportDuration = 1;

        // State fields
        public boolean enchanted = false;
        public String billboardMode = "fixed"; // fixed/vertical/horizontal/center
        public String brightnessMode = "ambient"; // custom/ambient
        public int blockBrightness = 0; // only used if brightnessMode is custom
        public int skyBrightness = 0;   // only used if brightnessMode is custom
        public float displayWidth = 48f;
        public float displayHeight = 48f;
        public String glowMode = "none"; // custom/none
        public int glowColor = 0xFFFFFF; // only used if glowMode is custom
        public float shadowRadius = 0f;
        public float shadowStrength = 0f;
        public float viewRange = 9999999f;
        public boolean defaultVisibility = false;
        public Set<UUID> shownForPlayers = new HashSet<>(); // cannot overlap hiddenForPlayers
        public Set<UUID> hiddenForPlayers = new HashSet<>(); // cannot overlap shownForPlayers
        public String variant = "default";

        public BoneInfo(String boneName) {
            this.boneName = boneName;
        }
    }

    public static class ItemDisplayInfo {
        public final String itemDisplayName;
        public String animationName;
        public int frame;
        public UUID entityUUID = null;
        public Map<String, Boolean> appliedStates = new HashMap<>();

        // Interpolation for posing
        public Integer poseInterpDelay = 0;
        public Integer poseInterpDuration = 1;
        public Integer poseTeleportDuration = 1;

        // State fields
        public boolean enchanted = false;
        public String billboardMode = "fixed";
        public String brightnessMode = "ambient";
        public int blockBrightness = 0;
        public int skyBrightness = 0;
        public float displayWidth = 48f;
        public float displayHeight = 48f;
        public String glowMode = "none";
        public int glowColor = 0xFFFFFF;
        public float shadowRadius = 0f;
        public float shadowStrength = 0f;
        public float viewRange = 9999999f;
        public boolean defaultVisibility = false;
        public Set<UUID> shownForPlayers = new HashSet<>();
        public Set<UUID> hiddenForPlayers = new HashSet<>();
        public String item = "STICK"; // default, will be set in initializeAllInfos

        public ItemDisplayInfo(String itemDisplayName) {
            this.itemDisplayName = itemDisplayName;
        }
    }

    public static class BlockDisplayInfo {
        public final String blockDisplayName;
        public UUID entityUUID = null;
        public Map<String, Boolean> appliedStates = new HashMap<>();

        // Interpolation for posing
        public Integer poseInterpDelay = 0;
        public Integer poseInterpDuration = 1;
        public Integer poseTeleportDuration = 1;

        // State fields
        public String billboardMode = "fixed";
        public String brightnessMode = "ambient";
        public int blockBrightness = 0;
        public int skyBrightness = 0;
        public float displayWidth = 48f;
        public float displayHeight = 48f;
        public String glowMode = "none";
        public int glowColor = 0xFFFFFF;
        public float shadowRadius = 0f;
        public float shadowStrength = 0f;
        public float viewRange = 9999999f;
        public boolean defaultVisibility = false;
        public Set<UUID> shownForPlayers = new HashSet<>();
        public Set<UUID> hiddenForPlayers = new HashSet<>();
        public String block = "STONE"; // default, will be set in initializeAllInfos

        public BlockDisplayInfo(String blockDisplayName) {
            this.blockDisplayName = blockDisplayName;
        }
    }

    public static class TextDisplayInfo {
        public final String textDisplayName;
        public UUID entityUUID = null;
        public Map<String, Boolean> appliedStates = new HashMap<>();

        // Interpolation for posing
        public Integer poseInterpDelay = 0;
        public Integer poseInterpDuration = 1;
        public Integer poseTeleportDuration = 1;

        // State fields
        public String billboardMode = "fixed";
        public String brightnessMode = "ambient";
        public int blockBrightness = 0;
        public int skyBrightness = 0;
        public float displayWidth = 48f;
        public float displayHeight = 48f;
        public String glowMode = "none";
        public int glowColor = 0xFFFFFF;
        public float shadowRadius = 0f;
        public float shadowStrength = 0f;
        public float viewRange = 9999999f;
        public boolean defaultVisibility = false;
        public Set<UUID> shownForPlayers = new HashSet<>();
        public Set<UUID> hiddenForPlayers = new HashSet<>();
        public String text = ""; // default, will be set in initializeAllInfos
        public String backgroundMode = "default";
        public int backgroundColor = 0x000000;
        public int opacity = 255;
        public boolean seeThrough = false;
        public boolean shadowed = false;

        public TextDisplayInfo(String textDisplayName) {
            this.textDisplayName = textDisplayName;
        }
    }

    public static class LocatorInfo {
        public final String locatorName;
        public UUID entityUUID = null;

        // Interpolation for posing (not used currently)
        public Integer poseInterpDelay = 0;
        public Integer poseInterpDuration = 1;
        public Integer poseTeleportDuration = 1;

        // Current calculated position and rotation
        public Vector3f currentPosition = null;
        public float currentYaw = 0f;
        public float currentPitch = 0f;

        public LocatorInfo(String locatorName) {
            this.locatorName = locatorName;
        }
    }

    // --- Info maps for each type ---
    private final Map<String, BoneInfo> boneInfos = new HashMap<>();
    private final Map<String, ItemDisplayInfo> itemDisplayInfos = new HashMap<>();
    private final Map<String, BlockDisplayInfo> blockDisplayInfos = new HashMap<>();
    private final Map<String, TextDisplayInfo> textDisplayInfos = new HashMap<>();
    private final Map<String, LocatorInfo> locatorInfos = new HashMap<>();

    // --- Entity cache ---
    private final Map<UUID, Entity> entityCache = new HashMap<>();

    // --- Previous root location tracking ---
    private Location previousRootLocation = null;
    private float previousRootYaw = 0f;
    private float previousRootPitch = 0f;

    // --- Constructor: only sets up the rig instance, does not summon entities ---
    public AJRigInstance(String exportNamespace, String internalId) {
        this.exportNamespace = exportNamespace;
        this.internalId = internalId;
        // Info maps are initialized in setup
    }

    // --- Setup/cleanup support ---
    public void initializeAllInfos(
            Set<String> boneNames,
            Set<String> locatorNames,
            Set<String> itemDisplayNames,
            Set<String> blockDisplayNames,
            Set<String> textDisplayNames
    ) {
        boneInfos.clear();
        locatorInfos.clear();
        itemDisplayInfos.clear();
        blockDisplayInfos.clear();
        textDisplayInfos.clear();

        System.out.println("[AJRigInstance] initializeAllInfos -> Starting initialization for rig=" + internalId + " ns=" + exportNamespace);

        // Debug: Print counts of each type
        System.out.println("  boneNames=" + boneNames.size() + " locatorNames=" + locatorNames.size() +
                " itemDisplayNames=" + itemDisplayNames.size() +
                " blockDisplayNames=" + blockDisplayNames.size() +
                " textDisplayNames=" + textDisplayNames.size());

        for (String boneName : boneNames) boneInfos.put(boneName, new BoneInfo(boneName));
        for (String locatorName : locatorNames) locatorInfos.put(locatorName, new LocatorInfo(locatorName));

        for (String name : itemDisplayNames) {
            ItemDisplayInfo info = new ItemDisplayInfo(name);
            AJItemDisplayData.ItemDisplayData data = AJItemDisplayData.getItemDisplayData(exportNamespace, name);
            if (data != null) {
                info.item = data.item;
            } else {
                System.out.println("[AJRigInstance] Warning: Item display data not found for " + name);
            }
            itemDisplayInfos.put(name, info);
        }

        for (String name : blockDisplayNames) {
            BlockDisplayInfo info = new BlockDisplayInfo(name);
            AJBlockDisplayData.BlockDisplayData data = AJBlockDisplayData.getBlockDisplayData(exportNamespace, name);
            if (data != null) {
                info.block = data.block;
            } else {
                System.out.println("[AJRigInstance] Warning: Block display data not found for " + name);
            }
            blockDisplayInfos.put(name, info);
        }

        for (String name : textDisplayNames) {
            TextDisplayInfo info = new TextDisplayInfo(name);
            AJTextDisplayData.TextDisplayData data = AJTextDisplayData.getTextDisplayData(exportNamespace, name);
            if (data != null) {
                info.text = data.text;
            } else {
                System.out.println("[AJRigInstance] Warning: Text display data not found for " + name);
            }
            textDisplayInfos.put(name, info);
        }

        // Debug: Print final counts
        System.out.println("[AJRigInstance] initializeAllInfos -> Completed initialization");
        System.out.println("  boneInfos=" + boneInfos.size() + " locatorInfos=" + locatorInfos.size() +
                " itemDisplayInfos=" + itemDisplayInfos.size() +
                " blockDisplayInfos=" + blockDisplayInfos.size() +
                " textDisplayInfos=" + textDisplayInfos.size());
    }

    public void clearEntities() {
        rootEntityUUID = null;
        boneEntityUUIDs.clear();
        itemDisplayEntityUUIDs.clear();
        blockDisplayEntityUUIDs.clear();
        textDisplayEntityUUIDs.clear();
        for (BoneInfo info : boneInfos.values()) info.entityUUID = null;
        for (ItemDisplayInfo info : itemDisplayInfos.values()) info.entityUUID = null;
        for (BlockDisplayInfo info : blockDisplayInfos.values()) info.entityUUID = null;
        for (TextDisplayInfo info : textDisplayInfos.values()) info.entityUUID = null;
        for (LocatorInfo info : locatorInfos.values()) info.entityUUID = null;
        entityCache.clear();
    }

    public void clearAll() {
        clearEntities();
        previousRootLocation = null;
        previousRootYaw = 0f;
        previousRootPitch = 0f;
        boneInfos.clear();
        locatorInfos.clear();
        itemDisplayInfos.clear();
        blockDisplayInfos.clear();
        textDisplayInfos.clear();
    }

    // --- Getters/setters for entity management ---
    public String getExportNamespace() { return exportNamespace; }
    public String getInternalId() { return internalId; }
    public UUID getRootEntityUUID() { return rootEntityUUID; }
    public void setRootEntityUUID(UUID uuid) { this.rootEntityUUID = uuid; }

    public Set<UUID> getBoneEntityUUIDs() { return boneEntityUUIDs; }
    public void addBoneEntityUUID(UUID uuid) { boneEntityUUIDs.add(uuid); }
    public void removeBoneEntityUUID(UUID uuid) { boneEntityUUIDs.remove(uuid); }

    public Set<UUID> getItemDisplayEntityUUIDs() { return itemDisplayEntityUUIDs; }
    public void addItemDisplayEntityUUID(UUID uuid) { itemDisplayEntityUUIDs.add(uuid); }
    public void removeItemDisplayEntityUUID(UUID uuid) { itemDisplayEntityUUIDs.remove(uuid); }

    public Set<UUID> getBlockDisplayEntityUUIDs() { return blockDisplayEntityUUIDs; }
    public void addBlockDisplayEntityUUID(UUID uuid) { blockDisplayEntityUUIDs.add(uuid); }
    public void removeBlockDisplayEntityUUID(UUID uuid) { blockDisplayEntityUUIDs.remove(uuid); }

    public Set<UUID> getTextDisplayEntityUUIDs() { return textDisplayEntityUUIDs; }
    public void addTextDisplayEntityUUID(UUID uuid) { textDisplayEntityUUIDs.add(uuid); }
    public void removeTextDisplayEntityUUID(UUID uuid) { textDisplayEntityUUIDs.remove(uuid); }

    // --- Info access for each type ---
    public BoneInfo getBoneInfo(String boneName) { return boneInfos.get(boneName); }
    public ItemDisplayInfo getItemDisplayInfo(String name) { return itemDisplayInfos.get(name); }
    public BlockDisplayInfo getBlockDisplayInfo(String name) { return blockDisplayInfos.get(name); }
    public TextDisplayInfo getTextDisplayInfo(String name) { return textDisplayInfos.get(name); }
    public LocatorInfo getLocatorInfo(String locatorName) { return locatorInfos.get(locatorName); }

    public Collection<BoneInfo> getAllBoneInfos() { return boneInfos.values(); }
    public Collection<ItemDisplayInfo> getAllItemDisplayInfos() { return itemDisplayInfos.values(); }
    public Collection<BlockDisplayInfo> getAllBlockDisplayInfos() { return blockDisplayInfos.values(); }
    public Collection<TextDisplayInfo> getAllTextDisplayInfos() { return textDisplayInfos.values(); }
    public Collection<LocatorInfo> getAllLocatorInfos() { return locatorInfos.values(); }

    public Set<String> getBoneNames() { return boneInfos.keySet(); }
    public Set<String> getItemDisplayNames() { return itemDisplayInfos.keySet(); }
    public Set<String> getBlockDisplayNames() { return blockDisplayInfos.keySet(); }
    public Set<String> getTextDisplayNames() { return textDisplayInfos.keySet(); }
    public Set<String> getLocatorNames() { return locatorInfos.keySet(); }

    // --- Previous root location tracking ---
    public Location getPreviousRootLocation() { return previousRootLocation == null ? null : previousRootLocation.clone(); }
    public void setPreviousRootLocation(Location loc) {
        previousRootLocation = loc == null ? null : loc.clone();
        if (loc != null) {
            previousRootYaw = loc.getYaw();
            previousRootPitch = loc.getPitch();
        }
    }
    public float getPreviousRootYaw() { return previousRootYaw; }
    public float getPreviousRootPitch() { return previousRootPitch; }

    // --- Entity caching ---
    public void cacheEntity(UUID uuid) {
        Entity e = Bukkit.getEntity(uuid);
        if (e != null) entityCache.put(uuid, e);
    }

    public Entity getCachedEntity(UUID uuid) {
        Entity e = entityCache.get(uuid);
        if (e == null || e.isDead()) {
            e = Bukkit.getEntity(uuid);
            if (e != null) entityCache.put(uuid, e);
        }
        return e;
    }

    public void cacheAllEntities() {
        for (UUID uuid : boneEntityUUIDs) cacheEntity(uuid);
        for (UUID uuid : itemDisplayEntityUUIDs) cacheEntity(uuid);
        for (UUID uuid : blockDisplayEntityUUIDs) cacheEntity(uuid);
        for (UUID uuid : textDisplayEntityUUIDs) cacheEntity(uuid);
        cacheEntity(rootEntityUUID);
    }

    // --- Info management for each type ---

    // Only locator pose interpolation should be set here; others are handled in state setter utils.
    public void setLocatorInterpolation(String locatorName, Integer interpDelay, Integer interpDuration, Integer teleportDuration) {
        LocatorInfo info = locatorInfos.get(locatorName);
        if (info != null) {
            info.poseInterpDelay = interpDelay;
            info.poseInterpDuration = interpDuration;
            info.poseTeleportDuration = teleportDuration;
        }
    }

    // --- Source management ---
    private final Map<String, AJAnimationSource> animationSources = new HashMap<>();
    private final Map<String, AJOffsetSource> offsetSources = new HashMap<>();

    // Dedicated default-pose source (managed separately)
    private AJDefaultPoseSource defaultPoseSource;

    // --- Animation Source API ---

    public AJAnimationSource createAnimationSource(String sourceId, int order) {
        if (animationSources.containsKey(sourceId)) return null;
        AJAnimationSource src = new AJAnimationSource(this, sourceId, order);
        animationSources.put(sourceId, src);
        // When animation sources change, recompute default-pose inclusions
        updateDefaultPoseInclusions();
        return src;
    }

    public void removeAnimationSource(String sourceId) {
        animationSources.remove(sourceId);
        updateDefaultPoseInclusions();
    }

    public AJAnimationSource getAnimationSource(String sourceId) {
        return animationSources.get(sourceId);
    }

    public Collection<AJAnimationSource> getAllAnimationSources() {
        List<AJAnimationSource> all = new ArrayList<>(animationSources.values());
        return all;
    }

    // --- Offset Source API ---
    public AJOffsetSource createOffsetSource(String sourceId, int order) {
        if (offsetSources.containsKey(sourceId)) return null;
        AJOffsetSource src = new AJOffsetSource(this, sourceId, order);
        offsetSources.put(sourceId, src);
        return src;
    }

    public void removeOffsetSource(String sourceId) {
        offsetSources.remove(sourceId);
    }

    public AJOffsetSource getOffsetSource(String sourceId) {
        return offsetSources.get(sourceId);
    }

    public Collection<AJOffsetSource> getAllOffsetSources() {
        return offsetSources.values();
    }

    // --- Notifier called by animation sources when they change (included sets / enabled / animationName) ---
    // AJAnimationSource calls rig.notifyAnimationSourceChanged() whenever a relevant setter is invoked.
    void notifyAnimationSourceChanged() {
        // TEMP DEBUG: log that the rig was notified and how many animation sources exist.
        try {
            int totalSources = animationSources.size();
            System.out.println("[AJRigInstance] notifyAnimationSourceChanged -> rig=" + internalId + " ns=" + exportNamespace +
                " animSources=" + totalSources);
        } catch (Throwable t) {
            // swallow to avoid affecting runtime
        }
        updateDefaultPoseInclusions();
    }

    /**
     * Compute the included sets for the default pose source.
     * The default pose should include any entities/locators NOT included in any other enabled animation source.
     * This delegates to the AJDefaultPoseSource.updateInclusions() helper which uses rig getters and the animationSources map.
     */
    public void updateDefaultPoseInclusions() {
        if (defaultPoseSource == null) return;
        defaultPoseSource.updateInclusions();
    }

    // --- Default pose source setup ---
    // Called after initializeAllInfos in rig setup (AJRigSetupUtil)
    public void setupDefaultPoseSource() {
        if (defaultPoseSource == null) {
            defaultPoseSource = new AJDefaultPoseSource(this);
            // Immediately compute inclusions based on current animation sources
            updateDefaultPoseInclusions();
        }
    }

    // Getter for the dedicated default pose source
    public AJDefaultPoseSource getDefaultPoseSource() { return defaultPoseSource; }
}