package com.jmcore.core.data.component;

import com.jmcore.core.data.PlayerDataComponentRegistry;

public class AttackInteractInputData {
    static {
        PlayerDataComponentRegistry.register(AttackInteractInputData.class, data -> new AttackInteractInputData());
    }
    
    private long lastAttackTimestamp = -1, lastInteractionTimestamp = -1;

    public long getLastAttackTimestamp() { return lastAttackTimestamp; }
    public void setLastAttackTimestamp(long ts) { this.lastAttackTimestamp = ts; }
    public long getLastInteractionTimestamp() { return lastInteractionTimestamp; }
    public void setLastInteractionTimestamp(long ts) { this.lastInteractionTimestamp = ts; }
}
