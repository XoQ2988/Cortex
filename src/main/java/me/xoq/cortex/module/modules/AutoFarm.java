package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockBreakEvent;
import me.xoq.cortex.event.misc.TickEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.IntSetting;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.InventoryUtils;
import net.minecraft.block.CropBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Queue;

import static me.xoq.cortex.CortexClient.mc;

public class AutoFarm extends Module {
    public AutoFarm() {
        super("auto-farm", "Automatically replant seeds when you harvest grown crops");


    }

    private final Setting<Integer> delay = registerSetting(
            new IntSetting.Builder()
                    .name("delay-ticks")
                    .description("Ticks to wait after breaking before replanting")
                    .defaultValue(1).min(0).max(5)
                    .build()
    );

    private static class Task { BlockPos pos; int ticks; int oldSlot; }
    private final Queue<Task> queue = new ArrayDeque<>();

    @Override
    protected void onEnable() {
        queue.clear();
    }

    @EventListener
    private void onBlockBreak(BlockBreakEvent event) {
        if (mc.player == null || mc.world == null) return;

        var pos = event.getPos();
        var state = mc.world.getBlockState(pos);

        if (!(state.getBlock() instanceof CropBlock crop)) return;

        Task task = new Task();
        task.pos = pos;
        task.ticks = delay.get();
        task.oldSlot = InventoryUtils.getSelectedHotbarSlot();
        queue.add(task);
    }

    @EventListener
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (queue.isEmpty()) return;

        Task currentTask = queue.peek();
        if (currentTask.ticks-- > 0) return;

        queue.remove();

        int seedSlot = InventoryUtils.getHotbarStacks().stream()
                .map(ItemStack::getItem)
                .toList()
                .indexOf(Items.WHEAT_SEEDS);

        if (seedSlot < 0) return;  // no seeds?

        InventoryUtils.setSelectedHotbarSlot(seedSlot);

        BlockPos farmlandPos = currentTask.pos;
        ChatUtils.info(String.format(
                "AutoFarm: t=%d, planting at %s",
                currentTask.ticks, farmlandPos
        ));
        
        place(farmlandPos, seedSlot);

        InventoryUtils.setSelectedHotbarSlot(currentTask.oldSlot);
    }

    public static void place(BlockPos blockPos, int slot) {
        if (mc.player == null) return;

        Vec3d hitPos = Vec3d.ofCenter(blockPos);
        Direction side = Direction.UP;

        BlockHitResult hitResult = new BlockHitResult(hitPos, side, blockPos, false);

        InventoryUtils.setSelectedHotbarSlot(slot);
        interact(hitResult);
    }

    public static void interact(BlockHitResult blockHitResult) {
        if (mc.player == null || mc.interactionManager == null) return;

        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);

        if (result.isAccepted())
            mc.player.swingHand(Hand.MAIN_HAND);
    }
}
