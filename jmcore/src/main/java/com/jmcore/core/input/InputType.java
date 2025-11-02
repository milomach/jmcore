package com.jmcore.core.input;

/**
 * Represents all supported movement input types (polled each tick).
 * Do not add event-based input types here.
 */
public enum InputType {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    SNEAK,
    SPRINT,
    JUMP
}