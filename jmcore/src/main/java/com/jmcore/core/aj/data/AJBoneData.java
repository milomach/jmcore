package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores AJ bone data for each export namespace and bone name.
 * Bone data is loaded from aj_data/rig/<export_namespace>/bones/<bone_name>.txt.
 * Uses aj_data/index/bone_index.txt to find all bone files.
 */
public class AJBoneData {
    // Special constant for model root parent (not a bone named "root")
    public static final String PARENT_MODEL_ROOT = "__MODEL_ROOT__";

    // exportNamespace -> boneName -> BoneData
    private static final Map<String, Map<String, BoneData>> boneDataMap = new ConcurrentHashMap<>();

    public static class BoneData {
        public final String boneName;
        public final String item;
        public final String itemModelPath;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;
        /**
         * The parent bone name, or PARENT_MODEL_ROOT if at the root, or null if unknown.
         */
        public final String parent;

        public BoneData(String boneName, String item, String itemModelPath, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags, String parent) {
            this.boneName = boneName;
            this.item = item;
            this.itemModelPath = itemModelPath;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
            this.parent = parent;
        }

        /**
         * Returns the parent bone name, or PARENT_MODEL_ROOT if at the root, or null if unknown.
         */
        public String getParent() {
            return parent;
        }
    }

    /**
     * Loads all bone data from aj_data/index/bone_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllBoneData() {
        boneDataMap.clear();
        String indexPath = "aj_data/index/bone_index.txt";
        String resourceRoot = "aj_data/rig/";
        try (InputStream indexStream = AJBoneData.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJBoneData] No " + indexPath + " found!");
                return;
            }
            BufferedReader indexReader = new BufferedReader(new InputStreamReader(indexStream));
            String line;
            while ((line = indexReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String resourcePath = resourceRoot + line;
                String[] parts = line.split("/");
                if (parts.length < 3) continue;
                String exportNamespace = parts[0];
                String boneName = parts[2].replace(".txt", "");
                try (InputStream boneStream = AJBoneData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (boneStream == null) {
                        System.out.println("[AJBoneData] Bone file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(boneStream));
                    String bName = null, item = null, itemModelPath = null;
                    int bboxHeight = 0, bboxWidth = 0;
                    Set<String> tags = new HashSet<>();
                    boolean inTags = false;
                    String l;
                    while ((l = reader.readLine()) != null) {
                        l = l.trim();
                        if (l.startsWith("Bone Name:")) bName = l.substring("Bone Name:".length()).trim();
                        else if (l.startsWith("Item:")) item = l.substring("Item:".length()).trim();
                        else if (l.startsWith("Item Model Path:")) itemModelPath = l.substring("Item Model Path:".length()).trim();
                        else if (l.startsWith("Bounding Box Height:")) bboxHeight = Integer.parseInt(l.substring("Bounding Box Height:".length()).trim());
                        else if (l.startsWith("Bounding Box Width:")) bboxWidth = Integer.parseInt(l.substring("Bounding Box Width:".length()).trim());
                        else if (l.equalsIgnoreCase("Tags:")) inTags = true;
                        else if (inTags && l.startsWith("-")) tags.add(l.substring(1).trim());
                    }
                    if (bName == null) bName = boneName;

                    // --- Parent detection logic ---
                    String parent = null;
                    for (String tag : tags) {
                        if (tag.equals("aj.global.root.child")) {
                            parent = PARENT_MODEL_ROOT;
                            break;
                        } else if (tag.startsWith("aj.global.bone.") && tag.endsWith(".child")) {
                            // Example: aj.global.bone.center.child
                            String[] tagParts = tag.split("\\.");
                            if (tagParts.length >= 5) {
                                parent = tagParts[3]; // <bone_name>
                                break;
                            }
                        }
                    }
                    // If no parent tag, parent remains null

                    BoneData data = new BoneData(bName, item, itemModelPath, bboxHeight, bboxWidth, tags, parent);
                    boneDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(boneName, data);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJBoneData] Error loading bone data:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the BoneData for the given export namespace and bone name, or null if not loaded.
     */
    public static BoneData getBoneData(String exportNamespace, String boneName) {
        Map<String, BoneData> nsMap = boneDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(boneName) : null;
    }

    /**
     * Returns a set of all bone names for the given export namespace.
     */
    public static Set<String> getAllBoneNames(String exportNamespace) {
        Map<String, BoneData> nsMap = boneDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}