package com.Dramora9879.mods.infinite_ocean_generator.worldgen;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class InfiniteOceanBiomeSource extends BiomeSource {
	public static final MapCodec<InfiniteOceanBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(
					Biome.CODEC.listOf().fieldOf("biomes").forGetter(source -> source.biomes))
			.apply(instance, InfiniteOceanBiomeSource::new));

	private final List<Holder<Biome>> biomes;

	public InfiniteOceanBiomeSource(List<Holder<Biome>> biomes) {
		if (biomes.isEmpty()) {
			throw new IllegalArgumentException("Infinite Ocean biome source requires at least one biome");
		}
		this.biomes = List.copyOf(biomes);
	}

	@Override
	protected MapCodec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.biomes.stream();
	}

	@Override
	public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
		double temperature = OceanNoise.octaveNoise(quartX / 96.0, quartZ / 96.0);
		int index = (int) Math.min(this.biomes.size() - 1, Math.floor(temperature * this.biomes.size()));
		return this.biomes.get(index);
	}
}
