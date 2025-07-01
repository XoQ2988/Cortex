package me.xoq.cortex.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.xoq.cortex.command.Command;
import me.xoq.cortex.command.Commands;
import me.xoq.cortex.util.ChatUtils;
import net.minecraft.command.CommandSource;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Show available commands");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSource> context) {
        ChatUtils.info("Commands:");
        for (Command command : Commands.getAll()) {
            ChatUtils.info(" - §6." + command.getName() + "§r " + command.getDescription());
        }
        return SINGLE_SUCCESS;
    }
}
