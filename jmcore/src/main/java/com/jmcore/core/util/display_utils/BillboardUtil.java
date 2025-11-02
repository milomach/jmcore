package com.jmcore.core.util.display_utils;

import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import java.util.Set;

public class BillboardUtil {

    public enum BillboardMode {
        FIXED, VERTICAL, HORIZONTAL, CENTER;

        public static BillboardMode fromString(String s) {
            switch (s.toLowerCase()) {
                case "fixed": return FIXED;
                case "vertical": return VERTICAL;
                case "horizontal": return HORIZONTAL;
                case "center": return CENTER;
                default: return null;
            }
        }
    }

    public static void setBillboardMode(Entity entity, BillboardMode mode) {
        if (entity instanceof Display display) {
            display.setBillboard(Display.Billboard.valueOf(mode.name()));
        }
    }

    public static void setBillboardMode(Set<Entity> entities, BillboardMode mode) {
        for (Entity e : entities) {
            setBillboardMode(e, mode);
        }
    }
}