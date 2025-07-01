package me.xoq.cortex.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.xoq.cortex.CortexClient;
import me.xoq.cortex.module.Modules;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve(CortexClient.MOD_ID + ".json");

    public static void load() {
        JsonObject root;

        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            String jsonString = Files.readString(CONFIG_PATH);
            root = JsonParser.parseString(jsonString).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config", e);
        }

        Modules.fromJson(root);
    }

    public static void save() {
        JsonObject root = Modules.toJson();
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config", e);
        }
    }
}
