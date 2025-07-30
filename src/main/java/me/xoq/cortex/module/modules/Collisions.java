package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockCollisionShapeEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.BoolSetting;
import me.xoq.cortex.setting.Setting;
import net.minecraft.block.*;
import net.minecraft.util.shape.VoxelShapes;

import static me.xoq.cortex.CortexClient.mc;

public class Collisions extends Module {
    public Collisions() {
        super("collisions", "");
    }

    private final Setting<Boolean> onlyOnGround = registerSetting(
            new BoolSetting.Builder()
                    .name("on-ground")
                    .description("Disables collision when not on ground")
                    .defaultValue(true)
                    .build()
    );

    private final Setting<Boolean> fire    = registerSetting(settingBool("fire")            .build());
    private final Setting<Boolean> cactus  = registerSetting(settingBool("cactus")          .build());
    private final Setting<Boolean> cobweb  = registerSetting(settingBool("cobweb")          .build());
    private final Setting<Boolean> pressure= registerSetting(settingBool("pressure-plate")  .build());
    private final Setting<Boolean> tripwire= registerSetting(settingBool("tripwire")        .build());
    private final Setting<Boolean> campfire= registerSetting(settingBool("campfire")        .build());
    private final Setting<Boolean> berry   = registerSetting(settingBool("sweet-berry-bush").build());
    private final Setting<Boolean> powder  = registerSetting(settingBool("powder-snow")     .build());
    private final Setting<Boolean> honey   = registerSetting(settingBool("honey-block")     .build());

    private static BoolSetting.Builder settingBool(String name) {
        return new BoolSetting.Builder()
                .name(name)
                .description("Protect sides of " + name.replace('-', ' '))
                .defaultValue(true);
    }

    @EventListener
    private void onCollision(BlockCollisionShapeEvent event) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        if (!event.getState().getFluidState().isEmpty()) return;
        if (onlyOnGround.get() && !mc.player.isOnGround()) return;

        BlockState state = event.getState();
        Block block = state.getBlock();

        boolean blockFire     = fire    .get() && block instanceof AbstractFireBlock;
        boolean blockCactus   = cactus  .get() && block instanceof CactusBlock;
        boolean blockCobweb   = cobweb  .get() && block instanceof CobwebBlock;
        boolean blockPressure = pressure.get() && block instanceof AbstractPressurePlateBlock;
        boolean blockTripwire = tripwire.get() && block instanceof TripwireBlock;
        boolean blockCampfire = campfire.get() && block instanceof CampfireBlock;
        boolean blockBerry    = berry   .get() && block instanceof SweetBerryBushBlock;
        boolean blockPowder   = powder  .get() && block instanceof PowderSnowBlock;
        boolean blockHoney    = honey   .get() && block instanceof HoneyBlock;

        if (blockFire || blockCactus || blockCobweb || blockPressure || blockTripwire
                || blockCampfire ||blockBerry || blockPowder || blockHoney) {
            event.setShape(VoxelShapes.fullCube());
        }
    }
}
