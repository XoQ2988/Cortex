package me.xoq.cortex.event.entity;


import me.xoq.cortex.event.CancellableEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * Fired when the client player interacts (right-clicks) with an entity.
 * Listeners may cancel to prevent the interaction.
 */
public class EntityInteractEvent extends CancellableEvent {
    private final PlayerEntity player;
    private final Entity target;
    private final Hand hand;

    public EntityInteractEvent(PlayerEntity player, Entity target, Hand hand) {
        this.player = player;
        this.target = target;
        this.hand   = hand;
    }

    /** The player interacting. */
    public PlayerEntity getPlayer() {
        return player;
    }

    /** The entity being interacted with. */
    public Entity getTarget() {
        return target;
    }

    /** Which hand was used (main or off-hand). */
    public Hand getHand() {
        return hand;
    }
}
