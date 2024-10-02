package com.b04ka.cavelib.utils;

import com.b04ka.cavelib.CaveLib;
import com.b04ka.cavelib.biome.CaveBiomeVisulals;
import com.b04ka.cavelib.deprecated.BiomeGenerationConfig;
import com.b04ka.cavelib.deprecated.ExpandedBiomes;
import com.b04ka.cavelib.structure.AbstractCaveGenerationStructure;
import com.b04ka.cavelib.structure.piece.CanyonStructurePiece;
import com.b04ka.cavelib.sufrace.CaveSufraceRules;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TestBiome {
    public static final ResourceKey<Biome> TEST2 = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(CaveLib.MODID, "test2"));

    private static final Vec3 TOXIC_CAVES_LIGHT_COLOR = new Vec3(0.5, 1.5, 0.5);

    public static List<ResourceKey<Biome>> testBiomes = new ArrayList<>();

    public static SurfaceRules.RuleSource createToxicCavesRules() {
        SurfaceRules.RuleSource radrock = SurfaceRules.state(Blocks.SANDSTONE.defaultBlockState());
        return SurfaceRules.sequence(CaveSufraceRules.bedrock(), radrock);
    }

    public static void init(){
        CaveBiomeVisulals.Builder builder = new CaveBiomeVisulals.Builder();
        builder.setBiome(TEST2).setAmbientLight(0.1F).setSkyOverride(1F).setFogNearness(0.5F)
                        .setLightColorOverride(TOXIC_CAVES_LIGHT_COLOR)
                .build();

        ExpandedBiomes.addExpandedBiome(TEST2, LevelStem.OVERWORLD);

        CaveSufraceRules.addRule(TEST2, createToxicCavesRules());

        BiomeGenerationConfig.addBiome(TEST2, BiomeGenerationConfig.PRIMORDIAL_CAVES_CONDITION);

        testBiomes.add(TEST2);

    }

    public static class DinoBowlStructure extends AbstractCaveGenerationStructure {

        private static final int BOWL_WIDTH_RADIUS = 100;
        private static final int BOWL_HEIGHT_RADIUS = 80;

        public static final int BOWL_Y_CENTER = -1;

        public static final MapCodec<DinoBowlStructure> CODEC = simpleCodec((settings) -> new DinoBowlStructure(settings));

        public DinoBowlStructure(StructureSettings settings) {
            super(settings, TestBiome.TEST2);
        }

        @Override
        protected StructurePiece createPiece(BlockPos offset, BlockPos center, int heightBlocks, int widthBlocks, RandomState randomState) {
            return new CanyonStructurePiece(offset, center, heightBlocks, widthBlocks, Blocks.ACACIA_LOG);
        }

        @Override
        public int getGenerateYHeight(WorldgenRandom random, int x, int y) {
            return BOWL_Y_CENTER;
        }

        @Override
        public int getWidthRadius(WorldgenRandom random) {
            return BOWL_WIDTH_RADIUS;
        }

        @Override
        public int getHeightRadius(WorldgenRandom random, int seaLevel) {
            return BOWL_HEIGHT_RADIUS;
        }

        @Override
        public StructureType<?> type() {
            return BOWL.get();
        }
    }

    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTER = DeferredRegister.create(Registries.STRUCTURE_TYPE, CaveLib.MODID);

    public static final Supplier<StructureType<DinoBowlStructure>> BOWL = DEFERRED_REGISTER.register("bowl", ()-> ()-> DinoBowlStructure.CODEC);

}
