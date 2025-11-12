package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores AJ block display data for each export namespace and block display name.
 * Data is loaded from aj_data/rig/<export_namespace>/block_displays/<block_display_name>.txt.
 * Uses aj_data/index/block_display_index.txt to find all block display files.
 */
public class AJBlockDisplayData {
    public static final String PARENT_MODEL_ROOT = "__MODEL_ROOT__";

    // exportNamespace -> blockDisplayName -> BlockDisplayData
    private static final Map<String, Map<String, BlockDisplayData>> blockDisplayDataMap = new ConcurrentHashMap<>();

    public static class BlockDisplayData {
        public final String blockDisplayName;
        public final String block;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;
        /**
         * The parent bone name, or PARENT_MODEL_ROOT if at the root, or null if unknown.
         */
        public final String parent;

        public BlockDisplayData(String blockDisplayName, String block, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags, String parent) {
            this.blockDisplayName = blockDisplayName;
            this.block = block;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
            this.parent = parent;
        }

        public String getParent() { return parent; }
    }

    /**
     * Loads all block display data from aj_data/index/block_display_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllBlockDisplayData() {
        blockDisplayDataMap.clear();
        String indexPath = "aj_data/index/block_display_index.txt";
        String resourceRoot = "aj_data/rig/";
        try (InputStream indexStream = AJBlockDisplayData.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJBlockDisplayData] No " + indexPath + " found!");
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
                String blockDisplayName = parts[2].replace(".txt", "");
                try (InputStream is = AJBlockDisplayData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.out.println("[AJBlockDisplayData] Block display file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String bName = null, block = null;
                    int bboxHeight = 0, bboxWidth = 0;
                    Set<String> tags = new HashSet<>();
                    boolean inTags = false;
                    String l;
                    while ((l = reader.readLine()) != null) {
                        l = l.trim();
                        if (l.startsWith("Block Display Name:")) bName = l.substring("Block Display Name:".length()).trim();
                        else if (l.startsWith("Block:")) block = l.substring("Block:".length()).trim();
                        else if (l.startsWith("Bounding Box Height:")) {
                            String value = l.substring("Bounding Box Height:".length()).trim();
                            bboxHeight = value.isEmpty() ? 0 : Integer.parseInt(value);
                        } else if (l.startsWith("Bounding Box Width:")) {
                            String value = l.substring("Bounding Box Width:".length()).trim();
                            bboxWidth = value.isEmpty() ? 0 : Integer.parseInt(value);
                        } else if (l.equalsIgnoreCase("Tags:")) inTags = true;
                        else if (inTags && l.startsWith("-")) tags.add(l.substring(1).trim());
                    }
                    if (bName == null) bName = blockDisplayName;

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

                    BlockDisplayData data = new BlockDisplayData(bName, block, bboxHeight, bboxWidth, tags, parent);
                    blockDisplayDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(blockDisplayName, data);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJBlockDisplayData] Error loading block display data:");
            e.printStackTrace();
        }
    }

    public static BlockDisplayData getBlockDisplayData(String exportNamespace, String blockDisplayName) {
        Map<String, BlockDisplayData> nsMap = blockDisplayDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(blockDisplayName) : null;
    }

    public static Set<String> getAllBlockDisplayNames(String exportNamespace) {
        Map<String, BlockDisplayData> nsMap = blockDisplayDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}