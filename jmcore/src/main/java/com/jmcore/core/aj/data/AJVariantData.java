package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores AJ variant data for each export namespace and variant name.
 * Variant data is loaded from aj_data/rig/<export_namespace>/variants/<variant_name>.txt.
 * Uses aj_data/index/variant_index.txt to find all variant files.
 */
public class AJVariantData {
    // exportNamespace -> variantName -> VariantData
    private static final Map<String, Map<String, VariantData>> variantDataMap = new ConcurrentHashMap<>();

    public static class VariantData {
        public final String variantName;
        public final List<String> bones;

        public VariantData(String variantName, List<String> bones) {
            this.variantName = variantName;
            this.bones = Collections.unmodifiableList(bones);
        }
    }

    /**
     * Loads all variant data from aj_data/index/variant_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllVariantData() {
        variantDataMap.clear();
        String indexPath = "aj_data/index/variant_index.txt";
        String resourceRoot = "aj_data/rig/";
        try (InputStream indexStream = AJVariantData.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJVariantData] No " + indexPath + " found!");
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
                String variantName = parts[2].replace(".txt", "");
                try (InputStream variantStream = AJVariantData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (variantStream == null) {
                        System.out.println("[AJVariantData] Variant file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(variantStream));
                    String vName = null;
                    List<String> bones = new ArrayList<>();
                    boolean inBones = false;
                    String l;
                    while ((l = reader.readLine()) != null) {
                        l = l.trim();
                        if (l.startsWith("Variant:")) vName = l.substring("Variant:".length()).trim();
                        else if (l.equalsIgnoreCase("Bones:")) inBones = true;
                        else if (inBones && l.startsWith("-")) bones.add(l.substring(1).trim());
                    }
                    if (vName == null) vName = variantName;
                    VariantData data = new VariantData(vName, bones);
                    variantDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(variantName, data);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJVariantData] Error loading variant data:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the VariantData for the given export namespace and variant name, or null if not loaded.
     */
    public static VariantData getVariantData(String exportNamespace, String variantName) {
        Map<String, VariantData> nsMap = variantDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(variantName) : null;
    }

    /**
     * Returns a set of all variant names for the given export namespace.
     */
    public static Set<String> getAllVariantNames(String exportNamespace) {
        Map<String, VariantData> nsMap = variantDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}