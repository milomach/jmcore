package com.jmcore.core.input;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.DebugData;
import com.jmcore.core.debug.DebugType;
import com.jmcore.core.input.events.AnvilInputEvent;
import com.jmcore.core.input.events.AttackInputEvent;
import com.jmcore.core.input.events.DropItemInputEvent;
import com.jmcore.core.input.events.InteractInputEvent;
import com.jmcore.core.input.events.InventoryClickInputEvent;
import com.jmcore.core.input.events.ItemHeldInputEvent;
import com.jmcore.core.input.events.JumpInputEvent;
import com.jmcore.core.input.events.SneakInputEvent;
import com.jmcore.core.input.events.SprintInputEvent;
import com.jmcore.core.input.events.SwapHandInputEvent;

public class InputDebugListener {
    private final PlayerDataManager playerDataManager;

    public InputDebugListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        InputEventBus.register(this::onInputEvent);
    }

    private void onInputEvent(PluginInputEvent event) {
        PlayerData data = playerDataManager.get(event.getPlayer());
        if (data == null) return;
        DebugData debugData = data.getComponent(DebugData.class);
        if (debugData == null) return;

        StringBuilder sb = new StringBuilder();
        String playerName = event.getPlayer().getName();

        if (event instanceof SneakInputEvent sneak && debugData.isDebugEnabled(DebugType.SNEAK_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Sneak Input: ").append(sneak.isSneaking() ? "Started sneaking" : "Stopped sneaking").append("\n");
            System.out.print(sb);
        } else if (event instanceof SprintInputEvent sprint && debugData.isDebugEnabled(DebugType.SPRINT_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Sprint Input: ").append(sprint.isSprinting() ? "Started sprinting" : "Stopped sprinting").append("\n");
            System.out.print(sb);
        } else if (event instanceof SwapHandInputEvent && debugData.isDebugEnabled(DebugType.SWAP_HAND_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Swap Hand Input: Player used swap hand key\n");
            System.out.print(sb);
        } else if (event instanceof DropItemInputEvent && debugData.isDebugEnabled(DebugType.DROP_ITEM_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Drop Item Input: Player dropped an item\n");
            System.out.print(sb);
        } else if (event instanceof ItemHeldInputEvent held && debugData.isDebugEnabled(DebugType.ITEM_HELD_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Item Held Input: Switched from slot ")
              .append(held.getPreviousSlot())
              .append(" to slot ")
              .append(held.getNewSlot())
              .append("\n");
            System.out.print(sb);
        } else if (event instanceof JumpInputEvent && debugData.isDebugEnabled(DebugType.JUMP_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Jump Input: Player jumped\n");
            System.out.print(sb);
        } else if (event instanceof AttackInputEvent && debugData.isDebugEnabled(DebugType.ATTACK_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Attack Input: Player attacked the target interaction entity\n");
            System.out.print(sb);
        } else if (event instanceof InteractInputEvent && debugData.isDebugEnabled(DebugType.INTERACT_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Interact Input: Player interacted with the target interaction entity\n");
            System.out.print(sb);
        } else if (event instanceof AnvilInputEvent anvil && debugData.isDebugEnabled(DebugType.ANVIL_INPUT)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Anvil Input: ").append(anvil.getText()).append("\n");
            System.out.print(sb);
        } else if (event instanceof InventoryClickInputEvent click && debugData.isDebugEnabled(DebugType.INVENTORY_CLICK)) {
            sb.append("\n-------------------- [GUI] ").append(playerName).append(" --------------------\n");
            sb.append("Inventory Click Input:\n");
            sb.append("  Inventory: ").append(click.getClickedInventory() != null ? click.getClickedInventory().getType() : "null").append("\n");
            sb.append("  Slot: ").append(click.getSlot()).append("\n");
            sb.append("  Click Type: ").append(click.getClickType()).append("\n");
            System.out.print(sb);
        }
    }
}