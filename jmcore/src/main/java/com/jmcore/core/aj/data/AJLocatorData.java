package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores AJ locator data for each export namespace and locator name.
 * Locator data is loaded from aj_data/rig/<export_namespace>/locators/<locator_name>.txt.
 * Uses aj_data/index/locator_index.txt to find all locator files.
 * Each locator file follows the format:
 *   Locator Name: <locator_name>
 *   Entity: <minecraft_entity_type>
 *   Tags:
 *     - tag1
 *     - tag2
 *     ...
 */
public class AJLocatorData {
    public static final String PARENT_MODEL_ROOT = "__MODEL_ROOT__";

    // exportNamespace -> locatorName -> LocatorData
    private static final Map<String, Map<String, LocatorData>> locatorDataMap = new ConcurrentHashMap<>();

    public static class LocatorData {
        private final String locatorName;
        private final String entityType;
        private final Set<String> tags;
        /**
         * The parent bone name, or PARENT_MODEL_ROOT if at the root, or null if unknown.
         */
        private final String parent;

        public LocatorData(String locatorName, String entityType, Set<String> tags, String parent) {
            this.locatorName = locatorName;
            this.entityType = entityType;
            this.tags = tags == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(tags));
            this.parent = parent;
        }

        public String getLocatorName() { return locatorName; }
        public String getEntityType() { return entityType; }
        public Set<String> getTags() { return tags; }
        public String getParent() { return parent; }
    }

    /**
     * Loads all locator data from aj_data/index/locator_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllLocatorData() {
        locatorDataMap.clear();
        String indexPath = "aj_data/index/locator_index.txt";
        String resourceRoot = "aj_data/rig/";
        try (InputStream indexStream = AJLocatorData.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJLocatorData] No " + indexPath + " found!");
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
                String locatorFile = parts[parts.length - 1];
                if (!locatorFile.endsWith(".txt")) continue;
                String locatorName = locatorFile.substring(0, locatorFile.length() - 4);

                LocatorData locatorData = parseLocatorData(resourcePath, locatorName);
                if (locatorData != null) {
                    locatorDataMap
                        .computeIfAbsent(exportNamespace, k -> new HashMap<>())
                        .put(locatorName, locatorData);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJLocatorData] Error loading locator index:");
            e.printStackTrace();
        }
    }

    /**
     * Parses a locator data file at the given resource path.
     */
    private static LocatorData parseLocatorData(String resourcePath, String locatorName) {
        try (InputStream is = AJLocatorData.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.out.println("[AJLocatorData] No locator file found at: " + resourcePath);
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            String foundLocatorName = null;
            String entityType = null;
            Set<String> tags = new HashSet<>();
            boolean inTags = false;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Locator Name:")) {
                    foundLocatorName = line.substring("Locator Name:".length()).trim();
                } else if (line.startsWith("Entity:")) {
                    entityType = line.substring("Entity:".length()).trim();
                } else if (line.equalsIgnoreCase("Tags:")) {
                    inTags = true;
                } else if (inTags && line.startsWith("-")) {
                    String tag = line.substring(1).trim();
                    tags.add(tag);
                }
            }
            // Use the file name as fallback if Locator Name is missing
            if (foundLocatorName == null || foundLocatorName.isEmpty()) {
                foundLocatorName = locatorName;
            }

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

            return new LocatorData(foundLocatorName, entityType, tags, parent);
        } catch (Exception e) {
            System.out.println("[AJLocatorData] Error parsing locator file: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    public static LocatorData getLocatorData(String exportNamespace, String locatorName) {
        Map<String, LocatorData> nsMap = locatorDataMap.get(exportNamespace);
        return nsMap == null ? null : nsMap.get(locatorName);
    }

    public static Set<String> getAllLocatorNames(String exportNamespace) {
        Map<String, LocatorData> nsMap = locatorDataMap.get(exportNamespace);
        return nsMap == null ? Collections.emptySet() : Collections.unmodifiableSet(nsMap.keySet());
    }
}