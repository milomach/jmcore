package com.jmcore.core.aj.entity_state;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.RigEntityUtil;
import com.jmcore.core.util.display_utils.BillboardUtil;
import com.jmcore.core.util.display_utils.BrightnessUtil;
import com.jmcore.core.util.display_utils.CullingUtil;
import com.jmcore.core.util.display_utils.GlowUtil;
import com.jmcore.core.util.display_utils.ShadowUtil;
import com.jmcore.core.util.display_utils.ViewRangeUtil;
import com.jmcore.core.util.display_utils.VisibilityUtil;
import com.jmcore.core.util.display_utils.BillboardUtil.BillboardMode;
import com.jmcore.core.util.display_utils.BrightnessUtil.BrightnessMode;
import com.jmcore.core.util.display_utils.GlowUtil.GlowMode;
import com.jmcore.core.util.display_utils.item.EnchantUtil;
import com.jmcore.core.util.display_utils.item.ItemUtil;

import java.util.*;

public class AJRigApplyItemDisplayStateUtil {

    private static final List<String> STATE_KEYS = List.of(
        "enchanted",
        "billboard_mode",
        "brightness",
        "display_width",
        "display_height",
        "glow",
        "shadow_radius",
        "shadow_strength",
        "view_range",
        "default_visibility",
        "shown_for_players",
        "hidden_for_players",
        "item"
    );

    public static void markAllStatesNotApplied(AJRigInstance.ItemDisplayInfo info) {
        if (info.appliedStates == null) info.appliedStates = new HashMap<>();
        for (String key : STATE_KEYS) info.appliedStates.put(key, false);
    }

    public static void markStateNotApplied(AJRigInstance.ItemDisplayInfo info, String key) {
        if (info.appliedStates == null) markAllStatesNotApplied(info);
        info.appliedStates.put(key, false);
    }

    public static void markStateApplied(AJRigInstance.ItemDisplayInfo info, String key) {
        if (info.appliedStates == null) markAllStatesNotApplied(info);
        info.appliedStates.put(key, true);
    }

    /**
     * Applies all non-applied states for the set of item display UUIDs in the rig.
     */
    public static void applyItemDisplayStates(AJRigInstance rig, Set<UUID> uuids, Plugin plugin) {
        Set<String> names = RigEntityUtil.getItemDisplayNamesFromUUIDs(rig, uuids);
        for (String name : names) {
            AJRigInstance.ItemDisplayInfo info = rig.getItemDisplayInfo(name);
            if (info == null || info.entityUUID == null) continue;
            Entity entity = Bukkit.getEntity(info.entityUUID);
            if (!(entity instanceof Display display)) continue;

            if (info.appliedStates == null) markAllStatesNotApplied(info);

            // --- enchanted ---
            if (!info.appliedStates.getOrDefault("enchanted", false)) {
                EnchantUtil.setEnchantedGlint(display, info.enchanted);
                markStateApplied(info, "enchanted");
            }

            // --- billboard_mode ---
            if (!info.appliedStates.getOrDefault("billboard_mode", false)) {
                BillboardMode mode = BillboardMode.valueOf(info.billboardMode.toUpperCase());
                BillboardUtil.setBillboardMode(display, mode);
                markStateApplied(info, "billboard_mode");
            }

            // --- brightness ---
            if (!info.appliedStates.getOrDefault("brightness", false)) {
                if ("ambient".equalsIgnoreCase(info.brightnessMode)) {
                    BrightnessUtil.setBrightness(display, BrightnessMode.AMBIENT, 0, 0);
                } else {
                    BrightnessUtil.setBrightness(display, BrightnessMode.CUSTOM, info.blockBrightness, info.skyBrightness);
                }
                markStateApplied(info, "brightness");
            }

            // --- display_width ---
            if (!info.appliedStates.getOrDefault("display_width", false)) {
                CullingUtil.setDisplayWidth(display, info.displayWidth);
                markStateApplied(info, "display_width");
            }

            // --- display_height ---
            if (!info.appliedStates.getOrDefault("display_height", false)) {
                CullingUtil.setDisplayHeight(display, info.displayHeight);
                markStateApplied(info, "display_height");
            }

            // --- glow ---
            if (!info.appliedStates.getOrDefault("glow", false)) {
                if ("none".equalsIgnoreCase(info.glowMode)) {
                    GlowUtil.setGlowColorOverride(display, GlowMode.NONE, 0);
                } else {
                    GlowUtil.setGlowColorOverride(display, GlowMode.CUSTOM, info.glowColor);
                }
                markStateApplied(info, "glow");
            }

            // --- shadow_radius ---
            if (!info.appliedStates.getOrDefault("shadow_radius", false)) {
                ShadowUtil.setShadowRadius(display, info.shadowRadius);
                markStateApplied(info, "shadow_radius");
            }

            // --- shadow_strength ---
            if (!info.appliedStates.getOrDefault("shadow_strength", false)) {
                ShadowUtil.setShadowStrength(display, info.shadowStrength);
                markStateApplied(info, "shadow_strength");
            }

            // --- view_range ---
            if (!info.appliedStates.getOrDefault("view_range", false)) {
                ViewRangeUtil.setViewRange(display, info.viewRange);
                markStateApplied(info, "view_range");
            }

            // --- default_visibility ---
            if (!info.appliedStates.getOrDefault("default_visibility", false)) {
                VisibilityUtil.setDefaultVisibility(display, info.defaultVisibility);
                markStateApplied(info, "default_visibility");
            }

            // --- shown_for_players & hidden_for_players ---
            if (!info.appliedStates.getOrDefault("shown_for_players", false) ||
                !info.appliedStates.getOrDefault("hidden_for_players", false)) {

                Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
                if (info.defaultVisibility) {
                    Set<Player> hiddenPlayers = new HashSet<>();
                    for (UUID uuid : info.hiddenForPlayers) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) hiddenPlayers.add(p);
                    }
                    VisibilityUtil.setVisibilityForPlayers(display, plugin, hiddenPlayers, false);
                    Set<Player> visiblePlayers = new HashSet<>(onlinePlayers);
                    visiblePlayers.removeAll(hiddenPlayers);
                    VisibilityUtil.setVisibilityForPlayers(display, plugin, visiblePlayers, true);
                } else {
                    Set<Player> shownPlayers = new HashSet<>();
                    for (UUID uuid : info.shownForPlayers) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) shownPlayers.add(p);
                    }
                    VisibilityUtil.setVisibilityForPlayers(display, plugin, shownPlayers, true);
                    Set<Player> hiddenPlayers = new HashSet<>(onlinePlayers);
                    hiddenPlayers.removeAll(shownPlayers);
                    VisibilityUtil.setVisibilityForPlayers(display, plugin, hiddenPlayers, false);
                }
                markStateApplied(info, "shown_for_players");
                markStateApplied(info, "hidden_for_players");
            }

            // --- item ---
            if (!info.appliedStates.getOrDefault("item", false)) {
                ItemUtil.setItem(entity, info.item);
                markStateApplied(info, "item");
            }
        }
    }
}