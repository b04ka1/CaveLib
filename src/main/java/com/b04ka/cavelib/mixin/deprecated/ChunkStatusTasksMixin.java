package com.b04ka.cavelib.mixin.deprecated;

import com.b04ka.cavelib.deprecated.MultiNoiseBiomeSourceAccessor;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {

    @Inject(at = @At("HEAD"),
            method = "Lnet/minecraft/world/level/chunk/status/ChunkStatusTasks;generateNoise")
    private static void cl_fillFromNoise
            (WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        if(pWorldGenContext.generator().getBiomeSource() instanceof MultiNoiseBiomeSourceAccessor multiNoiseBiomeSourceAccessor){
            multiNoiseBiomeSourceAccessor.setLastSampledSeed(pWorldGenContext.level().getSeed());
            multiNoiseBiomeSourceAccessor.setLastSampledDimension(pWorldGenContext.level().dimension());
        }
    }
}
