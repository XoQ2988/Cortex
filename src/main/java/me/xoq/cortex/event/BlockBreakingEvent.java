package me.xoq.cortex.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Fired when a player begins breaking a block.
 * Listeners may cancel to prevent the break.
 */
public class BlockBreakingEvent extends CancellableEvent{
    private final BlockPos pos;
    private final Direction direction;

    public BlockBreakingEvent(BlockPos pos, Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }

    /** Position being broken. */
    public BlockPos getPos() {
        return pos;
    }

    /** Face being broken */
    public Direction getDirection() {
        return direction;
    }
}
