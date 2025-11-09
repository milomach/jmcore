package com.jmcore.core.aj.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AnimatedOffsetRegistry
 *
 * Loads and stores valid animated offset animation/exportNamespace pairs from aj_data/index/animated_offset_index.txt.
 * Each line is of the form: <exportNamespace>/animations/<animationName>.txt
 * Used to validate animated offset assignments for offset sources.
 *
 * This class follows the conventions of other AJ data files.
 */
public class AnimatedOffsetRegistry {
    // exportNamespace -> set of animation names
    private static final Map<String, Set<String>> animatedOffsets = new ConcurrentHashMap<>();

    private AnimatedOffsetRegistry() {}

    /**
     * Loads all animated offset data from aj_data/index/animated_offset_index.txt.
     * Call this at plugin startup or reload.
     */
    public static void loadAllAnimatedOffsets() {
        animatedOffsets.clear();
        String indexPath = "aj_data/index/animated_offset_index.txt";
        try (InputStream indexStream = AnimatedOffsetRegistry.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AnimatedOffsetRegistry] No " + indexPath + " found!");
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(indexStream));
            String line;
            int loadedCount = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                // Format: <exportNamespace>/animations/<animationName>.txt
                String[] parts = line.split("/");
                if (parts.length < 3) continue;
                String exportNamespace = parts[0];
                String animFile = parts[parts.length - 1];
                if (!animFile.endsWith(".txt")) continue;
                String animationName = animFile.substring(0, animFile.length() - 4);
                animatedOffsets.computeIfAbsent(exportNamespace, k -> new HashSet<>()).add(animationName);
                loadedCount++;
            }
            System.out.println("[AnimatedOffsetRegistry] Loaded " + loadedCount + " animated offset entries.");
        } catch (Exception e) {
            System.out.println("[AnimatedOffsetRegistry] Error loading animated offset index:");
            e.printStackTrace();
        }
    }

    /**
     * Returns true if the given exportNamespace/animationName is a valid animated offset.
     */
    public static boolean isValidAnimatedOffset(String exportNamespace, String animationName) {
        Set<String> anims = animatedOffsets.get(exportNamespace);
        return anims != null && anims.contains(animationName);
    }

    /**
     * Returns all valid animation names for the given exportNamespace.
     */
    public static Set<String> getAllAnimationNames(String exportNamespace) {
        Set<String> anims = animatedOffsets.get(exportNamespace);
        return anims == null ? Collections.emptySet() : Collections.unmodifiableSet(anims);
    }

    /**
     * Returns all exportNamespaces that have animated offsets.
     */
    public static Set<String> getAllExportNamespaces() {
        return Collections.unmodifiableSet(animatedOffsets.keySet());
    }
}