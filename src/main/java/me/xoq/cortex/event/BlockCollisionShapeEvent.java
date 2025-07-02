package me.xoq.cortex.event;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class BlockCollisionShapeEvent {
    private final BlockState state;
    private final BlockView world;
    private final BlockPos pos;
    private VoxelShape overrideShape;

    public BlockCollisionShapeEvent(BlockState state, BlockView world, BlockPos pos) {
        this.state         = state;
        this.world         = world;
        this.pos           = pos;
        this.overrideShape = null;
    }

    public BlockState getState()       { return state; }
    public BlockView  getWorld()       { return world; }
    public BlockPos   getPos()         { return pos; }

    public void setShape(VoxelShape shape) {
        this.overrideShape = shape;
    }

    public boolean hasOverride()       { return overrideShape != null; }
    public VoxelShape getOverride()    { return overrideShape; }
}
