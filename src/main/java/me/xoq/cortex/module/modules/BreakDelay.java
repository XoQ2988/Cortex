package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockBreakingCooldownEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.IntSetting;
import me.xoq.cortex.setting.Setting;

public class BreakDelay extends Module {
    public BreakDelay() {
        super("break-delay", "Changes the delay between block breaking");
    }

    private final Setting<Integer> delay = registerSetting(
            new IntSetting.Builder()
                    .name("delay-ticks")
                    .description("How many ticks to wait between block‐breaking attempts (vanilla = 4)")
                    .defaultValue(0)
                    .min(0).max(20)
                    .build()
    );

    @EventListener
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        if (!isEnabled()) return;

        event.setCooldown(delay.get());
    }
}
