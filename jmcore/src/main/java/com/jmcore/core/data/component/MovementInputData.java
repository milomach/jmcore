package com.jmcore.core.data.component;

import java.util.EnumMap;
import java.util.Map;

import com.jmcore.core.data.PlayerDataComponentRegistry;
import com.jmcore.core.input.InputType;

public class MovementInputData {
    static {
        PlayerDataComponentRegistry.register(MovementInputData.class, data -> new MovementInputData());
    }

    private final Map<InputType, Boolean> inputStates = new EnumMap<>(InputType.class);

    public void setInputState(InputType type, boolean pressed) { inputStates.put(type, pressed); }
    public boolean isInputPressed(InputType type) { return inputStates.getOrDefault(type, false); }
    public Map<InputType, Boolean> getInputStates() { return inputStates; }
}