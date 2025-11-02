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
 */
public class AJLocatorData {
    // exportNamespace -> locatorName -> LocatorData
    private static final Map<String, Map<String, LocatorData>> locatorDataMap = new ConcurrentHashMap<>();

    public static class LocatorData {
        public final String locatorName;

        public LocatorData(String locatorName) {
            this.locatorName = locatorName;
        }
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
                String locatorName = parts[2].replace(".txt", "");
                try (InputStream locatorStream = AJLocatorData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (locatorStream == null) {
                        System.out.println("[AJLocatorData] Locator file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(locatorStream));
                    String lName = null;
                    String l;
                    while ((l = reader.readLine()) != null) {
                        l = l.trim();
                        if (l.startsWith("Locator Name:")) {
                            lName = l.substring("Locator Name:".length()).trim();
                        }
                    }
                    if (lName == null) lName = locatorName;
                    LocatorData data = new LocatorData(lName);
                    locatorDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(locatorName, data);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJLocatorData] Error loading locator data:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the LocatorData for the given export namespace and locator name, or null if not loaded.
     */
    public static LocatorData getLocatorData(String exportNamespace, String locatorName) {
        Map<String, LocatorData> nsMap = locatorDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(locatorName) : null;
    }

    /**
     * Returns a set of all locator names for the given export namespace.
     */
    public static Set<String> getAllLocatorNames(String exportNamespace) {
        Map<String, LocatorData> nsMap = locatorDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}