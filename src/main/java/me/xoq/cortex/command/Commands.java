package me.xoq.cortex.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.xoq.cortex.command.commands.*;
import me.xoq.cortex.util.ChatUtils;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.xoq.cortex.CortexClient.mc;


public class Commands {
    public static final String PREFIX = ".";
    private static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    private static final List<Command> COMMANDS = new ArrayList<>();

    public static void init() {
        register(new BindCommand());
        register(new BindsCommand());
        register(new HelpCommand());
        register(new ListCommand());
        register(new SettingsCommand());
        register(new ToggleCommand());
    }

    private static void register(Command command) {
        command.registerTo(DISPATCHER);
        for (String alias : command.getAliases()) {
            command.register(DISPATCHER, alias);
        }
        COMMANDS.add(command);
    }

    public static CommandDispatcher<CommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public static List<Command> getAll() {
        return Collections.unmodifiableList(COMMANDS);
    }

    public static void dispatch(String message) {
        CommandSource src = Objects.requireNonNull(mc.getNetworkHandler()).getCommandSource();
        try {
            DISPATCHER.execute(message, src);
        } catch (CommandSyntaxException e) {
            ChatUtils.error("Syntax error: " + e.getMessage());
        } catch (Exception e) {
            ChatUtils.error("Command failed: " + e.getMessage());
        }
    }
}
