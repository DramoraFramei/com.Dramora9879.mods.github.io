package com.Dramora9879.mods.infinite_ocean_generator;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import org.slf4j.Logger;

import com.Dramora9879.mods.infinite_ocean_generator.worldgen.InfiniteOceanBiomeSource;
import com.Dramora9879.mods.infinite_ocean_generator.worldgen.InfiniteOceanChunkGenerator;
import com.Dramora9879.mods.infinite_ocean_generator.registry.datagen.InfiniteOceanGeneratorCreativeModeTabs;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(InfiniteOceanGenerator.MODID)
@IncludeLangDefaults ({@LangDefault(key="tab.infinite_ocean_generator.base",value=InfiniteOceanGenerator.TAB_NAME)
@LangDefault(key = "tab.infinite_ocean_generator.items", value = InfiniteOceanGenerator.ITEMS_TAB_NAME)
})public class InfiniteOceanGenerator {
	public static final String MODID = "infinite_ocean_generator";
	public static final String MOD_NAME = "Infinite Ocean Generator";
	public static final String TAB_NAME = "Infinite Ocean Generator";
	public static final String ITEMS_TAB_NAME = "Infinite Ocean Generator Items";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
			);

	public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister
			.create(Registries.CHUNK_GENERATOR, MODID);
	public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister
			.create(Registries.BIOME_SOURCE, MODID);

	public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<InfiniteOceanChunkGenerator>> INFINITE_OCEAN_GENERATOR = CHUNK_GENERATORS
			.register("infinite_ocean", () -> InfiniteOceanChunkGenerator.CODEC);
	public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<InfiniteOceanBiomeSource>> INFINITE_OCEAN_BIOME_SOURCE = BIOME_SOURCES
			.register("ocean", () -> InfiniteOceanBiomeSource.CODEC);

	public InfiniteOceanGenerator(IEventBus modEventBus) {
		CHUNK_GENERATORS.register(modEventBus);
		BIOME_SOURCES.register(modEventBus);
		modEventBus.addListener(this::commonSetup);
		NeoForge.EVENT_BUS.register(this);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		LOGGER.info("Infinite Ocean Generator initialized");
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		ServerLevel overworld = event.getServer().overworld();
		if (overworld.getChunkSource().getGenerator() instanceof InfiniteOceanChunkGenerator generator) {
			overworld.setDefaultSpawnPos(new BlockPos(0, generator.getSeaLevel(), 0), 0.0F);
			LOGGER.info("Overworld spawn set to the Infinite Ocean raft at 0, {}, 0", generator.getSeaLevel());
		}
	}
}
