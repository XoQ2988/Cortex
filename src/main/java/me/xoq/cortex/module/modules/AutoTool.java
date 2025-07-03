package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockAttackEvent;
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

    private int lastSelectedSlot;
    private int candidateSlot;
    private float candidateScore;

    @Override
    protected void onEnable() {
        lastSelectedSlot = -1;
        candidateSlot = lastSelectedSlot;
        candidateScore = 0f;
    }

    @EventListener
    private void onBlockAttack(BlockAttackEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isCreative()) return;

        BlockPos blockPos = event.getPos();
        BlockState blockState = mc.world.getBlockState(blockPos);

        lastSelectedSlot = InventoryUtils.getSelectedHotbarSlot();

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
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }

        candidateSlot  = bestSlot;
        candidateScore = bestScore;

        if (bestSlot != mc.player.getInventory().getSelectedSlot()) {
            InventoryUtils.setSelectedHotbarSlot(bestSlot);
        }
    }

    private float getScore(BlockState state, ItemStack stack) {
        float handScore = 1.1f;  // empty hand speed, slightly higher than with holding items, fallback

        // if stack empty, use hand
        if (stack.isEmpty()) return handScore;

        // don't use wrong tool
        if (!stack.isSuitableFor(state)) return 0.0f;

        // compute base speed + optional efficiency bonus
        float base = stack.getMiningSpeedMultiplier(state);
        int effLevel = InventoryUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);

        if (effLevel > 0) base += (effLevel * effLevel + 1) * 0.5f; // Efficiency adds (level^2 + 1) / 2

        return base;
    }

    @Override
    protected String getStatus() {
        if (!isEnabled()) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("Slot ").append(candidateSlot + 1);
        sb.append(" (score ").append(String.format("%.1f", candidateScore)).append(")");

        if (candidateSlot != lastSelectedSlot) {
            sb.append(" (from  ").append(lastSelectedSlot + 1).append(")");
        }
        return sb.toString();
    }
}
