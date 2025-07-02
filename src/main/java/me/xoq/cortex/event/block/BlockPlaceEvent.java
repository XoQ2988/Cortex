package me.xoq.cortex.event.block;

import me.xoq.cortex.event.CancellableEvent;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;

public class BlockPlaceEvent extends CancellableEvent {
    private final ItemPlacementContext context;
    private final BlockState state;

    public BlockPlaceEvent(ItemPlacementContext context, BlockState state) {
        this.context = context;
        this.state = state;
    }

    public ItemPlacementContext getContext() {
        return context;
    }

    public BlockState getState() {
        return state;
    }
}
