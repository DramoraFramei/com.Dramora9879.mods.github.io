package com.Dramora9879.mods.infinite_ocean_generator;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = InfiniteOceanGenerator.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = InfiniteOceanGenerator.MODID, value = Dist.CLIENT)
public class InfiniteOceanGeneratorClient {
	public InfiniteOceanGeneratorClient() {
	}

	@SubscribeEvent
	static void onClientSetup(FMLClientSetupEvent event) {
		InfiniteOceanGenerator.LOGGER.info("Infinite Ocean Generator client initialized");
	}
}
