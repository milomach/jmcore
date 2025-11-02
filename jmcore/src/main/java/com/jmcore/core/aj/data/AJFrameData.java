package com.jmcore.core.aj.data;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector2f;
import org.joml.Quaternionf;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and stores all AJ animation frame data for fast lookup.
 * Data is loaded from resources in aj_data/rig/<exportNamespace>/animations/<animationName>.txt
 * Uses aj_data/index/animation_index.txt to list all animation files.
 * 
 * Supports bones, item_displays, block_displays, text_displays, and locators.
 */
public class AJFrameData {
    // --- Frame data maps for each type ---
    private static final Map<String, Map<String, Map<Integer, Map<String, BoneFrameData>>>> boneFrameData = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Map<Integer, Map<String, ItemDisplayFrameData>>>> itemDisplayFrameData = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Map<Integer, Map<String, BlockDisplayFrameData>>>> blockDisplayFrameData = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Map<Integer, Map<String, TextDisplayFrameData>>>> textDisplayFrameData = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Map<Integer, Map<String, LocatorFrameData>>>> locatorFrameData = new ConcurrentHashMap<>();

    // --- Frame data classes for each type ---
    public static class BoneFrameData {
        public final Vector3f translation;
        public final Vector3f scale;
        public final Quaternionf rotation;

        public BoneFrameData(Vector3f translation, Vector3f scale, Quaternionf rotation) {
            this.translation = translation;
            this.scale = scale;
            this.rotation = rotation;
        }
    }
    public static class ItemDisplayFrameData {
        public final Vector3f translation;
        public final Vector3f scale;
        public final Quaternionf rotation;

        public ItemDisplayFrameData(Vector3f translation, Vector3f scale, Quaternionf rotation) {
            this.translation = translation;
            this.scale = scale;
            this.rotation = rotation;
        }
    }
    public static class BlockDisplayFrameData {
        public final Vector3f translation;
        public final Vector3f scale;
        public final Quaternionf rotation;

        public BlockDisplayFrameData(Vector3f translation, Vector3f scale, Quaternionf rotation) {
            this.translation = translation;
            this.scale = scale;
            this.rotation = rotation;
        }
    }
    public static class TextDisplayFrameData {
        public final Vector3f translation;
        public final Vector3f scale;
        public final Quaternionf rotation;

        public TextDisplayFrameData(Vector3f translation, Vector3f scale, Quaternionf rotation) {
            this.translation = translation;
            this.scale = scale;
            this.rotation = rotation;
        }
    }
    public static class LocatorFrameData {
        public final Vector3f position;
        public final Vector2f rotation; // roty, rotx

        public LocatorFrameData(Vector3f position, Vector2f rotation) {
            this.position = position;
            this.rotation = rotation;
        }
    }

    /**
     * Loads all animation files listed in aj_data/index/animation_index.txt using classloader resource access.
     * Prints debug info for every step.
     */
    public static void loadAllAnimations() {
        System.out.println("[AJFrameData] Starting animation resource scan...");

        String indexPath = "aj_data/index/animation_index.txt";
        String animationResourceRoot = "aj_data/rig/";

        ClassLoader cl = AJFrameData.class.getClassLoader();
        try (InputStream indexStream = cl.getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJFrameData] No " + indexPath + " found!");
                return;
            }
            BufferedReader indexReader = new BufferedReader(new InputStreamReader(indexStream));
            String line;
            int fileCount = 0;
            while ((line = indexReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String resourcePath = animationResourceRoot + line;
                try (InputStream animStream = cl.getResourceAsStream(resourcePath)) {
                    if (animStream == null) {
                        System.out.println("[AJFrameData] Animation file not found: " + resourcePath);
                        continue;
                    }
                    String[] parts = line.split("/");
                    if (parts.length < 2) {
                        System.out.println("[AJFrameData] Invalid animation file path in index: " + line);
                        continue;
                    }
                    String exportNamespace = parts[0];
                    String animationName = parts[parts.length - 1].replace(".txt", "");
                    System.out.println("[AJFrameData] Found animation file: " + resourcePath);
                    loadAnimationFile(exportNamespace, animationName, animStream, null);
                    fileCount++;
                }
            }
            if (fileCount == 0) {
                System.out.println("[AJFrameData] No animation files loaded from index!");
            } else {
                System.out.println("[AJFrameData] Loaded " + fileCount + " animation files.");
            }
        } catch (Exception e) {
            System.out.println("[AJFrameData] Error loading animation resources:");
            e.printStackTrace();
        }
    }

    /**
     * Loads a single animation file and processes each line independently of its order.
     * Converts row-major matrix data to column-major using setTransposed for all display types.
     */
    private static void loadAnimationFile(String exportNamespace, String animationName, InputStream is, StringBuilder debug) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens.length < 3) continue; // Skip invalid lines
                int frame = Integer.parseInt(tokens[0]);
                String type = tokens[1];

                switch (type) {
                    case "bone":
                    case "item_display":
                    case "block_display":
                    case "text_display":
                        processMatrixEntity(exportNamespace, animationName, frame, type, tokens);
                        break;
                    case "locator":
                        processLocatorEntity(exportNamespace, animationName, frame, tokens);
                        break;
                    default:
                        System.out.println("[AJFrameData] Unknown entity type: " + type + " in line: " + line);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes entities with matrix data (bones, item displays, block displays, text displays).
     */
    private static void processMatrixEntity(String exportNamespace, String animationName, int frame, String type, String[] tokens) {
        // Debug: Log the tokens being processed
        System.out.println("[AJFrameData] Processing matrix entity line: " + String.join(" ", tokens));

        // Ensure the line has at least 4 tokens (frame, type, name, matrix)
        if (tokens.length < 4) {
            System.out.println("[AJFrameData] Invalid matrix entity line (not enough tokens): " + String.join(" ", tokens));
            return;
        }

        String name = tokens[2];
        String[] matrixTokens = tokens[3].split(",");

        // Debug: Log the matrix tokens and their count
        System.out.println("[AJFrameData] Matrix tokens for " + type + " " + name + ": " + Arrays.toString(matrixTokens) + " (count: " + matrixTokens.length + ")");

        // Ensure the matrix has exactly 16 elements
        if (matrixTokens.length != 16) {
            System.out.println("[AJFrameData] Invalid matrix for frame " + frame + " " + type + " " + name + ": " + tokens[3]);
            return;
        }

        // Parse the matrix elements
        float[] flat = new float[16];
        try {
            for (int i = 0; i < 16; i++) {
                flat[i] = Float.parseFloat(matrixTokens[i].trim());
            }
        } catch (NumberFormatException e) {
            System.out.println("[AJFrameData] Invalid number format in matrix for frame " + frame + " " + type + " " + name + ": " + tokens[3]);
            return;
        }

        // Convert the flat array to a FloatBuffer
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(flat.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(flat);
        buffer.flip();

        // Create a Matrix4f and extract translation, rotation, and scale
        Matrix4f originalMatrix = new Matrix4f();
        originalMatrix.setTransposed(buffer);

        Vector3f translation = new Vector3f();
        Quaternionf rotation = new Quaternionf();
        Vector3f scale = new Vector3f();

        originalMatrix.getTranslation(translation);
        originalMatrix.getUnnormalizedRotation(rotation);
        rotation.normalize();
        originalMatrix.getScale(scale);

        // Store the parsed data in the appropriate map
        switch (type) {
            case "bone":
                boneFrameData
                    .computeIfAbsent(exportNamespace, k -> new HashMap<>())
                    .computeIfAbsent(animationName, k -> new HashMap<>())
                    .computeIfAbsent(frame, k -> new HashMap<>())
                    .put(name, new BoneFrameData(translation, scale, rotation));
                break;
            case "item_display":
                itemDisplayFrameData
                    .computeIfAbsent(exportNamespace, k -> new HashMap<>())
                    .computeIfAbsent(animationName, k -> new HashMap<>())
                    .computeIfAbsent(frame, k -> new HashMap<>())
                    .put(name, new ItemDisplayFrameData(translation, scale, rotation));
                break;
            case "block_display":
                blockDisplayFrameData
                    .computeIfAbsent(exportNamespace, k -> new HashMap<>())
                    .computeIfAbsent(animationName, k -> new HashMap<>())
                    .computeIfAbsent(frame, k -> new HashMap<>())
                    .put(name, new BlockDisplayFrameData(translation, scale, rotation));
                break;
            case "text_display":
                textDisplayFrameData
                    .computeIfAbsent(exportNamespace, k -> new HashMap<>())
                    .computeIfAbsent(animationName, k -> new HashMap<>())
                    .computeIfAbsent(frame, k -> new HashMap<>())
                    .put(name, new TextDisplayFrameData(translation, scale, rotation));
                break;
            default:
                System.out.println("[AJFrameData] Unknown entity type: " + type + " in line: " + String.join(" ", tokens));
                break;
        }
    }

    /**
     * Processes locator entities.
     */
    private static void processLocatorEntity(String exportNamespace, String animationName, int frame, String[] tokens) {
        if (tokens.length < 5) {
            System.out.println("[AJFrameData] Invalid locator line: " + String.join(" ", tokens));
            return;
        }
        String locator = tokens[2];
        String[] posTokens = tokens[3].split(",");
        String[] rotTokens = tokens[4].split(",");
        if (posTokens.length != 3 || rotTokens.length != 2) {
            System.out.println("[AJFrameData] Invalid locator data for frame " + frame + " locator " + locator + ": " + String.join(" ", tokens));
            return;
        }
        Vector3f position = new Vector3f(
            Float.parseFloat(posTokens[0].trim()),
            Float.parseFloat(posTokens[1].trim()),
            Float.parseFloat(posTokens[2].trim())
        );
        Vector2f rotation = new Vector2f(
            Float.parseFloat(rotTokens[0].trim()),
            Float.parseFloat(rotTokens[1].trim())
        );
        locatorFrameData
            .computeIfAbsent(exportNamespace, k -> new HashMap<>())
            .computeIfAbsent(animationName, k -> new HashMap<>())
            .computeIfAbsent(frame, k -> new HashMap<>())
            .put(locator, new LocatorFrameData(position, rotation));
    }

    // --- Frame data accessors for each type ---
    public static BoneFrameData getBoneFrameData(String exportNamespace, String animationName, int frame, String bone) {
        return Optional.ofNullable(boneFrameData.get(exportNamespace))
            .map(m -> m.get(animationName))
            .map(m -> m.get(frame))
            .map(m -> m.get(bone))
            .orElse(null);
    }
    public static ItemDisplayFrameData getItemDisplayFrameData(String exportNamespace, String animationName, int frame, String itemDisplay) {
        return Optional.ofNullable(itemDisplayFrameData.get(exportNamespace))
            .map(m -> m.get(animationName))
            .map(m -> m.get(frame))
            .map(m -> m.get(itemDisplay))
            .orElse(null);
    }
    public static BlockDisplayFrameData getBlockDisplayFrameData(String exportNamespace, String animationName, int frame, String blockDisplay) {
        return Optional.ofNullable(blockDisplayFrameData.get(exportNamespace))
            .map(m -> m.get(animationName))
            .map(m -> m.get(frame))
            .map(m -> m.get(blockDisplay))
            .orElse(null);
    }
    public static TextDisplayFrameData getTextDisplayFrameData(String exportNamespace, String animationName, int frame, String textDisplay) {
        return Optional.ofNullable(textDisplayFrameData.get(exportNamespace))
            .map(m -> m.get(animationName))
            .map(m -> m.get(frame))
            .map(m -> m.get(textDisplay))
            .orElse(null);
    }
    public static LocatorFrameData getLocatorFrameData(String exportNamespace, String animationName, int frame, String locator) {
        return Optional.ofNullable(locatorFrameData.get(exportNamespace))
            .map(m -> m.get(animationName))
            .map(m -> m.get(frame))
            .map(m -> m.get(locator))
            .orElse(null);
    }

    // --- Per-frame entity name sets for each type ---
    public static Set<String> getBonesForFrame(String exportNamespace, String animationName, int frame) {
        Map<String, Map<Integer, Map<String, BoneFrameData>>> nsMap = boneFrameData.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        Map<Integer, Map<String, BoneFrameData>> animMap = nsMap.get(animationName);
        if (animMap == null) return Collections.emptySet();
        Map<String, BoneFrameData> frameMap = animMap.get(frame);
        if (frameMap == null) return Collections.emptySet();
        return frameMap.keySet();
    }
    public static Set<String> getItemDisplaysForFrame(String exportNamespace, String animationName, int frame) {
        Map<String, Map<Integer, Map<String, ItemDisplayFrameData>>> nsMap = itemDisplayFrameData.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        Map<Integer, Map<String, ItemDisplayFrameData>> animMap = nsMap.get(animationName);
        if (animMap == null) return Collections.emptySet();
        Map<String, ItemDisplayFrameData> frameMap = animMap.get(frame);
        if (frameMap == null) return Collections.emptySet();
        return frameMap.keySet();
    }
    public static Set<String> getBlockDisplaysForFrame(String exportNamespace, String animationName, int frame) {
        Map<String, Map<Integer, Map<String, BlockDisplayFrameData>>> nsMap = blockDisplayFrameData.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        Map<Integer, Map<String, BlockDisplayFrameData>> animMap = nsMap.get(animationName);
        if (animMap == null) return Collections.emptySet();
        Map<String, BlockDisplayFrameData> frameMap = animMap.get(frame);
        if (frameMap == null) return Collections.emptySet();
        return frameMap.keySet();
    }
    public static Set<String> getTextDisplaysForFrame(String exportNamespace, String animationName, int frame) {
        Map<String, Map<Integer, Map<String, TextDisplayFrameData>>> nsMap = textDisplayFrameData.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        Map<Integer, Map<String, TextDisplayFrameData>> animMap = nsMap.get(animationName);
        if (animMap == null) return Collections.emptySet();
        Map<String, TextDisplayFrameData> frameMap = animMap.get(frame);
        if (frameMap == null) return Collections.emptySet();
        return frameMap.keySet();
    }
    public static Set<String> getLocatorsForFrame(String exportNamespace, String animationName, int frame) {
        Map<String, Map<Integer, Map<String, LocatorFrameData>>> nsMap = locatorFrameData.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        Map<Integer, Map<String, LocatorFrameData>> animMap = nsMap.get(animationName);
        if (animMap == null) return Collections.emptySet();
        Map<String, LocatorFrameData> frameMap = animMap.get(frame);
        if (frameMap == null) return Collections.emptySet();
        return frameMap.keySet();
    }

    // --- Frame count and animation name access ---
    /**
     * Returns the frame count for an animation, considering all entity types.
     * The count is the highest frame index + 1 among all bones, item displays, block displays, text displays, and locators.
     */
    public static int getFrameCount(String exportNamespace, String animationName) {
        int maxFrame = -1;

        // Bones
        Map<String, Map<Integer, Map<String, BoneFrameData>>> boneNsMap = boneFrameData.get(exportNamespace);
        if (boneNsMap != null) {
            Map<Integer, Map<String, BoneFrameData>> animMap = boneNsMap.get(animationName);
            if (animMap != null) {
                for (Integer frame : animMap.keySet()) {
                    if (frame > maxFrame) maxFrame = frame;
                }
            }
        }

        // Item Displays
        Map<String, Map<Integer, Map<String, ItemDisplayFrameData>>> itemDisplayNsMap = itemDisplayFrameData.get(exportNamespace);
        if (itemDisplayNsMap != null) {
            Map<Integer, Map<String, ItemDisplayFrameData>> animMap = itemDisplayNsMap.get(animationName);
            if (animMap != null) {
                for (Integer frame : animMap.keySet()) {
                    if (frame > maxFrame) maxFrame = frame;
                }
            }
        }

        // Block Displays
        Map<String, Map<Integer, Map<String, BlockDisplayFrameData>>> blockDisplayNsMap = blockDisplayFrameData.get(exportNamespace);
        if (blockDisplayNsMap != null) {
            Map<Integer, Map<String, BlockDisplayFrameData>> animMap = blockDisplayNsMap.get(animationName);
            if (animMap != null) {
                for (Integer frame : animMap.keySet()) {
                    if (frame > maxFrame) maxFrame = frame;
                }
            }
        }

        // Text Displays
        Map<String, Map<Integer, Map<String, TextDisplayFrameData>>> textDisplayNsMap = textDisplayFrameData.get(exportNamespace);
        if (textDisplayNsMap != null) {
            Map<Integer, Map<String, TextDisplayFrameData>> animMap = textDisplayNsMap.get(animationName);
            if (animMap != null) {
                for (Integer frame : animMap.keySet()) {
                    if (frame > maxFrame) maxFrame = frame;
                }
            }
        }

        // Locators
        Map<String, Map<Integer, Map<String, LocatorFrameData>>> locatorNsMap = locatorFrameData.get(exportNamespace);
        if (locatorNsMap != null) {
            Map<Integer, Map<String, LocatorFrameData>> animMap = locatorNsMap.get(animationName);
            if (animMap != null) {
                for (Integer frame : animMap.keySet()) {
                    if (frame > maxFrame) maxFrame = frame;
                }
            }
        }

        return maxFrame + 1;
    }
    public static Set<String> getAllAnimationNames(String exportNamespace) {
        Map<String, Map<Integer, Map<String, BoneFrameData>>> nsMap = boneFrameData.get(exportNamespace);
        if (nsMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(nsMap.keySet());
    }
}