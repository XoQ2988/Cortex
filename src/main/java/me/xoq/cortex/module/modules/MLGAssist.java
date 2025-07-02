package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.TickEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.IntSetting;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;
import java.util.stream.IntStream;

import static me.xoq.cortex.CortexClient.mc;

public class MLGAssist extends Module {
    public MLGAssist() {
        super("mlg-assist", "Automatically switch to water bucket for MLG when falling");
    }

    private final Setting<Integer> fallDistance = registerSetting(
            new IntSetting.Builder()
                    .name("fall-distance")
                    .description("Minimum fall distance (blocks) to trigger")
                    .defaultValue(4).min(1).max(255)
                    .build()
    );

    private final Setting<Integer> pitchThreshold = registerSetting(
            new IntSetting.Builder()
                    .name("pitch-threshold")
                    .description("Minimum look-down pitch (degrees) to trigger")
                    .defaultValue(60).min(0).max(90)
                    .build()
    );

    private boolean hasSwapped;

    @Override
    protected void onEnable() {
        hasSwapped = false;
    }

    @EventListener
    private void onTick(TickEvent.Post event) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        float pitch = mc.player.getPitch();
        double fall = mc.player.fallDistance;
        boolean onGround = mc.player.isOnGround();

        if (!onGround && fall >= fallDistance.get() && pitch >= pitchThreshold.get() && !hasSwapped) {
            List<ItemStack> hotbar = InventoryUtils.getHotbarStacks();
            int waterSlot = IntStream.range(0, hotbar.size())
                    .filter(slot -> hotbar.get(slot).getItem() == Items.WATER_BUCKET)
                    .findFirst().orElse(-1);

            if (waterSlot >= 0) {
                InventoryUtils.setSelectedHotbarSlot(waterSlot);
                hasSwapped = true;
            }
        }
        if (onGround && hasSwapped) {
            hasSwapped = false;
        }
    }
}
