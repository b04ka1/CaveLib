package com.b04ka.cavelib;

import com.b04ka.cavelib.biome.CaveBiomeVisulals;
import com.b04ka.cavelib.deprecated.BiomeGenerationConfig;
import com.b04ka.cavelib.deprecated.ExpandedBiomes;
import com.b04ka.cavelib.structure.AbstractCaveGenerationStructure;
import com.b04ka.cavelib.structure.piece.LakeStructurePiece;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TestBiome {
    public static final ResourceKey<Biome> TEST = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("primordial_caves", CaveLib.MODID));

    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTER = DeferredRegister.create(Registries.STRUCTURE_TYPE, CaveLib.MODID);

    public static final Supplier<StructureType<Lake>> LAKE = DEFERRED_REGISTER.register("lake",()->()-> Lake.CODEC);

    public static void init(){
        CaveBiomeVisulals.Builder b= new CaveBiomeVisulals.Builder();
        b.setBiome(TEST).setAmbientLight(0.01F).build();
        ExpandedBiomes.addExpandedBiome(TEST, LevelStem.OVERWORLD);
        BiomeGenerationConfig.addBiome(TEST, BiomeGenerationConfig.MAGNETIC_CAVES_CONDITION);
    }

    public static class Lake extends AbstractCaveGenerationStructure {

        public static final MapCodec<Lake> CODEC = simpleCodec((settings) -> new Lake(settings));

        protected Lake(StructureSettings settings) {
            super(settings, TEST);
        }

        @Override
        protected StructurePiece createPiece(BlockPos offset, BlockPos center, int heightBlocks, int widthBlocks, RandomState randomState) {
            return new LakeStructurePiece(offset, center, heightBlocks, widthBlocks, TEST, Blocks.ACACIA_PLANKS, Blocks.WATER, Fluids.WATER);
        }

        @Override
        public int getGenerateYHeight(WorldgenRandom random, int x, int y) {
            return -10;
        }

        @Override
        public int getWidthRadius(WorldgenRandom random) {
            return 100;
        }

        @Override
        public int getHeightRadius(WorldgenRandom random, int seaLevel) {
            return 80;
        }

        @Override
        public StructureType<?> type() {
            return null;
        }
    }
}
