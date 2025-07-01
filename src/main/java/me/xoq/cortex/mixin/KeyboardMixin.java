package me.xoq.cortex.mixin;

import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.KeyEvent;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        KeyEvent evt;

        switch (action) {
            case GLFW.GLFW_PRESS:
                evt = new KeyEvent.Press(window, key, scancode, modifiers);
                break;
            case GLFW.GLFW_RELEASE:
                evt = new KeyEvent.Release(window, key, scancode, modifiers);
                break;
            case GLFW.GLFW_REPEAT:
                evt = new KeyEvent.Repeat(window, key, scancode, modifiers);
                break;
            default:
                return; // ignore other actions
        }

        EventBus.fire(evt);
        if (evt.isCancelled()) {
            ci.cancel();
        }
    }
}
