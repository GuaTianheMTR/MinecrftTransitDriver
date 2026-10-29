package com.thrt.mtrdriver;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MTRDriver implements ModInitializer {
	public static final String MOD_ID = "mtrdriver";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("MinecraftTransitDriver | MTR driving controls loaded (THRT)");
	}
}
