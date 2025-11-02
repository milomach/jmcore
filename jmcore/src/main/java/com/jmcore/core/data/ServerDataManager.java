package com.jmcore.core.data;

public class ServerDataManager {
    private static final ServerData INSTANCE;

    static {
        INSTANCE = new ServerData();
        ServerDataComponentRegistry.attachAllComponents(INSTANCE);
    }

    public static ServerData get() {
        return INSTANCE;
    }
}