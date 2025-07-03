package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.misc.Render2DEvent;
import me.xoq.cortex.module.Module;
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

    private static final Formatting PRIMARY   = Formatting.DARK_AQUA;
    private static final Formatting SECONDARY = Formatting.WHITE;

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
            String val  = entry.getValue().get();

            MutableText text = Text.literal(name + ": ").formatted(PRIMARY)
                    .append(Text.literal(val).formatted(SECONDARY));

            context.drawText(mc.textRenderer, text.asOrderedText(), x, y, 0xFFFFFFFF, false);
            y += lineHeight;
        }
    }
}
