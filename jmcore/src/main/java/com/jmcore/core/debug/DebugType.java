package com.jmcore.core.debug;

/**
 * Types of debugging available in the plugin.
 */
public enum DebugType {
    ROTATION_DIRECTION, // Title-based debugging (directions)
    CURSOR_POSITION,    // Console-based debugging (cursor/box info)
    MOVEMENT_INPUT,     // Console-based debugging (movement input states)
    SNEAK_INPUT,        // Console-based debugging (sneak input events)
    SPRINT_INPUT,       // Console-based debugging (sprint input events)
    SWAP_HAND_INPUT,    // Console-based debugging (swap hand events)
    DROP_ITEM_INPUT,    // Console-based debugging (drop item events)
    ITEM_HELD_INPUT,     // Console-based debugging (item held change events)
    JUMP_INPUT,      // Console-based debugging (jump input events)
    ATTACK_INPUT,      // Console-based debugging (attack input events)
    INTERACT_INPUT,     // Console-based debugging (interact input events)
    ANVIL_INPUT, // Console-based debugging (anvil input text changes)
    INVENTORY_CLICK, // Console-based debugging (inventory click events)
}