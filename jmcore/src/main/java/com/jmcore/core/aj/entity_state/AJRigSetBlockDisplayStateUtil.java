package com.jmcore.core.aj.entity_state;

import java.util.Set;
import java.util.UUID;

import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;

public class AJRigSetBlockDisplayStateUtil {

    // --- Pose Interpolation ---
    public static void setPoseInterpDelay(AJRigInstance rig, String name, int value) {
        setPoseInterpDelay(rig, Set.of(name), value);
    }
    public static void setPoseInterpDelay(AJRigInstance rig, Set<String> names, int value) {
        for (String n : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(n);
            if (info != null) info.poseInterpDelay = value;
        }
    }

    public static void setPoseInterpDuration(AJRigInstance rig, String name, int value) {
        setPoseInterpDuration(rig, Set.of(name), value);
    }
    public static void setPoseInterpDuration(AJRigInstance rig, Set<String> names, int value) {
        for (String n : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(n);
            if (info != null) info.poseInterpDuration = value;
        }
    }

    public static void setPoseTeleportDuration(AJRigInstance rig, String name, int value) {
        setPoseTeleportDuration(rig, Set.of(name), value);
    }
    public static void setPoseTeleportDuration(AJRigInstance rig, Set<String> names, int value) {
        for (String n : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(n);
            if (info != null) info.poseTeleportDuration = value;
        }
    }

    // --- State fields ---
    public static void setBillboardMode(AJRigInstance rig, String name, String value) {
        setBillboardMode(rig, Set.of(name), value);
    }
    public static void setBillboardMode(AJRigInstance rig, Set<String> names, String value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.billboardMode = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "billboard_mode");
            }
        }
    }

    public static void setBrightnessMode(AJRigInstance rig, String name, String value) {
        setBrightnessMode(rig, Set.of(name), value);
    }
    public static void setBrightnessMode(AJRigInstance rig, Set<String> names, String value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.brightnessMode = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setBlockBrightness(AJRigInstance rig, String name, int value) {
        setBlockBrightness(rig, Set.of(name), value);
    }
    public static void setBlockBrightness(AJRigInstance rig, Set<String> names, int value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.blockBrightness = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setSkyBrightness(AJRigInstance rig, String name, int value) {
        setSkyBrightness(rig, Set.of(name), value);
    }
    public static void setSkyBrightness(AJRigInstance rig, Set<String> names, int value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.skyBrightness = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setDisplayWidth(AJRigInstance rig, String name, float value) {
        setDisplayWidth(rig, Set.of(name), value);
    }
    public static void setDisplayWidth(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.displayWidth = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "display_width");
            }
        }
    }

    public static void setDisplayHeight(AJRigInstance rig, String name, float value) {
        setDisplayHeight(rig, Set.of(name), value);
    }
    public static void setDisplayHeight(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.displayHeight = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "display_height");
            }
        }
    }

    public static void setGlowMode(AJRigInstance rig, String name, String value) {
        setGlowMode(rig, Set.of(name), value);
    }
    public static void setGlowMode(AJRigInstance rig, Set<String> names, String value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.glowMode = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "glow");
            }
        }
    }

    public static void setGlowColor(AJRigInstance rig, String name, int value) {
        setGlowColor(rig, Set.of(name), value);
    }
    public static void setGlowColor(AJRigInstance rig, Set<String> names, int value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.glowColor = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "glow");
            }
        }
    }

    public static void setShadowRadius(AJRigInstance rig, String name, float value) {
        setShadowRadius(rig, Set.of(name), value);
    }
    public static void setShadowRadius(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.shadowRadius = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "shadow_radius");
            }
        }
    }

    public static void setShadowStrength(AJRigInstance rig, String name, float value) {
        setShadowStrength(rig, Set.of(name), value);
    }
    public static void setShadowStrength(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.shadowStrength = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "shadow_strength");
            }
        }
    }

    public static void setViewRange(AJRigInstance rig, String name, float value) {
        setViewRange(rig, Set.of(name), value);
    }
    public static void setViewRange(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.viewRange = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "view_range");
            }
        }
    }

    public static void setDefaultVisibility(AJRigInstance rig, String name, boolean value) {
        setDefaultVisibility(rig, Set.of(name), value);
    }
    public static void setDefaultVisibility(AJRigInstance rig, Set<String> names, boolean value) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.defaultVisibility = value;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "default_visibility");
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "shown_for_players");
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "hidden_for_players");
            }
        }
    }

    public static void setShownForPlayers(AJRigInstance rig, String name, Set<UUID> players) {
        setShownForPlayers(rig, Set.of(name), players);
    }
    public static void setShownForPlayers(AJRigInstance rig, Set<String> names, Set<UUID> players) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.shownForPlayers.clear();
                info.shownForPlayers.addAll(players);
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "shown_for_players");
            }
        }
    }

    public static void setHiddenForPlayers(AJRigInstance rig, String name, Set<UUID> players) {
        setHiddenForPlayers(rig, Set.of(name), players);
    }
    public static void setHiddenForPlayers(AJRigInstance rig, Set<String> names, Set<UUID> players) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.hiddenForPlayers.clear();
                info.hiddenForPlayers.addAll(players);
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "hidden_for_players");
            }
        }
    }

    public static void setBlock(AJRigInstance rig, String name, String block) {
        setBlock(rig, Set.of(name), block);
    }
    public static void setBlock(AJRigInstance rig, Set<String> names, String block) {
        for (String name : RigEntityUtil.filterBlockDisplayNames(rig, names)) {
            AJRigInstance.BlockDisplayInfo info = rig.getBlockDisplayInfo(name);
            if (info != null) {
                info.block = block;
                AJRigApplyBlockDisplayStateUtil.markStateNotApplied(info, "block");
            }
        }
    }
}