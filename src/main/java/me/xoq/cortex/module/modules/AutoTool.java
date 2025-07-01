package me.xoq.cortex.module.modules;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import me.xoq.cortex.event.BlockAttackEvent;
import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.util.ChatUtils;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

import static me.xoq.cortex.CortexClient.mc;

public class AutoTool extends Module {
    public AutoTool() {
        super("auto-tool", "Automatically switches to the most effective tool when performing a task");
    }

    @EventListener
    private void onBlockAttack(BlockAttackEvent event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos blockPos = event.getPos();
        BlockState blockState = mc.world.getBlockState(blockPos);

        int currentSlot = mc.player.getInventory().getSelectedSlot();
        ItemStack currentStack = mc.player.getInventory().getStack(currentSlot);
        float currentScore = getScore(blockState, currentStack);

        int bestSlot = currentSlot;
        ItemStack bestStack = currentStack;
        float bestScore = currentScore;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            float score = getScore(blockState, stack);

            ChatUtils.info("Slot §9" + (slot + 1) + "§r §6" + score + "§r.");

            if (score > bestScore) {
                bestSlot = slot;
                bestStack = stack;
                bestScore = score;
            }
        }

        ChatUtils.info("Best slot: §6" + (bestSlot + 1) + "§r.");
        if (bestSlot != mc.player.getInventory().getSelectedSlot()) {
            mc.player.getInventory().setSelectedSlot(bestSlot);
        }
    }

    private static float getScore(BlockState state, ItemStack stack) {
        if (stack.isEmpty()) return 1.0f;  // base hand speed
        if (stack.isDamageable() && !stack.isSuitableFor(state)) return -1.0f;  // avoid damaging tools not fit

        float base = stack.getMiningSpeedMultiplier(state);

        return base;
    }
}
