package me.xoq.cortex;

import me.xoq.cortex.event.*;
import me.xoq.cortex.module.Modules;
import me.xoq.cortex.util.Config;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CortexClient implements ClientModInitializer {
	public static final String MOD_ID = "cortex";
	public static final ModMetadata MOD_META;

	private static CortexClient INSTANCE;

	public static MinecraftClient mc;
	public static final Logger LOG;

	static {
		MOD_META = FabricLoader.getInstance()
				.getModContainer(MOD_ID)
				.orElseThrow(() -> new IllegalStateException("Mod container not found for " + MOD_ID))
				.getMetadata();

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

		Modules.init();
		Config.load();

		EventBus.register(this);

		Runtime.getRuntime()
				.addShutdownHook(new Thread(Config::save));
	}
}
