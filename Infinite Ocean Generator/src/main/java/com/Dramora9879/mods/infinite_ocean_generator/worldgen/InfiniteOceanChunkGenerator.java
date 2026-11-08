package com.Dramora9879.mods.infinite_ocean_generator.worldgen;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class InfiniteOceanChunkGenerator extends ChunkGenerator {
	public static final MapCodec<InfiniteOceanChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(InfiniteOceanChunkGenerator::getBiomeSource),
					Codec.INT.optionalFieldOf("sea_level", 63).forGetter(InfiniteOceanChunkGenerator::getSeaLevel),
					Codec.INT.optionalFieldOf("seafloor_base", 40).forGetter(generator -> generator.seafloorBase),
					Codec.INT.optionalFieldOf("seafloor_variation", 8)
							.forGetter(generator -> generator.seafloorVariation))
			.apply(instance, InfiniteOceanChunkGenerator::new));

	private static final int MIN_Y = -64;
	private static final int GEN_DEPTH = 384;
	private static final int RAFT_RADIUS = 3;
	private static final BlockPos CHEST_POS = new BlockPos(2, 63, 0);

	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState WATER = Blocks.WATER.defaultBlockState();
	private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
	private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
	private static final BlockState STONE = Blocks.STONE.defaultBlockState();
	private static final BlockState SAND = Blocks.SAND.defaultBlockState();
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState CLAY = Blocks.CLAY.defaultBlockState();
	private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
	private static final BlockState PLANKS = Blocks.OAK_PLANKS.defaultBlockState();

	private final int seaLevel;
	private final int seafloorBase;
	private final int seafloorVariation;

	public InfiniteOceanChunkGenerator(BiomeSource biomeSource, int seaLevel, int seafloorBase, int seafloorVariation) {
		super(biomeSource);
		this.seaLevel = seaLevel;
		this.seafloorBase = seafloorBase;
		this.seafloorVariation = seafloorVariation;
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
			StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
	}

	@Override
	public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random,
			ChunkAccess chunk) {
		BlockPos chestPos = new BlockPos(CHEST_POS.getX(), this.seaLevel, CHEST_POS.getZ());
		if (chunk.getPos().equals(new ChunkPos(chestPos))) {
			level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
			RandomizableContainer.setBlockEntityLootTable(level, level.getRandom(), chestPos,
					BuiltInLootTables.SPAWN_BONUS_CHEST);
		}
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion level) {
	}

	@Override
	public int getGenDepth() {
		return GEN_DEPTH;
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
			StructureManager structureManager, ChunkAccess chunk) {
		Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minY = chunk.getMinBuildHeight();
		int maxY = chunk.getMaxBuildHeight();

		for (int localZ = 0; localZ < 16; localZ++) {
			for (int localX = 0; localX < 16; localX++) {
				int worldX = chunk.getPos().getMinBlockX() + localX;
				int worldZ = chunk.getPos().getMinBlockZ() + localZ;
				int seafloorY = this.getSeafloorY(worldX, worldZ);

				for (int y = minY; y < maxY; y++) {
					BlockState state = this.getBlockState(worldX, y, worldZ, seafloorY, minY);
					if (state.isAir()) {
						continue;
					}
					cursor.set(localX, y, localZ);
					chunk.setBlockState(cursor, state, false);
					oceanFloor.update(localX, y, localZ, state);
					worldSurface.update(localX, y, localZ, state);
				}
			}
		}

		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getSeaLevel() {
		return this.seaLevel;
	}

	@Override
	public int getMinY() {
		return MIN_Y;
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
		int seafloorY = this.getSeafloorY(x, z);
		for (int y = Math.min(level.getMaxBuildHeight(), this.seaLevel + 1) - 1; y >= level.getMinBuildHeight(); y--) {
			BlockState state = this.getBlockState(x, y, z, seafloorY, level.getMinBuildHeight());
			if (type.isOpaque().test(state)) {
				return y + 1;
			}
		}
		return level.getMinBuildHeight();
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
		int minY = height.getMinBuildHeight();
		int columnHeight = height.getHeight();
		BlockState[] column = new BlockState[columnHeight];
		int seafloorY = this.getSeafloorY(x, z);
		for (int i = 0; i < columnHeight; i++) {
			column[i] = this.getBlockState(x, minY + i, z, seafloorY, minY);
		}
		return new NoiseColumn(minY, column);
	}

	@Override
	public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
		info.add(
				"Infinite Ocean sea_level=" + this.seaLevel + " seafloor=" + this.getSeafloorY(pos.getX(), pos.getZ()));
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor level) {
		return this.seaLevel;
	}

	private int getSeafloorY(int x, int z) {
		double noise = OceanNoise.octaveNoise(x / 48.0, z / 48.0) * 2.0 - 1.0;
		return this.seafloorBase + (int) Math.round(noise * this.seafloorVariation);
	}

	private BlockState getBlockState(int x, int y, int z, int seafloorY, int minY) {
		if (y == this.seaLevel - 1 && isRaft(x, z)) {
			return PLANKS;
		}
		if (y > seafloorY && y < this.seaLevel) {
			return WATER;
		}
		if (y > seafloorY) {
			return AIR;
		}
		if (y <= minY) {
			return BEDROCK;
		}
		if (y < minY + 5 && OceanNoise.hash01(x * 31 + y, z) < (minY + 5 - y) / 5.0) {
			return BEDROCK;
		}

		int depth = seafloorY - y;
		if (depth == 0) {
			return surfaceBlock(x, z);
		}
		if (depth <= 3) {
			return OceanNoise.hash01(x, z + 19) < 0.45 ? GRAVEL : SAND;
		}
		if (y < 0) {
			return DEEPSLATE;
		}
		return STONE;
	}

	private static boolean isRaft(int x, int z) {
		return Math.abs(x) <= RAFT_RADIUS && Math.abs(z) <= RAFT_RADIUS;
	}

	private static BlockState surfaceBlock(int x, int z) {
		double value = OceanNoise.hash01(x, z);
		if (value < 0.12) {
			return CLAY;
		}
		if (value < 0.28) {
			return GRAVEL;
		}
		if (value < 0.36) {
			return DIRT;
		}
		return SAND;
	}
}
