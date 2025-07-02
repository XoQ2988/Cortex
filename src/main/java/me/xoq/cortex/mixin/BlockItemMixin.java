package me.xoq.cortex.mixin;

import me.xoq.cortex.event.block.BlockPlaceEvent;
import me.xoq.cortex.event.EventBus;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getWorld().isClient) return;

        BlockPlaceEvent evt = new BlockPlaceEvent(context, state);
        EventBus.fire(evt);
        if (evt.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
