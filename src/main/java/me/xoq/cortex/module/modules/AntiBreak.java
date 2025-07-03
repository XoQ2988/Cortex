package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockAttackEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.IntSetting;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.ChatUtils;
import net.minecraft.item.ItemStack;

import static me.xoq.cortex.CortexClient.mc;

public class AntiBreak extends Module {
    public AntiBreak() {
        super("anti-break", "Prevents you from breaking your precious tools");
    }

    private final Setting<Integer> minDurabilityPct = registerSetting(
            new IntSetting.Builder()
                    .name("min-durability")
                    .description("Prevent breaking when tool is below this durability percentage")
                    .defaultValue(15)
                    .min(0).max(100)
                    .build()
    );

    private int lastPct;
    private boolean wouldBlock;

    @Override
    protected void onEnable() {
        lastPct = -1;
        wouldBlock = false;
    }

    @EventListener
    private void onBlockAttack(BlockAttackEvent event) {
        if (!isEnabled() || mc.player == null) return;

        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isDamageable()) {
            int max = stack.getMaxDamage();
            int damage = stack.getDamage();
            int remaining = max - damage;
            int pct = Math.round(remaining * 100f / max);

            wouldBlock = pct <= minDurabilityPct.get();

            if (wouldBlock) {
                // Cancel the break
                event.setCancelled(true);

                if (lastPct != pct) {
                    lastPct = pct;
                    ChatUtils.warn("AntiBreak: tool durability too low (" + pct + "%), break prevented.");
                }
            }
        }
    }

    @Override
    protected String getStatus() {
        if (!isEnabled() || mc.player == null) return null;

        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isDamageable()) {
            int max       = stack.getMaxDamage();
            int damage    = stack.getDamage();
            int remaining = max - damage;
            lastPct       = Math.round((remaining * 100f) / max);
            wouldBlock    = lastPct < minDurabilityPct.get();
        } else {
            lastPct    = -1;
            wouldBlock = false;
        }

        String decision = wouldBlock ? "Block" : "Allow";
        return String.format("%d%% > %s", lastPct, decision);
    }
}
