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
    // exportNamespace -> blockDisplayName -> BlockDisplayData
    private static final Map<String, Map<String, BlockDisplayData>> blockDisplayDataMap = new ConcurrentHashMap<>();

    public static class BlockDisplayData {
        public final String blockDisplayName;
        public final String block;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;

        public BlockDisplayData(String blockDisplayName, String block, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags) {
            this.blockDisplayName = blockDisplayName;
            this.block = block;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
        }
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
                try (InputStream blockStream = AJBlockDisplayData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (blockStream == null) {
                        System.out.println("[AJBlockDisplayData] Block display file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(blockStream));
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
                    if (bName == null) {
                        System.out.println("[AJBlockDisplayData] Missing required field: Block Display Name in " + resourcePath);
                        continue;
                    }
                    BlockDisplayData data = new BlockDisplayData(bName, block, bboxHeight, bboxWidth, tags);
                    blockDisplayDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(blockDisplayName, data);
                    System.out.println("[AJBlockDisplayData] Loaded block display: " + bName + " in namespace: " + exportNamespace);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJBlockDisplayData] Error loading block display data:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the BlockDisplayData for the given export namespace and block display name, or null if not loaded.
     */
    public static BlockDisplayData getBlockDisplayData(String exportNamespace, String blockDisplayName) {
        Map<String, BlockDisplayData> nsMap = blockDisplayDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(blockDisplayName) : null;
    }

    /**
     * Returns a set of all block display names for the given export namespace.
     */
    public static Set<String> getAllBlockDisplayNames(String exportNamespace) {
        Map<String, BlockDisplayData> nsMap = blockDisplayDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}