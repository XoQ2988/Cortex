package me.xoq.cortex.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.xoq.cortex.event.block.BlockCollisionShapeEvent;
import me.xoq.cortex.event.EventBus;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockCollisionSpliterator.class)
public class BlockCollisionSpliteratorMixin {
    @WrapOperation(method = "computeNext",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/ShapeContext;" +
                            "getCollisionShape(Lnet/minecraft/block/BlockState;" +
                            "Lnet/minecraft/world/CollisionView;" +
                            "Lnet/minecraft/util/math/BlockPos;)" +
                            "Lnet/minecraft/util/shape/VoxelShape;"
            )
    )
    private VoxelShape onComputeNextCollisionBox(
            ShapeContext context,
            BlockState state,
            CollisionView world,
            BlockPos pos,
            Operation<VoxelShape> original
    ) {
        VoxelShape defaultShape = original.call(context, state, world, pos);

        BlockCollisionShapeEvent event = new BlockCollisionShapeEvent(state, world, pos);
        EventBus.fire(event);

        if (event.hasOverride()) return event.getOverride();

        return defaultShape;
    }
}
