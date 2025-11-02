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
    // exportNamespace -> textDisplayName -> TextDisplayData
    private static final Map<String, Map<String, TextDisplayData>> textDisplayDataMap = new ConcurrentHashMap<>();

    public static class TextDisplayData {
        public final String textDisplayName;
        public final String text;
        public final int boundingBoxHeight;
        public final int boundingBoxWidth;
        public final Set<String> tags;

        public TextDisplayData(String textDisplayName, String text, int boundingBoxHeight, int boundingBoxWidth, Set<String> tags) {
            this.textDisplayName = textDisplayName;
            this.text = text;
            this.boundingBoxHeight = boundingBoxHeight;
            this.boundingBoxWidth = boundingBoxWidth;
            this.tags = Collections.unmodifiableSet(tags);
        }
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
                try (InputStream textStream = AJTextDisplayData.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (textStream == null) {
                        System.out.println("[AJTextDisplayData] Text display file not found: " + resourcePath);
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(textStream));
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
                    if (tName == null) {
                        System.out.println("[AJTextDisplayData] Missing required field: Text Display Name in " + resourcePath);
                        continue;
                    }
                    TextDisplayData data = new TextDisplayData(tName, text, bboxHeight, bboxWidth, tags);
                    textDisplayDataMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>()).put(textDisplayName, data);
                    System.out.println("[AJTextDisplayData] Loaded text display: " + tName + " in namespace: " + exportNamespace);
                }
            }
        } catch (Exception e) {
            System.out.println("[AJTextDisplayData] Error loading text display data:");
            e.printStackTrace();
        }
    }

    /**
     * Returns the TextDisplayData for the given export namespace and text display name, or null if not loaded.
     */
    public static TextDisplayData getTextDisplayData(String exportNamespace, String textDisplayName) {
        Map<String, TextDisplayData> nsMap = textDisplayDataMap.get(exportNamespace);
        return nsMap != null ? nsMap.get(textDisplayName) : null;
    }

    /**
     * Returns a set of all text display names for the given export namespace.
     */
    public static Set<String> getAllTextDisplayNames(String exportNamespace) {
        Map<String, TextDisplayData> nsMap = textDisplayDataMap.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}