package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores AJ item display data for each export namespace and item display name.
 * Data is loaded from aj_data/rig/<export_namespace>/item_displays/<item_display_name>.txt.
 * Uses aj_data/index/item_display_index.txt to find all item display files.
 */
public class AJItemDisplayData {
    public static final String PARENT_MODEL_ROOT = "__MODEL_ROOT__";

    // exportNamespace -> itemDisplayName -> ItemDisplayData
    private static final Map<String, Map<String, ItemDisplayData>> itemDisplayDataMap = new ConcurrentHashMap<>();

    public static class ItemDisplayData {
        public final String itemDisplayName;
        public final String item;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;
        /**
         * The parent bone name, or PARENT_MODEL_ROOT if at the root, or null if unknown.
         */
        public final String parent;

        public ItemDisplayData(String itemDisplayName, String item, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags, String parent) {
            this.itemDisplayName = itemDisplayName;
            this.item = item;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
            this.parent = parent;
        }

        public String getParent() { return parent; }
    }

    /**
     * Loads all item display data from aj_data/index/item_display_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllItemDisplayData() {
        itemDisplayDataMap.clear();
        String indexPath = "aj_data/index/item_display_index.txt";
        String resourceRoot = "aj_data/rig/";
        try (InputStream indexStream = AJItemDisplayData.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJItemDisplayData] No " + indexPath + " found!");
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
                String itemDisplayName = parts[2].replace(".txt", "");
                try (InputStream is = AJItemDisplayData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.out.println("[AJItemDisplayData] Item display file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String iName = null, item = null;
                    int bboxHeight = 0, bboxWidth = 0;
                    Set<String> tags = new HashSet<>();
                    boolean inTags = false;
                    String l;
                    while ((l = reader.readLine()) != null) {
                        l = l.trim();
                        if (l.startsWith("Item Display Name:")) iName = l.substring("Item Display Name:".length()).trim();
                        else if (l.startsWith("Item:")) item = l.substring("Item:".length()).trim();
                        else if (l.startsWith("Bounding Box Height:")) {
                            String value = l.substring("Bounding Box Height:".length()).trim();
                            bboxHeight = value.isEmpty() ? 0 : Integer.parseInt(value);
                        } else if (l.startsWith("Bounding Box Width:")) {
                            String value = l.substring("Bounding Box Width:".length()).trim();
                            bboxWidth = value.isEmpty() ? 0 : Integer.parseInt(value);
                        } else if (l.equalsIgnoreCase("Tags:")) inTags = true;
                        else if (inTags && l.startsWith("-")) tags.add(l.substring(1).trim());
                    }
                    if (iName == null) iName = itemDisplayName;

                    // --- Parent detection logic ---
                    String parent = null;
                    for (String tag : tags) {
                        if (tag.equals("aj.global.root.child")) {
                            parent = PARENT_MODEL_ROOT;
                            break;
                        } else if (tag.startsWith("aj.global.bone.") && tag.endsWith(".child")) {
                            String[] tagParts = tag.split("\\.");
                            if (tagParts.length >= 5) {
                                parent = tagParts[3];
                                break;
                            }
                        }
                    }
                    // If no parent tag, parent remains null

                    ItemDisplayData data = new ItemDisplayData(iName, item, bboxHeight, bboxWidth, tags, parent);
                    itemDisplayDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(itemDisplayName, data);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJItemDisplayData] Error loading item display data:");
            e.printStackTrace();
        }
    }

    public static ItemDisplayData getItemDisplayData(String exportNamespace, String itemDisplayName) {
        Map<String, ItemDisplayData> nsMap = itemDisplayDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(itemDisplayName) : null;
    }

    public static Set<String> getAllItemDisplayNames(String exportNamespace) {
        Map<String, ItemDisplayData> nsMap = itemDisplayDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}