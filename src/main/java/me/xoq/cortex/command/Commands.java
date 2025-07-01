package me.xoq.cortex.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.util.ChatUtils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;


/**
 * Very basic command dispatcher
 */
public class Commands {
    public static final String PREFIX = ".";
    private static final CommandDispatcher<CommandContext> DISPATCHER = new CommandDispatcher<>();

    static {
        DISPATCHER.register(
                LiteralArgumentBuilder.<CommandContext>literal("list")
                        .executes(ctx -> {
                            ChatUtils.info("Modules:");
                            for (Module m : Modules.getModules()) {
                                ChatUtils.info(" - " + m.getTitle()
                                        + (m.isEnabled() ? " §aON" : " §cOFF"));
                            }
                            return SINGLE_SUCCESS;
                        })
        );
    }

    // fake "player" context with no fields
    public static class CommandContext { }

    public static void dispatch(String input) {
        try {
            DISPATCHER.execute(input, new CommandContext());
        } catch (Exception e) {
            ChatUtils.error("Command error: " + e.getMessage());
        }
    }
}
