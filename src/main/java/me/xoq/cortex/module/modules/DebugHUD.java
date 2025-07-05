package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.misc.Render2DEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.EnumSetting;
import me.xoq.cortex.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static me.xoq.cortex.CortexClient.mc;

public class DebugHUD extends Module {
    public DebugHUD() {
        super("debug-hud", "Displays debug info lines");
    }

    private final Setting<Formatting> primaryColor = registerSetting(
            new EnumSetting.Builder<Formatting>()
                    .name("primary-color")
                    .enumClass(Formatting.class)
                    .defaultValue(Formatting.DARK_AQUA)
                    .build()
    );

    private final Setting<Formatting> secondaryColor = registerSetting(
            new EnumSetting.Builder<Formatting>()
                    .name("secondary-color")
                    .enumClass(Formatting.class)
                    .defaultValue(Formatting.WHITE)
                    .build()
    );

    private static final Map<String, Supplier<String>> lines = new LinkedHashMap<>();

    public static void registerLine(String name, Supplier<String> supplier) {
        lines.put(name, supplier);
    }

    public static void unregisterLine(String name) {
        lines.remove(name);
    }

    @EventListener
    private void onRender(Render2DEvent event) {
        if (mc.player == null) return;
        DrawContext context = event.getContext();

        int x = 5;
        int y = 5;
        int lineHeight = mc.textRenderer.fontHeight + 2;

        for (Map.Entry<String, Supplier<String>> entry : lines.entrySet()) {
            String name = entry.getKey();
            String val = entry.getValue().get();

            if (val == null) continue;

            MutableText text = Text.literal(name + " ").formatted(primaryColor.get())
                    .append(Text.literal(val).formatted(secondaryColor.get()));

            context.drawText(mc.textRenderer, text.asOrderedText(), x, y, 0xFFFFFFFF, false);
            y += lineHeight;
        }
    }
}
