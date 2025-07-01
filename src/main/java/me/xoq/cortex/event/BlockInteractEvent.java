package me.xoq.cortex.event;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Fired when a player interacts with a block.
 * Listeners may cancel to prevent the interaction.
 */
public class BlockInteractEvent extends CancellableEvent{
    private final ClientPlayerEntity player;
    private final Hand hand;
    private final BlockHitResult hitResult;

    public BlockInteractEvent(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        this.player = player;
        this.hand = hand;
        this.hitResult = hitResult;
    }

    public ClientPlayerEntity getPlayer() {
        return player;
    }

    public Hand getHand() {
        return hand;
    }

    public BlockHitResult getHitResult() {
        return hitResult;
    }
}
