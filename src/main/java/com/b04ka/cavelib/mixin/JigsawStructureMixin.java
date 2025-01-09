package com.b04ka.cavelib.mixin;

import com.b04ka.cavelib.misc.ACMath;
import com.b04ka.cavelib.misc.CLTags;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawStructure.class)
public class JigsawStructureMixin {

    @Shadow
    @Final
    private Optional<ResourceLocation> startJigsawName;

    @Shadow
    @Final
    private Holder<StructureTemplatePool> startPool;

    @Inject(
            method = {"Lnet/minecraft/world/level/levelgen/structure/structures/JigsawStructure;findGenerationPoint(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;)Ljava/util/Optional;"},
            remap = true,
            cancellable = true,
            at = @At(value = "HEAD")
    )
    private void cl_findGenerationPoint(Structure.GenerationContext context, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if ((this.startJigsawName.isPresent() && this.startJigsawName.get().toString().equals("minecraft:city_anchor")) || this.startPool.is(ResourceLocation.withDefaultNamespace("trial_chambers/chamber/end"))) {// limit to only ancient cities and trial chambers
            int i = context.chunkPos().getBlockX(9);
            int j = context.chunkPos().getBlockZ(9);

            for (Holder<Biome> holder : ACMath.getBiomesWithinAtY(context.biomeSource(), i, context.chunkGenerator().getSeaLevel() - 80, j, 80, context.randomState().sampler())) {
                if (holder.is(CLTags.HAS_NO_ANCIENT_CITIES_AND_TRIAL_CHAMBERS_IN)) {
                    cir.setReturnValue(Optional.empty());
                }
            }

        }
    }

}
