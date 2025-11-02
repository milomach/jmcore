package com.jmcore.core.aj;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.jmcore.core.aj.rig_instance.AJRigInstance;
import com.jmcore.core.aj.rig_instance.AJRigInstance.AJRigScope;

import java.util.*;

public class AJRigManager {
    // Player-specific rigs: player UUID -> (internal rig id -> AJRigInstance)
    private final Map<UUID, Map<String, AJRigInstance>> rigsByPlayer = new HashMap<>();
    // Global rigs: internal rig id -> AJRigInstance
    private final Map<String, AJRigInstance> globalRigs = new HashMap<>();

    // Registers a new player-specific AJ rig
    public void registerRig(Player player, String internalId, AJRigInstance rig) {
        rigsByPlayer.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(internalId, rig);
    }

    // Gets a player-specific rig
    public AJRigInstance getRig(Player player, String internalId) {
        Map<String, AJRigInstance> rigs = rigsByPlayer.get(player.getUniqueId());
        return rigs != null ? rigs.get(internalId) : null;
    }

    // Removes a player-specific rig
    public void removeRig(Player player, String internalId) {
        Map<String, AJRigInstance> rigs = rigsByPlayer.get(player.getUniqueId());
        if (rigs != null) rigs.remove(internalId);
    }

    // Removes all rigs for a player
    public void removeAllRigsForPlayer(Player player) {
        rigsByPlayer.remove(player.getUniqueId());
    }

    // Gets all player-specific rigs for a player
    public Collection<AJRigInstance> getAllPlayerRigs() {
        List<AJRigInstance> all = new ArrayList<>();
        for (Map<String, AJRigInstance> rigs : rigsByPlayer.values()) {
            all.addAll(rigs.values());
        }
        return all;
    }

    // Registers a new global AJ rig
    public void registerGlobalRig(String internalId, AJRigInstance rig) {
        globalRigs.put(internalId, rig);
    }

    // Gets a global rig
    public AJRigInstance getGlobalRig(String internalId) {
        return globalRigs.get(internalId);
    }

    // Removes a global rig
    public void removeGlobalRig(String internalId) {
        globalRigs.remove(internalId);
    }

    // Gets all global rigs
    public Collection<AJRigInstance> getAllGlobalRigs() {
        return globalRigs.values();
    }

    // Gets a rig by scope, player name (if applicable), and internal ID
    public AJRigInstance getRigByScope(AJRigScope scope, String playerName, String internalId) {
    if (scope == AJRigScope.PLAYER && playerName != null) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            return getRig(player, internalId);
        }
    } else if (scope == AJRigScope.GLOBAL) {
        return getGlobalRig(internalId);
    }
    return null;
}
    
    // Clears all global and player rigs
    public void clearAllRigs() {
        rigsByPlayer.clear();
        globalRigs.clear();
    }
}