package me.xoq.cortex.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.xoq.cortex.event.EventBus;
import me.xoq.cortex.event.ItemStackTooltipEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

import static me.xoq.cortex.CortexClient.mc;

@Mixin(net.minecraft.item.ItemStack.class)
public class ItemStackMixin {
    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> onGetTooltip(List<Text> original) {
        if (mc == null || mc.world == null || mc.player == null) return original;

        ItemStackTooltipEvent event = new ItemStackTooltipEvent((ItemStack)(Object)this, original);
        EventBus.fire(event);

        return event.getList();
    }
}
