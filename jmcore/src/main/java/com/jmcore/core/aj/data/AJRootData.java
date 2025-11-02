package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores AJ root data for each export namespace.
 * Root data is loaded from aj_data/rig/<export_namespace>/root/root.txt.
 * Uses aj_data/index/root_index.txt to find all root files.
 * Currently stores tags, but is extensible for future fields.
 */
public class AJRootData {
    // exportNamespace -> RootData
    private static final Map<String, RootData> rootDataMap = new ConcurrentHashMap<>();

    public static class RootData {
        private final Set<String> tags = new HashSet<>();
        public Set<String> getTags() { return Collections.unmodifiableSet(tags); }
    }

    /**
     * Loads all root data using aj_data/index/root_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllRootData() {
        rootDataMap.clear();
        String indexPath = "aj_data/index/root_index.txt";
        String resourceRoot = "aj_data/rig/";
        try (InputStream indexStream = AJRootData.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJRootData] No " + indexPath + " found!");
                return;
            }
            BufferedReader indexReader = new BufferedReader(new InputStreamReader(indexStream));
            String line;
            while ((line = indexReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String resourcePath = resourceRoot + line;
                // Extract exportNamespace from the path (e.g., blueprint/root/root.txt -> blueprint)
                String[] parts = line.split("/");
                if (parts.length < 3) continue;
                String exportNamespace = parts[0];
                RootData data = new RootData();
                try (InputStream is = AJRootData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.out.println("[AJRootData] No root.txt found for namespace: " + exportNamespace);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String l;
                    boolean inTags = false;
                    while ((l = reader.readLine()) != null) {
                        l = l.trim();
                        if (l.equalsIgnoreCase("Tags:")) {
                            inTags = true;
                            continue;
                        }
                        if (inTags && l.startsWith("-")) {
                            String tag = l.substring(1).trim();
                            data.tags.add(tag);
                        }
                    }
                    rootDataMap.put(exportNamespace, data);
                } catch (Exception e) {
                    System.out.println("[AJRootData] Error loading root data for " + exportNamespace);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("[AJRootData] Error loading root index:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the RootData for the given export namespace, or null if not loaded.
     */
    public static RootData getRootData(String exportNamespace) {
        return rootDataMap.get(exportNamespace);
    }
}