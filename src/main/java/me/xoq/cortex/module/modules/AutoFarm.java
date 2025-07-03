package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockBreakEvent;
import me.xoq.cortex.event.misc.TickEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.IntSetting;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.InventoryUtils;
import me.xoq.cortex.util.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

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

    private static class Task { BlockPos pos; int ticks; CropType type; int oldSlot; }
    private final Queue<Task> queue = new ArrayDeque<>();

    public enum CropType {
        ALL("All", null, null),
        WHEAT("Wheat", Items.WHEAT_SEEDS, (CropBlock) Blocks.WHEAT),
        CARROT("Carrot", Items.CARROT, (CropBlock) Blocks.CARROTS),
        POTATO("Potato", Items.POTATO, (CropBlock) Blocks.POTATOES);

        public final String title;
        public final Item seedItem;
        public final CropBlock cropBlock;

        CropType(String title, Item seedItem, CropBlock cropBlock) {
            this.title = title;
            this.seedItem = seedItem;
            this.cropBlock = cropBlock;
        }

        public boolean matches(CropBlock crop) {
            if (this == ALL) return true;
            return crop == this.cropBlock;
        }

        public static CropType fromBlock(CropBlock crop) {
            for (CropType type : values()) {
                if (type != ALL && type.matches(crop)) return type;
            }
            return ALL;
        }
    }

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

        CropType typePicked = CropType.fromBlock(crop);

        Task task = new Task();
        task.pos = pos;
        task.type = typePicked;
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

        Item toPlant = currentTask.type.seedItem;

        if (toPlant == null) return;

        int seedSlot = InventoryUtils.getHotbarStacks().stream()
                .map(ItemStack::getItem)
                .toList()
                .indexOf(toPlant);

        if (seedSlot < 0) return;  // no seed item?

        InventoryUtils.setSelectedHotbarSlot(seedSlot);
        Utils.place(currentTask.pos, seedSlot);
        InventoryUtils.setSelectedHotbarSlot(currentTask.oldSlot);
    }
}
