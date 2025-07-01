package me.xoq.cortex.mixin;

import me.xoq.cortex.event.BlockBreakingEvent;
import me.xoq.cortex.event.EventBus;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        // Create a fresh event for this break action
        BlockBreakingEvent event = new BlockBreakingEvent(pos, direction);

        // Dispatch to all listeners
        EventBus.fire(event);

        // If any listener cancelled it, prevent the break
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
