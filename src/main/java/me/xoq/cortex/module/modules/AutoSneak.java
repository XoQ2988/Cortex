package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.block.BlockPlaceEvent;
import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.misc.TickEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.BoolSetting;
import me.xoq.cortex.setting.IntSetting;
import me.xoq.cortex.setting.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;

import static me.xoq.cortex.CortexClient.mc;

public class AutoSneak extends Module {
    private final Setting<Integer> scanRadius = registerSetting(
            new IntSetting.Builder()
                    .name("scan-radius")
                    .description("Blocks around your feet to check for nearby ground (0 = only under you)")
                    .defaultValue(1)
                    .min(0).max(5)
                    .build()
    );

    private final Setting<Integer> releaseDelay = registerSetting(
            new IntSetting.Builder()
                    .name("release-delay")
                    .description("Ticks to wait after placing a block before releasing sneak")
                    .defaultValue(2)
                    .min(0).max(20)
                    .build()
    );

    private final Setting<Boolean> onlyOnGround = registerSetting(
            new BoolSetting.Builder()
                    .name("only-on-ground")
                    .description("Only auto‐sneak when you are touching ground")
                    .defaultValue(true)
                    .build()
    );

    private KeyBinding sneakKey;
    private boolean wasSneaking;
    private int pendingRelease;

    public AutoSneak() {
        super("auto-sneak", "Auto-hold sneak when you're dangling over an edge");
    }

    @Override
    protected void onEnable() {
        wasSneaking = false;
        pendingRelease = -1;

        DebugHUD.registerLine(getTitle(), this::getSneaking);
    }

    @Override
    protected void onDisable() {
        pendingRelease = -1;
        releaseSneak();

        DebugHUD.unregisterLine(getTitle());
    }

    @EventListener
    private void onTick(TickEvent.Post event) {
        if (!isEnabled() || mc.player == null || mc.world == null ) return;
        if (sneakKey == null) sneakKey = mc.options.sneakKey;

        var player = mc.player;
        var world  = mc.world;

        // handle delayed release
        if (pendingRelease >= 0) {
            if (pendingRelease-- <= 0) {
                releaseSneak();
                pendingRelease = -1;
            }
            return;
        }

        // only‐on‐ground guard
        if (onlyOnGround.get() && !mc.player.isOnGround()) {
            releaseSneak();
            return;
        }

        // determine if any block in a square radius is solid
        BlockPos feet = player.getBlockPos().down();
        int r = scanRadius.get();
        boolean anyGround = false;
        for (int dx = -r; dx <= r && !anyGround; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                BlockState bs = world.getBlockState(feet.add(dx, 0, dz));
                if (!bs.isAir()) {
                    anyGround = true;
                    break;
                }
            }
        }

        if (anyGround && world.isAir(feet)) engageSneak();
        else releaseSneak();
    }

    @EventListener
    private void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled() || mc.player == null || mc.world == null ) return;

        BlockPos feet = mc.player.getBlockPos().down();
        if (event.getContext().getBlockPos().equals(feet)) {
            pendingRelease = releaseDelay.get();
        }
    }

    private void engageSneak() {
        if (!wasSneaking) {
            sneakKey.setPressed(true);
            wasSneaking = true;
        }
    }

    private void releaseSneak() {
        if (wasSneaking) {
            sneakKey.setPressed(false);
            wasSneaking = false;
        }
    }

    private String getSneaking() {
        if (pendingRelease > 0) {
            return "Releasing in " + pendingRelease + "t";
        } else if (pendingRelease == 0) {
            return "Releasing now";
        } else if (wasSneaking) {
            return "Sneaking";
        } else {
            return "Idle";
        }
    }
}
