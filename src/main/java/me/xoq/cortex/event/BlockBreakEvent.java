package me.xoq.cortex.event;

import net.minecraft.util.math.BlockPos;

/**
 * Fired when a player breaks a block (client-side).
 * Cancelling via listeners causes weird behaviour.
 */
public class BlockBreakEvent extends CancellableEvent{
    private final BlockPos pos;

    public BlockBreakEvent(BlockPos pos) {
        this.pos = pos;
    }

    /** Position broken. */
    public BlockPos getPos() {
        return pos;
    }
}
