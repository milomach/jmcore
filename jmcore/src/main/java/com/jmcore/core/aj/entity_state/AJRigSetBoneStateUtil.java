package com.jmcore.core.aj.entity_state;

import java.util.HashSet;
import java.util.Set;

import com.jmcore.core.aj.data.AJVariantData;
import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;

public class AJRigSetBoneStateUtil {

    // --- Pose Interpolation ---
    public static void setPoseInterpDelay(AJRigInstance rig, String boneName, int value) {
        setPoseInterpDelay(rig, Set.of(boneName), value);
    }
    public static void setPoseInterpDelay(AJRigInstance rig, Set<String> boneNames, int value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.poseInterpDelay = value;
            }
        }
    }

    public static void setPoseInterpDuration(AJRigInstance rig, String boneName, int value) {
        setPoseInterpDuration(rig, Set.of(boneName), value);
    }
    public static void setPoseInterpDuration(AJRigInstance rig, Set<String> boneNames, int value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.poseInterpDuration = value;
            }
        }
    }

    public static void setPoseTeleportDuration(AJRigInstance rig, String boneName, int value) {
        setPoseTeleportDuration(rig, Set.of(boneName), value);
    }
    public static void setPoseTeleportDuration(AJRigInstance rig, Set<String> boneNames, int value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.poseTeleportDuration = value;
            }
        }
    }

    // --- Bone state ---
    public static void setEnchanted(AJRigInstance rig, String boneName, boolean value) {
        setEnchanted(rig, Set.of(boneName), value);
    }
    public static void setEnchanted(AJRigInstance rig, Set<String> boneNames, boolean value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.enchanted = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "enchanted");
            }
        }
    }

    public static void setBillboardMode(AJRigInstance rig, String boneName, String value) {
        setBillboardMode(rig, Set.of(boneName), value);
    }
    public static void setBillboardMode(AJRigInstance rig, Set<String> boneNames, String value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.billboardMode = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "billboard_mode");
            }
        }
    }

    public static void setBrightnessMode(AJRigInstance rig, String boneName, String value) {
        setBrightnessMode(rig, Set.of(boneName), value);
    }
    public static void setBrightnessMode(AJRigInstance rig, Set<String> boneNames, String value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.brightnessMode = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setBlockBrightness(AJRigInstance rig, String boneName, int value) {
        setBlockBrightness(rig, Set.of(boneName), value);
    }
    public static void setBlockBrightness(AJRigInstance rig, Set<String> boneNames, int value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.blockBrightness = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setSkyBrightness(AJRigInstance rig, String boneName, int value) {
        setSkyBrightness(rig, Set.of(boneName), value);
    }
    public static void setSkyBrightness(AJRigInstance rig, Set<String> boneNames, int value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.skyBrightness = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setDisplayWidth(AJRigInstance rig, String boneName, float value) {
        setDisplayWidth(rig, Set.of(boneName), value);
    }
    public static void setDisplayWidth(AJRigInstance rig, Set<String> boneNames, float value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.displayWidth = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "display_width");
            }
        }
    }

    public static void setDisplayHeight(AJRigInstance rig, String boneName, float value) {
        setDisplayHeight(rig, Set.of(boneName), value);
    }
    public static void setDisplayHeight(AJRigInstance rig, Set<String> boneNames, float value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.displayHeight = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "display_height");
            }
        }
    }

    public static void setGlowMode(AJRigInstance rig, String boneName, String value) {
        setGlowMode(rig, Set.of(boneName), value);
    }
    public static void setGlowMode(AJRigInstance rig, Set<String> boneNames, String value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.glowMode = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "glow");
            }
        }
    }

    public static void setGlowColor(AJRigInstance rig, String boneName, int value) {
        setGlowColor(rig, Set.of(boneName), value);
    }
    public static void setGlowColor(AJRigInstance rig, Set<String> boneNames, int value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.glowColor = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "glow");
            }
        }
    }

    public static void setShadowRadius(AJRigInstance rig, String boneName, float value) {
        setShadowRadius(rig, Set.of(boneName), value);
    }
    public static void setShadowRadius(AJRigInstance rig, Set<String> boneNames, float value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.shadowRadius = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "shadow_radius");
            }
        }
    }

    public static void setShadowStrength(AJRigInstance rig, String boneName, float value) {
        setShadowStrength(rig, Set.of(boneName), value);
    }
    public static void setShadowStrength(AJRigInstance rig, Set<String> boneNames, float value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.shadowStrength = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "shadow_strength");
            }
        }
    }

    public static void setViewRange(AJRigInstance rig, String boneName, float value) {
        setViewRange(rig, Set.of(boneName), value);
    }
    public static void setViewRange(AJRigInstance rig, Set<String> boneNames, float value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.viewRange = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "view_range");
            }
        }
    }

    public static void setDefaultVisibility(AJRigInstance rig, String boneName, boolean value) {
        setDefaultVisibility(rig, Set.of(boneName), value);
    }
    public static void setDefaultVisibility(AJRigInstance rig, Set<String> boneNames, boolean value) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.defaultVisibility = value;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "default_visibility");
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "shown_for_players");
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "hidden_for_players");
            }
        }
    }

    public static void setShownForPlayers(AJRigInstance rig, String boneName, Set<java.util.UUID> players) {
        setShownForPlayers(rig, Set.of(boneName), players);
    }
    public static void setShownForPlayers(AJRigInstance rig, Set<String> boneNames, Set<java.util.UUID> players) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.shownForPlayers.clear();
                info.shownForPlayers.addAll(players);
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "shown_for_players");
            }
        }
    }

    public static void setHiddenForPlayers(AJRigInstance rig, String boneName, Set<java.util.UUID> players) {
        setHiddenForPlayers(rig, Set.of(boneName), players);
    }
    public static void setHiddenForPlayers(AJRigInstance rig, Set<String> boneNames, Set<java.util.UUID> players) {
        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.hiddenForPlayers.clear();
                info.hiddenForPlayers.addAll(players);
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "hidden_for_players");
            }
        }
    }

    public static void setVariant(AJRigInstance rig, String boneName, String variant) {
        setVariant(rig, Set.of(boneName), variant);
    }
    public static void setVariant(AJRigInstance rig, Set<String> boneNames, String variant) {
        AJVariantData.VariantData variantData = AJVariantData.getVariantData(rig.getExportNamespace(), variant);
        if (variantData == null) return;
        Set<String> allowedBones = new HashSet<>(variantData.bones);

        for (String name : RigEntityUtil.filterBoneNames(rig, boneNames)) {
            if (!allowedBones.contains(name)) continue;
            AJRigInstance.BoneInfo info = rig.getBoneInfo(name);
            if (info != null) {
                info.variant = variant;
                AJRigApplyBoneStateUtil.markStateNotApplied(info, "variant");
            }
        }
    }
}