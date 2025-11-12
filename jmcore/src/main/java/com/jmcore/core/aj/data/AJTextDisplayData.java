package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores AJ text display data for each export namespace and text display name.
 * Data is loaded from aj_data/rig/<export_namespace>/text_displays/<text_display_name>.txt.
 * Uses aj_data/index/text_display_index.txt to find all text display files.
 */
public class AJTextDisplayData {
    public static final String PARENT_MODEL_ROOT = "__MODEL_ROOT__";

    // exportNamespace -> textDisplayName -> TextDisplayData
    private static final Map<String, Map<String, TextDisplayData>> textDisplayDataMap = new ConcurrentHashMap<>();

    public static class TextDisplayData {
        public final String textDisplayName;
        public final String text;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;
        /**
         * The parent bone name, or PARENT_MODEL_ROOT if at the root, or null if unknown.
         */
        public final String parent;

        public TextDisplayData(String textDisplayName, String text, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags, String parent) {
            this.textDisplayName = textDisplayName;
            this.text = text;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
            this.parent = parent;
        }

        public String getParent() { return parent; }
    }

    /**
     * Loads all text display data from aj_data/index/text_display_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllTextDisplayData() {
        textDisplayDataMap.clear();
        String indexPath = "aj_data/index/text_display_index.txt";
        String resourceRoot = "aj_data/rig/";
        try (InputStream indexStream = AJTextDisplayData.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJTextDisplayData] No " + indexPath + " found!");
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
                String textDisplayName = parts[2].replace(".txt", "");
                try (InputStream is = AJTextDisplayData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.out.println("[AJTextDisplayData] Text display file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String tName = null, text = null;
                    int bboxHeight = 0, bboxWidth = 0;
                    Set<String> tags = new HashSet<>();
                    boolean inTags = false;
                    String l;
                    while ((l = reader.readLine()) != null) {
                        l = l.trim();
                        if (l.startsWith("Text Display Name:")) tName = l.substring("Text Display Name:".length()).trim();
                        else if (l.startsWith("Text:")) text = l.substring("Text:".length()).trim();
                        else if (l.startsWith("Bounding Box Height:")) {
                            String value = l.substring("Bounding Box Height:".length()).trim();
                            bboxHeight = value.isEmpty() ? 0 : Integer.parseInt(value);
                        } else if (l.startsWith("Bounding Box Width:")) {
                            String value = l.substring("Bounding Box Width:".length()).trim();
                            bboxWidth = value.isEmpty() ? 0 : Integer.parseInt(value);
                        } else if (l.equalsIgnoreCase("Tags:")) inTags = true;
                        else if (inTags && l.startsWith("-")) tags.add(l.substring(1).trim());
                    }
                    if (tName == null) tName = textDisplayName;

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

                    TextDisplayData data = new TextDisplayData(tName, text, bboxHeight, bboxWidth, tags, parent);
                    textDisplayDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(textDisplayName, data);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJTextDisplayData] Error loading text display data:");
            e.printStackTrace();
        }
    }

    public static TextDisplayData getTextDisplayData(String exportNamespace, String textDisplayName) {
        Map<String, TextDisplayData> nsMap = textDisplayDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(textDisplayName) : null;
    }

    public static Set<String> getAllTextDisplayNames(String exportNamespace) {
        Map<String, TextDisplayData> nsMap = textDisplayDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}