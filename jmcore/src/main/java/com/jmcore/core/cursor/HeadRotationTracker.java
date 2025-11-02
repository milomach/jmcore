package com.jmcore.core.cursor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.CursorData;
import com.jmcore.core.data.component.DebugData;
import com.jmcore.core.debug.DebugType;
import com.jmcore.core.player.state.managers.CursorCleanup;
import com.jmcore.core.player.state.managers.CursorMaintenance;
import com.jmcore.core.player.state.managers.CursorSetup;

import org.bukkit.plugin.Plugin;
import net.kyori.adventure.title.Title;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeadRotationTracker extends BukkitRunnable {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;
    private final CursorSetup cursorSetup;
    private final CursorCleanup cursorCleanup;
    private final Map<UUID, Float[]> previousRotations = new HashMap<>();
    private final Map<UUID, RotationDelta> latestDeltas = new HashMap<>();
    private final Map<UUID, BoxDelta> latestBoxDeltas = new HashMap<>();

    public HeadRotationTracker(Plugin plugin, PlayerDataManager playerDataManager, CursorMaintenance cursorMaintenance) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.cursorSetup = new CursorSetup(plugin, playerDataManager, cursorMaintenance);
        this.cursorCleanup = new CursorCleanup(playerDataManager, cursorMaintenance);
    }

    public void enable(Player player) {
        cursorSetup.setup(player);
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        CursorData cursorData = data.getComponent(CursorData.class);
        if (cursorData == null) return;

        cursorData.setHeadTrackingEnabled(true);
        cursorData.resetCurrentBox();
        previousRotations.put(player.getUniqueId(), new Float[]{player.getLocation().getYaw(), player.getLocation().getPitch()});
        latestDeltas.put(player.getUniqueId(), new RotationDelta(0.0, 0.0));
        latestBoxDeltas.put(player.getUniqueId(), new BoxDelta(0.0, 0.0));
    }

    public void disable(Player player) {
        cursorCleanup.cleanup(player);
        PlayerData data = playerDataManager.get(player);
        if (data == null) return;
        CursorData cursorData = data.getComponent(CursorData.class);
        if (cursorData == null) return;

        cursorData.setHeadTrackingEnabled(false);
        previousRotations.remove(player.getUniqueId());
        latestDeltas.remove(player.getUniqueId());
        latestBoxDeltas.remove(player.getUniqueId());
        player.clearTitle();
    }

    public boolean isEnabled(Player player) {
        PlayerData data = playerDataManager.get(player);
        CursorData cursorData = data.getComponent(CursorData.class);
        return cursorData.isHeadTrackingEnabled();
    }

    public RotationDelta getLatestDelta(Player player) {
        return latestDeltas.get(player.getUniqueId());
    }

    public BoxDelta getLatestBoxDelta(Player player) {
        return latestBoxDeltas.get(player.getUniqueId());
    }

    public int[] getCurrentBox(Player player) {
        PlayerData data = playerDataManager.get(player);
        CursorData cursorData = data.getComponent(CursorData.class); 
        return new int[] { cursorData.getCurrentBoxX(), cursorData.getCurrentBoxY() };
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerDataManager.get(player);
            CursorData cursorData = data.getComponent(CursorData.class);
            DebugData debugData = data.getComponent(DebugData.class);

            if (!cursorData.isHeadTrackingEnabled()) continue;

            UUID uuid = player.getUniqueId();
            Float[] prev = previousRotations.get(uuid);
            if (prev == null) {
                previousRotations.put(uuid, new Float[]{player.getLocation().getYaw(), player.getLocation().getPitch()});
                latestDeltas.put(uuid, new RotationDelta(0.0, 0.0));
                latestBoxDeltas.put(uuid, new BoxDelta(0.0, 0.0));
                cursorData.resetInternalBox();
                cursorData.resetCurrentBox();
                continue;
            }
            float prevYaw = prev[0];
            float prevPitch = prev[1];

            float currYaw = player.getLocation().getYaw();
            float currPitch = player.getLocation().getPitch();

            // Calculate delta, accounting for yaw wrap-around
            double deltaYaw = normalizeYaw(currYaw - prevYaw);
            double deltaPitchRaw = currPitch - prevPitch;
            double deltaPitch = -deltaPitchRaw; // Invert so positive = up, negative = down

            latestDeltas.put(uuid, new RotationDelta(deltaYaw, deltaPitch));

            // Calculate and store box delta
            double degreesPerBox = cursorData.getDegreesPerBox();
            if (degreesPerBox == 0.0) degreesPerBox = 1.0;
            double deltaBoxesYaw = deltaYaw / degreesPerBox;
            double deltaBoxesPitch = deltaPitch / degreesPerBox;
            latestBoxDeltas.put(uuid, new BoxDelta(deltaBoxesYaw, deltaBoxesPitch));

            // --- Update internal box position (full precision) ---
            double newInternalBoxX = cursorData.getInternalBoxX() + deltaBoxesYaw;
            double newInternalBoxY = cursorData.getInternalBoxY() + deltaBoxesPitch;

            // Clamp internal box to allowed range
            double minX = -cursorData.getMaxBoxXNeg();
            double maxX = cursorData.getMaxBoxXPos();
            double minY = -cursorData.getMaxBoxYNeg();
            double maxY = cursorData.getMaxBoxYPos();

            if (newInternalBoxX < minX) newInternalBoxX = minX;
            if (newInternalBoxX > maxX) newInternalBoxX = maxX;
            if (newInternalBoxY < minY) newInternalBoxY = minY;
            if (newInternalBoxY > maxY) newInternalBoxY = maxY;

            cursorData.setInternalBox(newInternalBoxX, newInternalBoxY);

            // --- Set external (integer) box by rounding internal box ---
            int newBoxX = (int) Math.round(newInternalBoxX);
            int newBoxY = (int) Math.round(newInternalBoxY);
            cursorData.setCurrentBox(newBoxX, newBoxY);

            // --- Only log if there is a change in yaw or pitch ---
            boolean yawChanged = Math.abs(deltaYaw) > 0.0001;
            boolean pitchChanged = Math.abs(deltaPitch) > 0.0001;

            // Centralized debug: console logging
            if ((yawChanged || pitchChanged) && debugData.isDebugEnabled(DebugType.CURSOR_POSITION)) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n-------------------- [GUI] ").append(player.getName()).append(" --------------------\n");
                sb.append("ΔYaw   = ").append(String.format("%+.2f°", deltaYaw)).append("\n");
                sb.append("ΔPitch = ").append(String.format("%+.2f°", deltaPitch)).append("\n");
                sb.append("ΔBoxYaw   = ").append(String.format("%+.4f", deltaBoxesYaw)).append("\n");
                sb.append("ΔBoxPitch = ").append(String.format("%+.4f", deltaBoxesPitch)).append("\n");
                sb.append("InternalBox = (")
                  .append(String.format("%.4f", newInternalBoxX)).append(", ")
                  .append(String.format("%.4f", newInternalBoxY)).append(")\n");
                sb.append("Box         = (").append(newBoxX).append(", ").append(newBoxY).append(")\n");
                sb.append("------------------------------------------------------------");
                plugin.getLogger().info(sb.toString());
            }

            // Centralized debug: title display
            if (debugData.isDebugEnabled(DebugType.ROTATION_DIRECTION)) {
                StringBuilder directions = new StringBuilder();
                if (deltaYaw < -0.1) directions.append("L");
                else if (deltaYaw > 0.1) directions.append("R");
                if (deltaPitch > 0.1) directions.append("U");
                else if (deltaPitch < -0.1) directions.append("D");

                if (directions.length() > 0) {
                    Title title = Title.title(
                        net.kyori.adventure.text.Component.text(directions.toString()),
                        net.kyori.adventure.text.Component.empty(),
                        Title.Times.times(java.time.Duration.ZERO, java.time.Duration.ofMillis(500), java.time.Duration.ofMillis(500))
                    );
                    player.showTitle(title);
                } else {
                    player.clearTitle();
                }
            } else {
                player.clearTitle();
            }

            // --- Handle pitch lock at vertical extremes ---
            if (Math.abs(currPitch) >= 90.0f) {
                player.setRotation(currYaw, 0.0f); // Directly set yaw and pitch
                previousRotations.put(uuid, new Float[]{currYaw, 0.0f});
            } else {
                previousRotations.put(uuid, new Float[]{currYaw, currPitch});
            }
        }
    }

    private double normalizeYaw(double yaw) {
        while (yaw > 180) yaw -= 360;
        while (yaw < -180) yaw += 360;
        return yaw;
    }
}