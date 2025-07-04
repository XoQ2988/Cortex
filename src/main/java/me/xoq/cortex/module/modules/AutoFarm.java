package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockBreakEvent;
import me.xoq.cortex.event.misc.TickEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.EnumSetting;
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
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

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

    private final Setting<CropType> cropType = registerSetting(
            new EnumSetting.Builder<CropType>()
                    .name("crop")
                    .description("Which crop to auto-replant")
                    .enumClass(CropType.class)
                    .defaultValue(CropType.ALL)
                    .build()
    );

    private static class Task { BlockPos pos; int ticks; CropType type; int oldSlot; }
    private final Queue<Task> queue = new ArrayDeque<>();

    public enum CropType {
        ALL("All", null, null),
        WHEAT("Wheat", Items.WHEAT_SEEDS, (CropBlock) Blocks.WHEAT),
        CARROT("Carrot", Items.CARROT, (CropBlock) Blocks.CARROTS),
        POTATO("Potato", Items.POTATO, (CropBlock) Blocks.POTATOES),
        BEETROOT("Beetroot", Items.BEETROOT_SEEDS, (CropBlock) Blocks.BEETROOTS);

        public final String title;
        public final Item seedItem;
        public final CropBlock cropBlock;

        private static final Map<CropBlock, CropType> BY_BLOCK;
        static {
            BY_BLOCK = Arrays.stream(values())
                    .filter(t -> t.cropBlock != null)
                    .collect(Collectors.toMap(t -> t.cropBlock, t -> t));
        }

        CropType(String title, Item seedItem, CropBlock cropBlock) {
            this.title = title;
            this.seedItem = seedItem;
            this.cropBlock = cropBlock;
        }

        public static CropType fromBlock(CropBlock crop) {
            return BY_BLOCK.getOrDefault(crop, ALL);
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
        if (cropType.get() != CropType.ALL && cropType.get() != typePicked) return;

        Task task = new Task();
        task.pos = pos;
        task.type = typePicked;
        task.ticks = delay.get();
        task.oldSlot = InventoryUtils.getSelectedHotbarSlot();
        queue.add(task);
    }

    @EventListener
    private void onTick(TickEvent.Post event) {
        if (mc.player == null ||mc.world == null || mc.interactionManager == null) return;
        if (queue.isEmpty()) return;

        Task currentTask = queue.peek();
        if (currentTask.ticks-- > 0) return;

        queue.remove();

        Item toPlant = currentTask.type.seedItem != null
                ? currentTask.type.seedItem
                : CropType.fromBlock((CropBlock) mc.world.getBlockState(currentTask.pos).getBlock()).seedItem;

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
