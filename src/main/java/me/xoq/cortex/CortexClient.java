package me.xoq.cortex;

import me.xoq.cortex.event.*;
import me.xoq.cortex.utils.ChatUtils;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
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

		EventBus.register(this);
	}

	@EventListener
	private void eventListener(EntityAttackEvent event) {
        if (event.getTarget() instanceof VillagerEntity) {
			event.cancel();
			ChatUtils.warn("Prevented attack.");
		} else {
			ChatUtils.info("Attacked entity" + event.getTarget().getType().getName().getString());
		}
	}
}