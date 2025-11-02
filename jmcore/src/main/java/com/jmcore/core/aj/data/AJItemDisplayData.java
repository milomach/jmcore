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
    // exportNamespace -> itemDisplayName -> ItemDisplayData
    private static final Map<String, Map<String, ItemDisplayData>> itemDisplayDataMap = new ConcurrentHashMap<>();

    public static class ItemDisplayData {
        public final String itemDisplayName;
        public final String item;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;

        public ItemDisplayData(String itemDisplayName, String item, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags) {
            this.itemDisplayName = itemDisplayName;
            this.item = item;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
        }
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
                try (InputStream itemStream = AJItemDisplayData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (itemStream == null) {
                        System.out.println("[AJItemDisplayData] Item display file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(itemStream));
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
                    if (iName == null) {
                        System.out.println("[AJItemDisplayData] Missing required field: Item Display Name in " + resourcePath);
                        continue;
                    }
                    ItemDisplayData data = new ItemDisplayData(iName, item, bboxHeight, bboxWidth, tags);
                    itemDisplayDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(itemDisplayName, data);
                    System.out.println("[AJItemDisplayData] Loaded item display: " + iName + " in namespace: " + exportNamespace);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJItemDisplayData] Error loading item display data:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the ItemDisplayData for the given export namespace and item display name, or null if not loaded.
     */
    public static ItemDisplayData getItemDisplayData(String exportNamespace, String itemDisplayName) {
        Map<String, ItemDisplayData> nsMap = itemDisplayDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(itemDisplayName) : null;
    }

    /**
     * Returns a set of all item display names for the given export namespace.
     */
    public static Set<String> getAllItemDisplayNames(String exportNamespace) {
        Map<String, ItemDisplayData> nsMap = itemDisplayDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}