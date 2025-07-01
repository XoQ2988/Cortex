package me.xoq.cortex;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CortexClient implements ClientModInitializer {
	public static final String MOD_ID = "cortex";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitializeClient() {
		LOG.info("Hello, World!");
	}
}