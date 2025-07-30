package me.xoq.cortex.command.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.xoq.cortex.command.Command;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.Utils;
import net.minecraft.command.CommandSource;

public class BindsCommand extends Command {
    public BindsCommand() {
        super("binds", "Show current key bindings for all modules", "keybinds", "kb");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSource> context) {
        ChatUtils.info("§eKeybindings:");
        for (Module module : Modules.getModules()) {
            ChatUtils.info(" - §6" + module.getTitle() + "§r : §a" + Utils.keyToString(module.getKeybind()) + "§r");
        }
        return SINGLE_SUCCESS;
    }
}