package me.xoq.cortex;

import me.xoq.cortex.command.Commands;
import me.xoq.cortex.event.*;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.util.Config;
import me.xoq.cortex.util.Utils;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CortexClient implements ClientModInitializer {
	public static final String MOD_ID = "cortex";
	public static final ModMetadata MOD_META;
	public static MinecraftClient mc;
	public static final Logger LOG;

	private static CortexClient INSTANCE;

	static {
        ModContainer container = FabricLoader.getInstance()
				.getModContainer(MOD_ID)
				.orElseThrow(() -> new IllegalStateException("Mod container not found for " + MOD_ID));
		MOD_META = container.getMetadata();
		LOG = LoggerFactory.getLogger(MOD_META.getName());
	}

	@Override
	public void onInitializeClient() {
		// Prevent double-initialization
		if (INSTANCE != null) {
			LOG.warn("{} tried to initialize twice - ignoring.", MOD_ID);
			return;
		}
		INSTANCE = this;

		mc = MinecraftClient.getInstance();

		LOG.info("Initializing {} v{}", MOD_META.getName(), MOD_META.getVersion().getFriendlyString());

		// Initialize subsystems
		Modules.init();
		Commands.init();
		Config.load();

		// EventBus.register(this);

		// Save config on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(Config::save));
	}
}
