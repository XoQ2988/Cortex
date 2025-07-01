package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.BlockAttackEvent;
import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.BoolSetting;
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

    private static float getScore(BlockState state, ItemStack stack) {
        if (stack.isEmpty()) return 1.0f;  // base hand speed
        if (stack.isDamageable() && !stack.isSuitableFor(state)) return -1.0f;  // avoid damaging tools not fit

        float base = stack.getMiningSpeedMultiplier(state);
        int effLevel = InventoryUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);

        if (effLevel > 0) base += (effLevel * effLevel + 1) * 0.5f; // Efficiency adds (level^2 + 1) / 2

        return base;
    }
}
