package com.jmcore.core.data.component;

import java.util.EnumSet;
import java.util.Set;

import com.jmcore.core.data.PlayerDataComponentRegistry;
import com.jmcore.core.debug.DebugType;

public class DebugData {
    static {
        PlayerDataComponentRegistry.register(DebugData.class, data -> new DebugData());
    }

    private final Set<DebugType> enabledDebugTypes = EnumSet.noneOf(DebugType.class);

    public Set<DebugType> getEnabledDebugTypes() { return enabledDebugTypes; }
    public boolean isDebugEnabled(DebugType type) { return enabledDebugTypes.contains(type); }
    public void enableDebug(DebugType type) { enabledDebugTypes.add(type); }
    public void disableDebug(DebugType type) { enabledDebugTypes.remove(type); }
}