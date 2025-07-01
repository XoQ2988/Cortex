package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EntityAttackEvent;
import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.utils.ChatUtils;
import net.minecraft.entity.passive.VillagerEntity;

public class ProtectVillager extends Module {
    public ProtectVillager() {
        super("ProtectVillager", "Prevents you from hitting villagers");
    }

    @Override
    protected void onEnable() {
        ChatUtils.info("Now protecting villagers");
    }

    @Override
    protected void onDisable() {
        ChatUtils.warn("No longer protecting villagers!");
    }

    @EventListener
    private void onEntityAttack(EntityAttackEvent event) {
        if (event.getTarget() instanceof VillagerEntity) {
            event.cancel();
            ChatUtils.warn("Protected " + event.getTarget().getName().getString());
        }
    }
}
