package com.jmcore.core.aj.rig_instance;

import org.bukkit.entity.Player;

import com.jmcore.core.aj.data.AJBlockDisplayData;
import com.jmcore.core.aj.data.AJBoneData;
import com.jmcore.core.aj.data.AJItemDisplayData;
import com.jmcore.core.aj.data.AJLocatorData;
import com.jmcore.core.aj.data.AJTextDisplayData;
import com.jmcore.core.aj.rig_instance.AJRigInstance.AJRigScope;

public class AJRigSetupUtil {

    /**
     * Sets up a new AJRigInstance with persistent info for all entity types.
     * Does NOT summon any entities.
     */
    public static AJRigInstance setupRigInstance(
            AJRigScope scope,
            Player player,
            String exportNamespace,
            String internalId
    ) {
        AJRigInstance rig = new AJRigInstance(exportNamespace, internalId);
        // Initialize info for all entity types in the namespace
        rig.initializeAllInfos(
                AJBoneData.getAllBoneNames(exportNamespace),
                AJLocatorData.getAllLocatorNames(exportNamespace),
                AJItemDisplayData.getAllItemDisplayNames(exportNamespace),
                AJBlockDisplayData.getAllBlockDisplayNames(exportNamespace),
                AJTextDisplayData.getAllTextDisplayNames(exportNamespace)
        );
        rig.setupDefaultPoseSource();
        return rig;
    }
}