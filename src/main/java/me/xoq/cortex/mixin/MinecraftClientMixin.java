package me.xoq.cortex.mixin;

import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo ci) {
        EventBus.fire(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        EventBus.fire(new TickEvent.Post());
    }
}
