package me.xoq.cortex.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.xoq.cortex.command.Command;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.util.ChatUtils;
import net.minecraft.command.CommandSource;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a module a key", "kb", "keybind");
    }

    private static final SuggestionProvider<CommandSource> MODULE_SUGGESTIONS = (context, builder) -> {
        Modules.getModules().forEach(module -> builder.suggest(module.getName()));
        return builder.buildFuture();
    };

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("module", StringArgumentType.word())
                        .suggests(MODULE_SUGGESTIONS)
                        .executes(this::execute)
                );
    }

    @SuppressWarnings("SameReturnValue")
    private int execute(CommandContext<CommandSource> context) {
        String name = StringArgumentType.getString(context, "module");
        Module module = Modules.get(name);

        if (module == null) ChatUtils.error("Module not found: " + name);
        else Modules.startBinding(module);

        return SINGLE_SUCCESS;
    }
}