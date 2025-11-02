package com.jmcore.core.data.component;

import com.jmcore.core.data.PlayerDataComponentRegistry;

public class AnvilInputData {
    static {
        PlayerDataComponentRegistry.register(AnvilInputData.class, data -> new AnvilInputData());
    }

    private String lastAnvilInput = "";

    public String getLastAnvilInput() { return lastAnvilInput; }
    public void setLastAnvilInput(String input) { this.lastAnvilInput = input; }
}