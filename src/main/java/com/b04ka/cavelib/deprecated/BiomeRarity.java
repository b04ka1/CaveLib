package com.b04ka.cavelib.deprecated;

import com.b04ka.cavelib.misc.VoronoiGenerator;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class BiomeRarity {
    private static long lastTestedSeed = 0;
    private static final List<Integer> BIOME_OCTAVES = ImmutableList.of(0);
    private static final PerlinSimplexNoise NOISE_X = new PerlinSimplexNoise(new XoroshiroRandomSource(1234L), BIOME_OCTAVES);
    private static final PerlinSimplexNoise NOISE_Z = new PerlinSimplexNoise(new XoroshiroRandomSource(4321L), BIOME_OCTAVES);
    private static final VoronoiGenerator VORONOI_GENERATOR = new VoronoiGenerator(42L);
    /**
     * Does the heavy lifting of finding if x and z quads should contain a rare biome.
     *
     * @param worldSeed seed of the world for Voroni generation
     * @param x         xQuad being tested
     * @param z         zQuad being tested
     * @return the voroni info if a rare biome is present
     */
    @Nullable
    public static VoronoiGenerator.VoronoiInfo getRareBiomeInfoForQuad(double offsetAmount, double biomeSize, double seperationDistance,long worldSeed, int x, int z) {
        //start of code to initialize noise for world
        VORONOI_GENERATOR.setOffsetAmount(offsetAmount);
        VORONOI_GENERATOR.setSeed(worldSeed);
        double sampleX = x / seperationDistance;
        double sampleZ = z / seperationDistance;
        double positionOffsetX = 0.15D * NOISE_X.getValue(sampleX, sampleZ, false);
        double positionOffsetZ = 0.15D * NOISE_Z.getValue(sampleX, sampleZ, false);
        VoronoiGenerator.VoronoiInfo info = VORONOI_GENERATOR.get2(sampleX + positionOffsetX, sampleZ + positionOffsetZ);
        if (info.distance() < (biomeSize / seperationDistance)) {
            return info;
        } else {
            return null;
        }
    }

    /**
     * gets the center of the biome pertaining to the voronoiInfo. Result is in quad coordinates.
     *
     * @param voronoiInfo the info of the sampled biome
     * @return a coordinate
     */
    @Nullable
    public static Vec3 getRareBiomeCenter(VoronoiGenerator.VoronoiInfo voronoiInfo, double seperationDistance) {
        return voronoiInfo.cellPos().scale(seperationDistance);
    }

    /**
     * gets the 'rarityOffset' for voroniInfo, which is essentially an internal biome ID.
     *
     * @param voronoiInfo the info of the sampled biome
     * @return the rarityOffset of the info
     */
    @Nullable
    public static int getRareBiomeOffsetId(VoronoiGenerator.VoronoiInfo voronoiInfo) {
        return (int) (((voronoiInfo.hash() + 1D) * 0.5D) * (double) BiomeGenerationConfig.getBiomeCount());
    }

//    public static boolean isQuartInRareBiome(long worldSeed, int x, int z) {
//        return BiomeRarity.getRareBiomeInfoForQuad(worldSeed, x, z) != null;
//    }
}
