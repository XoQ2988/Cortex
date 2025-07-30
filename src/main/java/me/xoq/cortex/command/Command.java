package me.xoq.cortex.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.xoq.cortex.util.Utils;
import net.minecraft.command.CommandSource;

import java.util.List;

public abstract class Command {
    protected static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

    private final String name;
    private final String title;
    private final String description;
    private final List<String> aliases;

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.aliases = List.of(aliases);
    }

    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument(name, argumentType);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal( String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public final void registerTo(CommandDispatcher<CommandSource> commandDispatcher) {
        register(commandDispatcher, name);
    }

    public void register(CommandDispatcher<CommandSource> commandDispatcher, String alias) {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(alias);
        build(builder);
        commandDispatcher.register(builder);
    }

    public abstract void build(LiteralArgumentBuilder<CommandSource> builder);

    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getAliases() { return aliases; }
}
