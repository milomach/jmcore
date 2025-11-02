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
    // exportNamespace -> boneName -> BoneData
    private static final Map<String, Map<String, BoneData>> boneDataMap = new ConcurrentHashMap<>();

    public static class BoneData {
        public final String boneName;
        public final String item;
        public final String itemModelPath;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;

        public BoneData(String boneName, String item, String itemModelPath, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags) {
            this.boneName = boneName;
            this.item = item;
            this.itemModelPath = itemModelPath;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
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
                    BoneData data = new BoneData(bName, item, itemModelPath, bboxHeight, bboxWidth, tags);
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