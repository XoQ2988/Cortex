package me.xoq.cortex.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.xoq.cortex.command.Command;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.Config;
import net.minecraft.command.CommandSource;

import java.util.Optional;

public class SettingsCommand extends Command {
    public SettingsCommand() {
        super("setting", "View or change a module's setting");
    }

    // Suggest module names
    private static final SuggestionProvider<CommandSource> MODULE_SUGGESTER =
            (ctx, sb) -> {
                Modules.getModules().stream()
                        .map(Module::getName)
                        .forEach(sb::suggest);
                return sb.buildFuture();
            };


    // Suggest setting keys for the given module
    private static final SuggestionProvider<CommandSource> SETTING_SUGGESTER =
            (ctx, sb) -> {
                String name = StringArgumentType.getString(ctx, "module");
                Module module = Modules.get(name);
                if (module != null) {
                    module.getSettings().stream()
                            .map(Setting::getName)
                            .forEach(sb::suggest);
                }
                return sb.buildFuture();
            };

    // Suggest possible values
    private static final SuggestionProvider<CommandSource> VALUE_SUGGESTER =
            (ctx, sb) -> {
                String name = StringArgumentType.getString(ctx, "module");
                String setting = StringArgumentType.getString(ctx, "setting");
                Module module = Modules.get(name);
                if (module != null) {
                    Optional<Setting<?>> opt = module.getSettings().stream()
                            .filter(s -> s.getName().equals(setting))
                            .findFirst();
                    opt.ifPresent(s -> s.getSuggestions().forEach(sb::suggest));
                }
                return sb.buildFuture();
            };

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("module", StringArgumentType.word())
                        .suggests(MODULE_SUGGESTER)
                        // .setting <module>
                        .executes(this::listAll)
                        // .setting <module> reset
                        .then(literal("reset")
                                .executes(this::resetAll))
                        // .setting <module> <setting>
                        .then(argument("setting", StringArgumentType.word())
                                .suggests(SETTING_SUGGESTER)
                                .executes(this::viewOne)
                                // .setting <module> <setting> reset>
                                .then(literal("reset")
                                        .executes(this::resetOne)
                                )
                                // .setting <module> <setting> <value>
                                .then(argument("value", StringArgumentType.word())
                                        .suggests(VALUE_SUGGESTER)
                                        .executes(this::setOne)
                                )
                        )
                );
    }

    private int listAll(CommandContext<CommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "module");
        Module module = Modules.get(name);

        if (module == null) {
            ChatUtils.error("Unknown module: " + name);
        } else if (module.getSettings().isEmpty()) {
            ChatUtils.info(module.getTitle() + " does not have any settings.");
        } else {
            ChatUtils.info("Settings for " + module.getTitle() + ":");
            for (Setting<?> setting : module.getSettings()) {
                ChatUtils.info("§b" + setting.getTitle() + "§r [§f" + setting.get().toString() + "§r] §e" + setting.getDescription());
            }
        }
        return SINGLE_SUCCESS;
    }

    private int resetAll(CommandContext<CommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "module");
        Module module = Modules.get(name);

        if (module == null) ChatUtils.error("Unknown module: " + name);
        else {
            module.getSettings().forEach(Setting::resetToDefault);
            Config.save();
            ChatUtils.info("Reset all settings for " + module.getTitle());
        }

        return SINGLE_SUCCESS;
    }

    private int viewOne(CommandContext<CommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "module");
        String setting = StringArgumentType.getString(ctx, "setting");
        Module module = Modules.get(name);
        if (module == null) {
            ChatUtils.error("Unknown module: " + name);
        } else {
            Optional<Setting<?>> opt = module.getSettings().stream()
                    .filter(s -> s.getName().equals(setting)).findFirst();
            if (opt.isEmpty()) {
                ChatUtils.error("Unknown setting: " + setting);
            } else {
                Setting<?> s = opt.get();
                Config.save();
                ChatUtils.info(String.format("%s = %s",
                        s.getName(), s.get().toString()));
            }
        }
        return SINGLE_SUCCESS;
    }

    private int resetOne(CommandContext<CommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "module");
        String setting = StringArgumentType.getString(ctx, "setting");
        Module module = Modules.get(name);

        if (module == null) {
            ChatUtils.error("Unknown module: " + name);
        } else {
            Optional<Setting<?>> opt = module.getSettings().stream()
                    .filter(s -> s.getName().equals(setting))
                    .findFirst();

            if (opt.isEmpty()) {
                ChatUtils.error("Unknown setting: " + setting);
            } else {
                Setting<?> s = opt.get();
                s.resetToDefault();
                Config.save();
                ChatUtils.info("Reset " + s.getName() + " to default: " + s.get());
            }
        }
        return SINGLE_SUCCESS;
    }

    @SuppressWarnings({"unchecked", "SameReturnValue"})
    private int setOne(CommandContext<CommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "module");
        String settingName = StringArgumentType.getString(ctx, "setting");
        String rawValue = StringArgumentType.getString(ctx, "value");
        Module module = Modules.get(name);

        if (module == null) {
            ChatUtils.error("Unknown module: " + name);
            return SINGLE_SUCCESS;
        }

        Optional<Setting<?>> opt = module.getSettings().stream()
                .filter(s -> s.getName().equals(settingName))
                .findFirst();

        if (opt.isEmpty()) {
            ChatUtils.error("Unknown setting: " + settingName);
            return SINGLE_SUCCESS;
        }

        Setting<Object> setting = (Setting<Object>)opt.get();
        try {
            Object parsed = setting.parseValue(rawValue);
            setting.set(parsed);
            Config.save();
            ChatUtils.info("Set " + setting.getName() + " = " + setting.get());
        } catch (IllegalArgumentException iae) {
            ChatUtils.error(
                    "Invalid value for " + setting.getName() + ": " + iae.getMessage()
            );
        }

        return SINGLE_SUCCESS;
    }
}
