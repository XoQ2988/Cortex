package me.xoq.cortex.mixin;

import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.block.BlockAttackEvent;
import me.xoq.cortex.event.block.BlockBreakEvent;
import me.xoq.cortex.event.block.BlockBreakingCooldownEvent;
import me.xoq.cortex.event.block.BlockInteractEvent;
import me.xoq.cortex.event.entity.EntityAttackEvent;
import me.xoq.cortex.event.entity.EntityInteractEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow private int blockBreakingCooldown;

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BlockAttackEvent evt = new BlockAttackEvent(pos, direction);
        EventBus.fire(evt);
        if (evt.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakEvent evt = new BlockBreakEvent(pos);
        EventBus.fire(evt);
        if (evt.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        BlockInteractEvent evt = new BlockInteractEvent(player, hand, hitResult);
        EventBus.fire(evt);
        if (evt.isCancelled()) {
            cir.cancel();
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        EntityAttackEvent evt = new EntityAttackEvent(player, target);
        EventBus.fire(evt);
        if (evt.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        EntityInteractEvent evt = new EntityInteractEvent(player, entity, hand);
        EventBus.fire(evt);
        if (evt.isCancelled()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Redirect(
            method = "updateBlockBreakingProgress",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 2
            )
    )
    private void onSurvivalBreakDelayChange(ClientPlayerInteractionManager interactionManager, int value) {
        BlockBreakingCooldownEvent event = new BlockBreakingCooldownEvent(value);
        EventBus.fire(event);

        this.blockBreakingCooldown = event.getCooldown();
    }
}
