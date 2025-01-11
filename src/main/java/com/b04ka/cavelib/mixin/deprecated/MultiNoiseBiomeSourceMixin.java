package com.b04ka.cavelib.mixin.deprecated;

import com.b04ka.cavelib.deprecated.*;
import com.b04ka.cavelib.misc.VoronoiGenerator;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = MultiNoiseBiomeSource.class, priority = -69420)
public class MultiNoiseBiomeSourceMixin implements MultiNoiseBiomeSourceAccessor {

    private long lastSampledWorldSeed;

    private ResourceKey<Level> lastSampledDimension;

    @Inject(at = @At("HEAD"),
            method = "Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource;getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            cancellable = true
    )
    private void cl_getNoiseBiomeCoords(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        for (Map.Entry<ResourceKey<Biome>, BiomeGenerationNoiseCondition> entry : BiomeGenerationConfig.BIOMES.entrySet()) {
            BiomeGenerationNoiseCondition condition = entry.getValue();
            double separation = condition.getSeparationDistance();
            VoronoiGenerator.VoronoiInfo voronoiInfo = BiomeRarity.getRareBiomeInfoForQuad(
                    condition.getOffsetAmount(),
                    condition.getBiomeSize(),
                    separation,
                    lastSampledWorldSeed,
                    x,
                    z
            );
            if (voronoiInfo != null) {
                float unquantizedDepth = Climate.unquantizeCoord(sampler.sample(x, y, z).depth());
                int offsetId = BiomeRarity.getRareBiomeOffsetId(voronoiInfo);
                if (offsetId == condition.getRarityOffset() && condition.test(x, y, z, unquantizedDepth, sampler, lastSampledDimension, voronoiInfo,separation )) {
                    cir.setReturnValue(((BiomeSourceAccessor) this).getResourceKeyMap().get(entry.getKey()));
                }
            }
        }
    }

    @Override
    public void setLastSampledSeed(long seed) {
        lastSampledWorldSeed = seed;
    }

    @Override
    public void setLastSampledDimension(ResourceKey<Level> dimension) {
        lastSampledDimension = dimension;
    }
}
