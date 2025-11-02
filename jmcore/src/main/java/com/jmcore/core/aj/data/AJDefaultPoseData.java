package com.jmcore.core.aj.data;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector2f;
import org.joml.Quaternionf;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AJDefaultPoseData
 *
 * Purpose:
 * - Loads default-pose files listed in aj_data/index/default_pose_index.txt.
 * - Each default-pose file contains a single set of transforms (no frame numbers).
 * - Stores per-namespace default pose transforms for bones, displays and locators.
 *
 * Notes:
 * - Default-pose data is intentionally separate from the animation frame system (AJFrameData).
 * - The format of a default-pose file is similar to an animation file but without leading frame indices:
 *     bone <name> <16-float-matrix>
 *     item_display <name> <16-float-matrix>
 *     block_display <name> <16-float-matrix>
 *     text_display <name> <16-float-matrix>
 *     locator <name> <x,y,z> <roty,rotx>
 *
 * Threading:
 * - Data maps are ConcurrentHashMap so they can be safely read from multiple threads after load.
 */
public class AJDefaultPoseData {
    // --- Storage maps: exportNamespace -> name -> pose data ---
    private static final Map<String, Map<String, DefaultBonePose>> bonePoseMap = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, DefaultDisplayPose>> itemDisplayPoseMap = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, DefaultDisplayPose>> blockDisplayPoseMap = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, DefaultDisplayPose>> textDisplayPoseMap = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, DefaultLocatorPose>> locatorPoseMap = new ConcurrentHashMap<>();

    // --- Data holder classes (explicit, do NOT use "frame" terminology) ---
    public static class DefaultBonePose {
        public final Vector3f translation;
        public final Vector3f scale;
        public final Quaternionf rotation;
        public DefaultBonePose(Vector3f translation, Vector3f scale, Quaternionf rotation) {
            this.translation = translation;
            this.scale = scale;
            this.rotation = rotation;
        }
    }

    public static class DefaultDisplayPose {
        public final Vector3f translation;
        public final Vector3f scale;
        public final Quaternionf rotation;
        public DefaultDisplayPose(Vector3f translation, Vector3f scale, Quaternionf rotation) {
            this.translation = translation;
            this.scale = scale;
            this.rotation = rotation;
        }
    }

    public static class DefaultLocatorPose {
        public final Vector3f position;
        public final Vector2f rotation; // roty, rotx
        public DefaultLocatorPose(Vector3f position, Vector2f rotation) {
            this.position = position;
            this.rotation = rotation;
        }
    }

    /**
     * Load all default-pose files referenced by aj_data/index/default_pose_index.txt.
     * This is intended to be called once at startup (AJFrameData.loadAllAnimations calls it).
     */
    public static void loadAllDefaultPoses() {
        System.out.println("[AJDefaultPoseData] Loading default-pose resources...");

        String indexPath = "aj_data/index/default_pose_index.txt";
        String resourceRoot = "aj_data/rig/";

        ClassLoader cl = AJDefaultPoseData.class.getClassLoader();
        try (InputStream indexStream = cl.getResourceAsStream(indexPath)) {
            if (indexStream == null) {
                System.out.println("[AJDefaultPoseData] No " + indexPath + " found; skipping default-pose load.");
                return;
            }
            BufferedReader indexReader = new BufferedReader(new InputStreamReader(indexStream));
            String line;
            int loaded = 0;
            while ((line = indexReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String resourcePath = resourceRoot + line;
                try (InputStream poseStream = cl.getResourceAsStream(resourcePath)) {
                    if (poseStream == null) {
                        System.out.println("[AJDefaultPoseData] Default-pose file not found: " + resourcePath);
                        continue;
                    }
                    // namespace is the first path component (e.g., "blueprint")
                    String[] parts = line.split("/");
                    if (parts.length < 2) {
                        System.out.println("[AJDefaultPoseData] Invalid default-pose path in index: " + line);
                        continue;
                    }
                    String exportNamespace = parts[0];
                    // Parse the file
                    loadDefaultPoseFile(exportNamespace, poseStream);
                    loaded++;
                } catch (Exception e) {
                    System.out.println("[AJDefaultPoseData] Error loading default-pose: " + resourcePath);
                    e.printStackTrace();
                }
            }
            System.out.println("[AJDefaultPoseData] Loaded " + loaded + " default-pose files.");
        } catch (Exception e) {
            System.out.println("[AJDefaultPoseData] Error reading default-pose index:");
            e.printStackTrace();
        }
    }

    /**
     * Load a single default-pose file stream for the given namespace.
     * File lines are expected to be in the "no-frame" format described above.
     */
    private static void loadDefaultPoseFile(String exportNamespace, InputStream is) {
        Map<String, DefaultBonePose> boneMap = bonePoseMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>());
        Map<String, DefaultDisplayPose> itemMap = itemDisplayPoseMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>());
        Map<String, DefaultDisplayPose> blockMap = blockDisplayPoseMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>());
        Map<String, DefaultDisplayPose> textMap = textDisplayPoseMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>());
        Map<String, DefaultLocatorPose> locMap = locatorPoseMap.computeIfAbsent(exportNamespace, k -> new ConcurrentHashMap<>());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] tokens = line.split(" ");
                if (tokens.length < 3) continue;

                String type = tokens[0];
                // For default-pose files token indexing is:
                // - bone / item_display / block_display / text_display: tokens[1] = name, tokens[2] = 16-float-matrix
                // - locator: tokens[1] = name, tokens[2] = pos (x,y,z), tokens[3] = rot (roty,rotx)
                if ("bone".equals(type) || "item_display".equals(type) || "block_display".equals(type) || "text_display".equals(type)) {
                    if (tokens.length < 3) continue;
                    String name = tokens[1];
                    String[] matrixTokens = tokens[2].split(",");
                    if (matrixTokens.length != 16) {
                        System.out.println("[AJDefaultPoseData] Invalid matrix in default-pose for " + type + " " + name + ": " + tokens[2]);
                        continue;
                    }
                    float[] flat = new float[16];
                    for (int i = 0; i < 16; i++) flat[i] = Float.parseFloat(matrixTokens[i].trim());
                    FloatBuffer buffer = ByteBuffer
                            .allocateDirect(flat.length * Float.BYTES)
                            .order(ByteOrder.nativeOrder())
                            .asFloatBuffer()
                            .put(flat);
                    buffer.flip();

                    Matrix4f originalMatrix = new Matrix4f();
                    originalMatrix.setTransposed(buffer);

                    Vector3f translation = new Vector3f();
                    Quaternionf rotation = new Quaternionf();
                    Vector3f scale = new Vector3f();

                    originalMatrix.getTranslation(translation);
                    originalMatrix.getUnnormalizedRotation(rotation);
                    rotation.normalize();
                    originalMatrix.getScale(scale);

                    if ("bone".equals(type)) {
                        boneMap.put(name, new DefaultBonePose(translation, scale, rotation));
                    } else if ("item_display".equals(type)) {
                        itemMap.put(name, new DefaultDisplayPose(translation, scale, rotation));
                    } else if ("block_display".equals(type)) {
                        blockMap.put(name, new DefaultDisplayPose(translation, scale, rotation));
                    } else if ("text_display".equals(type)) {
                        textMap.put(name, new DefaultDisplayPose(translation, scale, rotation));
                    }
                } else if ("locator".equals(type)) {
                    if (tokens.length < 4) continue;
                    String name = tokens[1];
                    String[] posTokens = tokens[2].split(",");
                    String[] rotTokens = tokens[3].split(",");
                    if (posTokens.length != 3 || rotTokens.length != 2) {
                        System.out.println("[AJDefaultPoseData] Invalid locator data for locator " + name + ": " + line);
                        continue;
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
                    locMap.put(name, new DefaultLocatorPose(position, rotation));
                } else {
                    // unknown token type -> ignore
                }
            }
        } catch (Exception e) {
            System.out.println("[AJDefaultPoseData] Error parsing default-pose file for namespace " + exportNamespace);
            e.printStackTrace();
        }
    }

    // --- Lookup helpers used by other systems (read-only after load) ---

    public static DefaultBonePose getBonePose(String exportNamespace, String boneName) {
        return Optional.ofNullable(bonePoseMap.get(exportNamespace)).map(m -> m.get(boneName)).orElse(null);
    }

    public static DefaultDisplayPose getItemDisplayPose(String exportNamespace, String name) {
        return Optional.ofNullable(itemDisplayPoseMap.get(exportNamespace)).map(m -> m.get(name)).orElse(null);
    }

    public static DefaultDisplayPose getBlockDisplayPose(String exportNamespace, String name) {
        return Optional.ofNullable(blockDisplayPoseMap.get(exportNamespace)).map(m -> m.get(name)).orElse(null);
    }

    public static DefaultDisplayPose getTextDisplayPose(String exportNamespace, String name) {
        return Optional.ofNullable(textDisplayPoseMap.get(exportNamespace)).map(m -> m.get(name)).orElse(null);
    }

    public static DefaultLocatorPose getLocatorPose(String exportNamespace, String locatorName) {
        return Optional.ofNullable(locatorPoseMap.get(exportNamespace)).map(m -> m.get(locatorName)).orElse(null);
    }

    public static Set<String> getBoneNames(String exportNamespace) {
        Map<String, DefaultBonePose> m = bonePoseMap.get(exportNamespace);
        return m == null ? Collections.emptySet() : Collections.unmodifiableSet(m.keySet());
    }

    public static Set<String> getLocatorNames(String exportNamespace) {
        Map<String, DefaultLocatorPose> m = locatorPoseMap.get(exportNamespace);
        return m == null ? Collections.emptySet() : Collections.unmodifiableSet(m.keySet());
    }

    // Additional getters for item/block/text display name sets
    public static Set<String> getItemDisplayNames(String exportNamespace) {
        Map<String, DefaultDisplayPose> m = itemDisplayPoseMap.get(exportNamespace);
        return m == null ? Collections.emptySet() : Collections.unmodifiableSet(m.keySet());
    }

    public static Set<String> getBlockDisplayNames(String exportNamespace) {
        Map<String, DefaultDisplayPose> m = blockDisplayPoseMap.get(exportNamespace);
        return m == null ? Collections.emptySet() : Collections.unmodifiableSet(m.keySet());
    }

    public static Set<String> getTextDisplayNames(String exportNamespace) {
        Map<String, DefaultDisplayPose> m = textDisplayPoseMap.get(exportNamespace);
        return m == null ? Collections.emptySet() : Collections.unmodifiableSet(m.keySet());
    }
}