package com.jmcore.core.data.component;

import java.util.UUID;

import com.jmcore.core.data.PlayerDataComponentRegistry;
/**
 * General player data component, stores the player's UUID and a short unique identifier.
 */
public class GeneralData {
    static {
        // Register this component for all PlayerData, passing the player's UUID
        PlayerDataComponentRegistry.register(GeneralData.class, data -> new GeneralData(data.getPlayerId()));
    }

    private final UUID uuid;
    private String uniqueId; // Short unique identifier (e.g., first 8 hex digits of UUID)

    public GeneralData(UUID uuid) {
        this.uuid = uuid;
        this.uniqueId = generateShortId(uuid);
    }

    /**
     * Generates a short unique identifier from the player's UUID.
     * This can be used for tagging entities/items for cross-plugin/datapack identification.
     * Example: "3441c1db" from UUID "3441c1db-2030-4dff-8fd2-fa10917b500f"
     */
    private String generateShortId(UUID uuid) {
        // Use the first 8 hex digits of the UUID (most significant bits)
        return uuid.toString().replace("-", "").substring(0, 8);
    }

    /**
     * Returns the player's full UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the player's short unique identifier.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Allows regenerating the uniqueId if needed (e.g., for migration).
     */
    public void regenerateUniqueId() {
        this.uniqueId = generateShortId(this.uuid);
    }
}