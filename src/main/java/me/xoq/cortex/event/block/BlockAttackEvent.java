package me.xoq.cortex.event.block;

import me.xoq.cortex.event.CancellableEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Fired when a player begins breaking a block.
 * Listeners may cancel to prevent the break.
 */
public class BlockAttackEvent extends CancellableEvent {
    private final BlockPos pos;
    private final Direction direction;

    public BlockAttackEvent(BlockPos pos, Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }

    /** Position being attacked. */
    public BlockPos getPos() {
        return pos;
    }

    /** Face being attacked. */
    public Direction getDirection() {
        return direction;
    }
}
