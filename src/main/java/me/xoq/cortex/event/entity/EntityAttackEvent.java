package me.xoq.cortex.event.entity;

import me.xoq.cortex.event.CancellableEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Fired when the client player attempts to attack an entity.
 * Listeners may cancel to prevent the attack packet from being sent.
 */
public class EntityAttackEvent extends CancellableEvent {
    private final PlayerEntity player;
    private final Entity target;

    public EntityAttackEvent(PlayerEntity player, Entity target) {
        this.player = player;
        this.target = target;
    }

    /** The player attacking */
    public PlayerEntity getPlayer() {
        return player;
    }

    /** The entity being attacked */
    public Entity getTarget() {
        return target;
    }
}
