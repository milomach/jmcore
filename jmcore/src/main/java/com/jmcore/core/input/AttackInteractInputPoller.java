package com.jmcore.core.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.jmcore.core.data.PlayerData;
import com.jmcore.core.data.PlayerDataManager;
import com.jmcore.core.data.component.AttackInteractInputData;
import com.jmcore.core.input.events.AttackInputEvent;
import com.jmcore.core.input.events.InteractInputEvent;
import com.jmcore.core.player.state.utils.InteractionEntitiesUtil;

import java.util.UUID;

/**
 * Polls the target interaction entity for attack and interact input each tick.
 * Fires internal events when a new attack or interaction is detected,
 * but only if the action was performed by the player who owns the entity.
 */
public class AttackInteractInputPoller extends BukkitRunnable {
    private final PlayerDataManager playerDataManager;

    public AttackInteractInputPoller(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerDataManager.get(player);
            AttackInteractInputData attackInteractInput = data.getComponent(AttackInteractInputData.class);
            Interaction target = InteractionEntitiesUtil.getTarget(data);
            if (target == null) continue;

            Interaction.PreviousInteraction lastAttack = target.getLastAttack();
            Interaction.PreviousInteraction lastInteraction = target.getLastInteraction();

            // Attack detection
            if (lastAttack != null) {
                long attackTs = lastAttack.getTimestamp();
                UUID attacker = lastAttack.getPlayer().getUniqueId();
                if (attackTs > 0
                        && attackTs != attackInteractInput.getLastAttackTimestamp()
                        && attacker != null
                        && attacker.equals(player.getUniqueId())) {
                    attackInteractInput.setLastAttackTimestamp(attackTs);
                    InputEventBus.post(new AttackInputEvent(player));
                }
            }

            // Interact detection
            if (lastInteraction != null) {
                long interactTs = lastInteraction.getTimestamp();
                UUID interactor = lastInteraction.getPlayer().getUniqueId();
                if (interactTs > 0
                        && interactTs != attackInteractInput.getLastInteractionTimestamp()
                        && interactor != null
                        && interactor.equals(player.getUniqueId())) {
                    attackInteractInput.setLastInteractionTimestamp(interactTs);
                    InputEventBus.post(new InteractInputEvent(player));
                }
            }
        }
    }
}