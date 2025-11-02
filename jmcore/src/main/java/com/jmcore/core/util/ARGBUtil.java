package com.jmcore.core.util;

import org.bukkit.Color;

/**
 * Utility for converting ARGB integer (0xAARRGGBB) into Bukkit Color.
 */
public class ARGBUtil {
    public static Color fromARGB(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b = (argb      ) & 0xFF;
        // Bukkit's Color only supports RGB; alpha is implicit.
        return Color.fromRGB(r, g, b);
    }
}