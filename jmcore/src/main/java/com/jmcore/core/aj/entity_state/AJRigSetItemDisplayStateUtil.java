package com.jmcore.core.aj.entity_state;

import java.util.Set;
import java.util.UUID;

import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;

public class AJRigSetItemDisplayStateUtil {

    // --- Pose Interpolation ---
    public static void setPoseInterpDelay(AJRigInstance rig, String name, int value) {
        setPoseInterpDelay(rig, Set.of(name), value);
    }
    public static void setPoseInterpDelay(AJRigInstance rig, Set<String> names, int value) {
        for (String n : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(n);
            if (info != null) info.poseInterpDelay = value;
        }
    }

    public static void setPoseInterpDuration(AJRigInstance rig, String name, int value) {
        setPoseInterpDuration(rig, Set.of(name), value);
    }
    public static void setPoseInterpDuration(AJRigInstance rig, Set<String> names, int value) {
        for (String n : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(n);
            if (info != null) info.poseInterpDuration = value;
        }
    }

    public static void setPoseTeleportDuration(AJRigInstance rig, String name, int value) {
        setPoseTeleportDuration(rig, Set.of(name), value);
    }
    public static void setPoseTeleportDuration(AJRigInstance rig, Set<String> names, int value) {
        for (String n : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(n);
            if (info != null) info.poseTeleportDuration = value;
        }
    }

    // --- State fields ---
    public static void setEnchanted(AJRigInstance rig, String name, boolean value) {
        setEnchanted(rig, Set.of(name), value);
    }
    public static void setEnchanted(AJRigInstance rig, Set<String> names, boolean value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.enchanted = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "enchanted");
            }
        }
    }

    public static void setBillboardMode(AJRigInstance rig, String name, String value) {
        setBillboardMode(rig, Set.of(name), value);
    }
    public static void setBillboardMode(AJRigInstance rig, Set<String> names, String value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.billboardMode = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "billboard_mode");
            }
        }
    }

    public static void setBrightnessMode(AJRigInstance rig, String name, String value) {
        setBrightnessMode(rig, Set.of(name), value);
    }
    public static void setBrightnessMode(AJRigInstance rig, Set<String> names, String value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.brightnessMode = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setBlockBrightness(AJRigInstance rig, String name, int value) {
        setBlockBrightness(rig, Set.of(name), value);
    }
    public static void setBlockBrightness(AJRigInstance rig, Set<String> names, int value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.blockBrightness = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setSkyBrightness(AJRigInstance rig, String name, int value) {
        setSkyBrightness(rig, Set.of(name), value);
    }
    public static void setSkyBrightness(AJRigInstance rig, Set<String> names, int value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.skyBrightness = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "brightness");
            }
        }
    }

    public static void setDisplayWidth(AJRigInstance rig, String name, float value) {
        setDisplayWidth(rig, Set.of(name), value);
    }
    public static void setDisplayWidth(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.displayWidth = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "display_width");
            }
        }
    }

    public static void setDisplayHeight(AJRigInstance rig, String name, float value) {
        setDisplayHeight(rig, Set.of(name), value);
    }
    public static void setDisplayHeight(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.displayHeight = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "display_height");
            }
        }
    }

    public static void setGlowMode(AJRigInstance rig, String name, String value) {
        setGlowMode(rig, Set.of(name), value);
    }
    public static void setGlowMode(AJRigInstance rig, Set<String> names, String value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.glowMode = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "glow");
            }
        }
    }

    public static void setGlowColor(AJRigInstance rig, String name, int value) {
        setGlowColor(rig, Set.of(name), value);
    }
    public static void setGlowColor(AJRigInstance rig, Set<String> names, int value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.glowColor = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "glow");
            }
        }
    }

    public static void setShadowRadius(AJRigInstance rig, String name, float value) {
        setShadowRadius(rig, Set.of(name), value);
    }
    public static void setShadowRadius(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.shadowRadius = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "shadow_radius");
            }
        }
    }

    public static void setShadowStrength(AJRigInstance rig, String name, float value) {
        setShadowStrength(rig, Set.of(name), value);
    }
    public static void setShadowStrength(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.shadowStrength = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "shadow_strength");
            }
        }
    }

    public static void setViewRange(AJRigInstance rig, String name, float value) {
        setViewRange(rig, Set.of(name), value);
    }
    public static void setViewRange(AJRigInstance rig, Set<String> names, float value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.viewRange = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "view_range");
            }
        }
    }

    public static void setDefaultVisibility(AJRigInstance rig, String name, boolean value) {
        setDefaultVisibility(rig, Set.of(name), value);
    }
    public static void setDefaultVisibility(AJRigInstance rig, Set<String> names, boolean value) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.defaultVisibility = value;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "default_visibility");
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "shown_for_players");
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "hidden_for_players");
            }
        }
    }

    public static void setShownForPlayers(AJRigInstance rig, String name, Set<UUID> players) {
        setShownForPlayers(rig, Set.of(name), players);
    }
    public static void setShownForPlayers(AJRigInstance rig, Set<String> names, Set<UUID> players) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.shownForPlayers.clear();
                info.shownForPlayers.addAll(players);
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "shown_for_players");
            }
        }
    }

    public static void setHiddenForPlayers(AJRigInstance rig, String name, Set<UUID> players) {
        setHiddenForPlayers(rig, Set.of(name), players);
    }
    public static void setHiddenForPlayers(AJRigInstance rig, Set<String> names, Set<UUID> players) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.hiddenForPlayers.clear();
                info.hiddenForPlayers.addAll(players);
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "hidden_for_players");
            }
        }
    }

    public static void setItem(AJRigInstance rig, String name, String item) {
        setItem(rig, Set.of(name), item);
    }
    public static void setItem(AJRigInstance rig, Set<String> names, String item) {
        for (String name : RigEntityUtil.filterItemDisplayNames(rig, names)) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info != null) {
                info.item = item;
                AJRigApplyItemDisplayStateUtil.markStateNotApplied(info, "item");
            }
        }
    }
}