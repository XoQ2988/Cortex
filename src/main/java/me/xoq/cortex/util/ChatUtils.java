package me.xoq.cortex.util;

import com.mojang.brigadier.StringReader;
import me.xoq.cortex.CortexClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static me.xoq.cortex.CortexClient.mc;

public class ChatUtils {
    private ChatUtils() { }

    private static final MutableText PREFIX = Text
            .literal("[").formatted(Formatting.DARK_GRAY)
            .append(Text.literal(CortexClient.MOD_META.getName()).formatted(Formatting.DARK_AQUA))
            .append(Text.literal("] ").formatted(Formatting.DARK_GRAY));

    public static void info(String msg) {
        send(msg, Formatting.GRAY);
    }

    public static void warn(String msg) {
        send(msg, Formatting.YELLOW);
    }

    public static void error(String msg) {
        send(msg, Formatting.RED);
    }

    /** Low-level send: push a new component into the in-game chat HUD. */
    private static void send(String content, Formatting color) {
        if (mc.player == null) return;
        MutableText msg = PREFIX.copy();

        msg.append(parseFormatting(content, color));
        mc.inGameHud.getChatHud().addMessage(msg);
    }

    public static MutableText parseFormatting(String content, Formatting defaultColor) {
        StringReader reader = new StringReader(content);
        MutableText text = Text.empty();
        Style style = Style.EMPTY.withFormatting(defaultColor);
        StringBuilder builder = new StringBuilder();

        while (reader.canRead()) {
            char character = reader.read();
            if (character == '§' && reader.canRead()) {
                // flush buffered text
                if (!builder.isEmpty()) {
                    text.append(Text.literal(builder.toString()).setStyle(style));
                    builder.setLength(0);
                }
                // apply new code
                char code = reader.read();
                Formatting formatting = Formatting.byCode(code);
                if (formatting != null) {
                    style = Style.EMPTY.withFormatting(formatting);
                } else {
                    // unknown code, treat literally
                    builder.append('§').append(code);
                }
            } else {
                builder.append(character);
            }
        }
        // flush remainder
        if (!builder.isEmpty()) text.append(Text.literal(builder.toString()).setStyle(style));
        return text;
    }
}
