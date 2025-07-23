package me.xoq.cortex.mixin;

import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.misc.OpenScreenEvent;
import me.xoq.cortex.event.misc.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    /** Shadow the currentScreen field so we can read it*/
    @Shadow @Nullable public Screen currentScreen;

    /**
     * Temporarily holds the previous screen across the setScreen call,
     * so that we can fire Post with both previous & new.
     */
    private Screen cortex$previousScreen;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo ci) {
        EventBus.fire(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        EventBus.fire(new TickEvent.Post());
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreenPre(Screen screen, CallbackInfo ci) {
        // stash old screen
        this.cortex$previousScreen = this.currentScreen;

        // fire cancellable Pre
        OpenScreenEvent.Pre pre = new OpenScreenEvent.Pre(screen);
        EventBus.fire(pre);
        if (pre.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    private void onOpenScreenPost(Screen screen, CallbackInfo ci) {
        // fire non-cancellable Post with both old & new
        EventBus.fire(new OpenScreenEvent.Post(screen, this.cortex$previousScreen));
    }
}
