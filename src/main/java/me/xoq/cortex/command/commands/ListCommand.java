package me.xoq.cortex.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.xoq.cortex.command.Command;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.util.ChatUtils;
import net.minecraft.command.CommandSource;

import java.util.Collection;

public class ListCommand extends Command {
    public ListCommand() {
        super("list", "List all modules and their states");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSource> context) {
        Collection<Module> all = Modules.getModules();

        ChatUtils.info("§eModules (" + all.size() + "):");
        for (Module module : all) {
            ChatUtils.info("§7[ " + (module.isEnabled() ? "§a ON " : "§cOFF") + " §7]" + " §6" + module.getTitle() + "§7");
        }
        return SINGLE_SUCCESS;
    }
}
