package com.jmcore.core.data.component;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.view.AnvilView;

import com.jmcore.core.data.PlayerDataComponentRegistry;

import java.util.function.Consumer;

public class AnvilMenuData {
    static {
        PlayerDataComponentRegistry.register(AnvilMenuData.class, data -> new AnvilMenuData());
    }

    private Consumer<AnvilView> anvilMenuCallback;
    private boolean anvilMenuOpened = false;
    private InventoryView exampleAnvilMenuView;
    private Long lastAnvilMenuReopen = null;

    public Consumer<AnvilView> getAnvilMenuCallback() { return anvilMenuCallback; }
    public void setAnvilMenuCallback(Consumer<AnvilView> callback) { this.anvilMenuCallback = callback; }

    public boolean isAnvilMenuOpened() { return anvilMenuOpened; }
    public void setAnvilMenuOpened(boolean opened) { this.anvilMenuOpened = opened; }

    public boolean hasExampleAnvilMenuOpen() { return anvilMenuCallback != null; }

    public InventoryView getExampleAnvilMenuView() { return exampleAnvilMenuView; }
    public void setExampleAnvilMenuView(InventoryView view) { this.exampleAnvilMenuView = view; }
    public void clearExampleAnvilMenuView() { this.exampleAnvilMenuView = null; }
    public Long getLastAnvilMenuReopen() { return lastAnvilMenuReopen; }
    public void setLastAnvilMenuReopen(Long time) { this.lastAnvilMenuReopen = time; }
}