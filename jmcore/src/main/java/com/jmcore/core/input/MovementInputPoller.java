package com.jmcore.core.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.DebugData;
import com.jmcore.core.data.component.MovementInputData;
import com.jmcore.core.debug.DebugType;


/**
 * Polls the absolute current movement input state for all players each tick.
 * Updates PlayerData with the latest movement input states.
 * Uses org.bukkit.Input API for digital movement input states.
 * 
 * To add new movement types, update InputType and this poller.
 * For other input categories (mouse, inventory, etc.), create separate pollers/listeners.
 */
public class MovementInputPoller extends BukkitRunnable {
    private final PlayerDataManager playerDataManager;

    public MovementInputPoller(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerDataManager.get(player);
            MovementInputData movementInput = data.getComponent(MovementInputData.class);
            DebugData debugData = data.getComponent(DebugData.class);

            org.bukkit.Input in = player.getCurrentInput();

            // Update input states
            movementInput.setInputState(InputType.FORWARD, in.isForward());
            movementInput.setInputState(InputType.BACKWARD, in.isBackward());
            movementInput.setInputState(InputType.LEFT, in.isLeft());
            movementInput.setInputState(InputType.RIGHT, in.isRight());
            movementInput.setInputState(InputType.JUMP, in.isJump());
            movementInput.setInputState(InputType.SNEAK, in.isSneak());
            movementInput.setInputState(InputType.SPRINT, in.isSprint());

            // --- Centralized debug: movement input console logging ---
            if (debugData.isDebugEnabled(DebugType.MOVEMENT_INPUT)) {
                boolean anyInput = in.isForward() || in.isBackward() || in.isLeft() || in.isRight()
                        || in.isJump() || in.isSneak() || in.isSprint();
                if (anyInput) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n-------------------- [GUI] ").append(player.getName()).append(" --------------------\n");
                    sb.append("Movement Inputs:\n");
                    sb.append("  Forward : ").append(in.isForward() ? "ON" : "off").append("\n");
                    sb.append("  Backward: ").append(in.isBackward() ? "ON" : "off").append("\n");
                    sb.append("  Left    : ").append(in.isLeft() ? "ON" : "off").append("\n");
                    sb.append("  Right   : ").append(in.isRight() ? "ON" : "off").append("\n");
                    sb.append("  Jump    : ").append(in.isJump() ? "ON" : "off").append("\n");
                    sb.append("  Sneak   : ").append(in.isSneak() ? "ON" : "off").append("\n");
                    sb.append("  Sprint  : ").append(in.isSprint() ? "ON" : "off").append("\n");
                    sb.append("------------------------------------------------------------");
                    Bukkit.getLogger().info(sb.toString());
                }
            }
        }
    }
}