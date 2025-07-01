package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EntityAttackEvent;
import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.utils.ChatUtils;
import net.minecraft.entity.passive.VillagerEntity;
import org.lwjgl.glfw.GLFW;

public class ProtectVillager extends Module {
    public ProtectVillager() {
        super("ProtectVillager", "Prevents you from hitting villagers");
        setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
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
