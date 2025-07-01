package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.BlockAttackEvent;
import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.BoolSetting;
import me.xoq.cortex.setting.IntSetting;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static me.xoq.cortex.CortexClient.mc;

public class AutoTool extends Module {
    public AutoTool() {
        super("auto-tool", "Automatically switches to the most effective tool when performing a task");
    }

    private final Setting<Boolean> debug = registerSetting(
            new BoolSetting.Builder()
                    .name("debug")
                    .description("Whether or not to log debug information to chat")
                    .defaultValue(false)
                    .build()
    );

    private final Setting<Integer> minDurabilityPercent = registerSetting(
            new IntSetting.Builder()
                    .name("min-durability")
                    .description("Don't auto-switch to tools below this durability percentage")
                    .defaultValue(10)
                    .min(0)
                    .max(100)
                    .build()
    );

    @EventListener
    private void onBlockAttack(BlockAttackEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isCreative()) return;

        BlockPos blockPos = event.getPos();
        BlockState blockState = mc.world.getBlockState(blockPos);

        // grab hotbar stacks and current slot
        List<ItemStack> hotbar = InventoryUtils.getHotbarStacks();
        int currentSlot = InventoryUtils.getSelectedHotbarSlot();
        ItemStack currentStack = hotbar.get(currentSlot);
        float currentScore = getScore(blockState, currentStack);

        // find the best slot
        int bestSlot = currentSlot;
        float bestScore = currentScore;
        for (int slot = 0; slot < hotbar.size(); slot++) {
            float score = getScore(blockState, hotbar.get(slot));
            if (debug.get()) ChatUtils.info("Slot §9" + (slot + 1) + "§r §6" + score + "§r.");
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }

        if (debug.get()) ChatUtils.info("Slot §6" + (bestSlot + 1) + "§r");
        if (bestSlot != mc.player.getInventory().getSelectedSlot()) {
            InventoryUtils.setSelectedHotbarSlot(bestSlot);
        }
    }

    private float getScore(BlockState state, ItemStack stack) {
        float handScore = 1.1f;  // empty hand speed, slightly higher than with holding items, fallback

        // if stack empty, use hand
        if (stack.isEmpty()) return handScore;

        if (stack.isDamageable()) {
            // don't use wrong tool
            if (!stack.isSuitableFor(state)) return 0.0f;

            // if correct tool, check durability
            int max = stack.getMaxDamage();
            int dmg = stack.getDamage();
            int pct = Math.round((float)(max - dmg) * 100f / max);

            int minPct = minDurabilityPercent.get();
            if (pct < minPct) {
                return -handScore;
            }
        }

        // compute base speed + optional efficiency bonus
        float base = stack.getMiningSpeedMultiplier(state);
        int effLevel = InventoryUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);

        if (effLevel > 0) base += (effLevel * effLevel + 1) * 0.5f; // Efficiency adds (level^2 + 1) / 2

        return base;
    }
}
