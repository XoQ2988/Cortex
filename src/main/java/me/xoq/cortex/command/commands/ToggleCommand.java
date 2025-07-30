package me.xoq.cortex.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.xoq.cortex.command.Command;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.util.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class ToggleCommand extends Command {
    private static final SuggestionProvider<CommandSource> MODULE_SUGGESTIONS =
            (CommandContext<CommandSource> ctx, SuggestionsBuilder builder) -> {
                Modules.getModules().stream().map(Module::getName).forEach(builder::suggest);
                return builder.buildFuture();
            };

    public ToggleCommand() {
        super("toggle", "Enable or disable a module", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("module", StringArgumentType.word())
                        .suggests(MODULE_SUGGESTIONS)
                        .executes(this::toggleModule)
                        .then(literal("on") .executes(context -> setModuleState(context, true)))
                        .then(literal("off").executes(context -> setModuleState(context, false)))
                );
    }

    private int toggleModule(CommandContext<CommandSource> context) {
        String name = getString(context, "module");
        Module module = Modules.get(name);

        if (module == null) ChatUtils.error("Unknown module: " + name);
        else module.toggle();

        return SINGLE_SUCCESS;
    }

    private int setModuleState(CommandContext<CommandSource> context, boolean enable) {
        String name = getString(context, "module");
        Module module = Modules.get(name);

        if (module == null) {
            ChatUtils.error("Unknown module: " + name);
        } else {
            if (enable) {
                if (!module.isEnabled()) module.toggle();
                else ChatUtils.warn(module.getTitle() + " is already on.");
            } else {
                if (module.isEnabled()) module.toggle();
                else ChatUtils.warn(module.getTitle() + " is already off.");
            }
        }

        return SINGLE_SUCCESS;
    }
}
