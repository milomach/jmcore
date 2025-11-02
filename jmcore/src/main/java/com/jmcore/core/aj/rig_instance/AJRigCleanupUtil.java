package com.jmcore.core.aj.rig_instance;

import org.bukkit.entity.Player;

import com.jmcore.core.aj.AJRigManager;
import com.jmcore.core.aj.rig_instance.AJRigInstance.AJRigScope;

public class AJRigCleanupUtil {

    /**
     * Cleans up a rig instance: removes all entities and unregisters the rig from the manager.
     */
    public static void cleanupRig(
            AJRigInstance rig,
            AJRigManager manager,
            AJRigScope scope,
            Player player // null if not player scope
    ) {
        AJRigRemoveUtil.removeRootEntity(rig);
        if (scope == AJRigScope.PLAYER && player != null) {
            manager.removeRig(player, rig.getInternalId());
        } else if (scope == AJRigScope.GLOBAL) {
            manager.removeGlobalRig(rig.getInternalId());
        }
    }
}