package com.Dramora9879.mods.infinite_ocean_generator.mixin.presets;

import com.Dramora9879.mods.infinite_ocean_generator.InfiniteOceanGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGenerators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerators.class)
public class ChunkGeneratorsMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(final Registry<MapCodec<? extends ChunkGenerator>> registry, final  CallbackInfoReturnable<MapCodec<? extends ChunkGenerator>> cir) {
        Registry.register(registry, InfiniteOceanGenerator.asResource)
    }
}
