package com.b04ka.cavelib.deprecated;

import com.b04ka.cavelib.misc.VoronoiGenerator;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BiomeGenerationNoiseCondition {
    private boolean disabledCompletely;

    public double getOffsetAmount() {
        return offsetAmount;
    }

    public double getBiomeSize() {
        return biomeSize;
    }

    public double getSeparationDistance() {
        return separationDistance;
    }

    private double offsetAmount;
    private double biomeSize;
    private double separationDistance;
    private int distanceFromSpawn;
    private final int alexscavesRarityOffset;
    private final float[] continentalness;
    private final float[] erosion;
    private final float[] humidity;
    private final float[] temperature;
    private final float[] weirdness;
    private final float[] depth;
    private final List<String> dimensions;

    private BiomeGenerationNoiseCondition(boolean disabledCompletely, double offsetAmount, double biomeSize, double separationDistance, int distanceFromSpawn, int alexscavesRarityOffset, float[] continentalness, float[] erosion, float[] humidity, float[] temperature, float[] weirdness, float[] depth, String[] dimensions) {
        this.disabledCompletely = disabledCompletely;
        this.offsetAmount = offsetAmount;
        this.biomeSize = biomeSize;
        this.separationDistance = separationDistance;
        this.distanceFromSpawn = distanceFromSpawn;
        this.continentalness = continentalness;
        this.erosion = erosion;
        this.humidity = humidity;
        this.temperature = temperature;
        this.weirdness = weirdness;
        this.depth = depth;
        this.alexscavesRarityOffset = alexscavesRarityOffset;
        this.dimensions = List.of(dimensions);
    }

    public boolean test(EventReplaceBiome event, VoronoiGenerator.VoronoiInfo info, double separationDistance) {
        if (disabledCompletely) {
            return false;
        }
        Vec3 rareBiomeCenter = BiomeRarity.getRareBiomeCenter(info, separationDistance);
        if (rareBiomeCenter == null) {
            return false;
        }
        Climate.TargetPoint centerTargetPoint = event.getClimateSampler().sample((int) Math.floor(rareBiomeCenter.x), event.getY(), (int) Math.floor(rareBiomeCenter.z));
        float c = Climate.unquantizeCoord(centerTargetPoint.continentalness());
        if (continentalness != null && continentalness.length >= 2 && (c < continentalness[0] || c > continentalness[1])) {
            return false;
        }
        float e = Climate.unquantizeCoord(centerTargetPoint.erosion());
        if (erosion != null && erosion.length >= 2 && (e < erosion[0] || e > erosion[1])) {
            return false;
        }
        float v = Climate.unquantizeCoord(centerTargetPoint.humidity());
        if (humidity != null && humidity.length >= 2 && (v < humidity[0] || v > humidity[1])) {
            return false;
        }
        float t = Climate.unquantizeCoord(centerTargetPoint.temperature());

        if (temperature != null && temperature.length >= 2 && (t < temperature[0] || t > temperature[1])) {
            return false;
        }
        float w = Climate.unquantizeCoord(centerTargetPoint.weirdness());
        if (weirdness != null && weirdness.length >= 2 && (w < weirdness[0] || w > weirdness[1])) {
            return false;
        }
        // sample depth per coord - we don't want biomes bleeding onto the surface
        if (depth != null && depth.length >= 2 && !event.testDepth(depth[0], depth[1])) {
            return false;
        }
        if (event.getWorldDimension() != null && !dimensions.contains(event.getWorldDimension().location().toString())) {
            return false;
        }
        if (!isFarEnoughFromSpawn(event, distanceFromSpawn)) {
            return false;
        }

        return true;
    }

    private static boolean isFarEnoughFromSpawn(EventReplaceBiome event, double dist) {
        int x = QuartPos.fromSection(event.getX());
        int z = QuartPos.toBlock(event.getZ());
        return x * x + z * z >= dist * dist;
    }

    public boolean test(int x, int y, int z, float unquantizedDepth, Climate.Sampler climateSampler, ResourceKey<Level> dimension, VoronoiGenerator.VoronoiInfo info, double separationDistance) {
        if (disabledCompletely) {
            return false;
        }
        Vec3 rareBiomeCenter = BiomeRarity.getRareBiomeCenter(info, separationDistance);
        if (rareBiomeCenter == null) {
            return false;
        }
        Climate.TargetPoint centerTargetPoint = climateSampler.sample((int) Math.floor(rareBiomeCenter.x), y, (int) Math.floor(rareBiomeCenter.z));
        float c = Climate.unquantizeCoord(centerTargetPoint.continentalness());
        if (continentalness != null && continentalness.length >= 2 && (c < continentalness[0] || c > continentalness[1])) {
            return false;
        }
        float e = Climate.unquantizeCoord(centerTargetPoint.erosion());
        if (erosion != null && erosion.length >= 2 && (e < erosion[0] || e > erosion[1])) {
            return false;
        }
        float v = Climate.unquantizeCoord(centerTargetPoint.humidity());
        if (humidity != null && humidity.length >= 2 && (v < humidity[0] || v > humidity[1])) {
            return false;
        }
        float t = Climate.unquantizeCoord(centerTargetPoint.temperature());
        if (temperature != null && temperature.length >= 2 && (t < temperature[0] || t > temperature[1])) {
            return false;
        }
        float w = Climate.unquantizeCoord(centerTargetPoint.weirdness());
        if (weirdness != null && weirdness.length >= 2 && (w < weirdness[0] || w > weirdness[1])) {
            return false;
        }
        // sample depth per coord - we don't want biomes bleeding onto the surface
        if (depth != null && depth.length >= 2 && (unquantizedDepth < depth[0] || unquantizedDepth > depth[1])) {
            return false;
        }
        if (dimension != null && !dimensions.contains(dimension.location().toString())) {
            return false;
        }
        if (!isFarEnoughFromSpawn(x, z, distanceFromSpawn)) {
            return false;
        }

        return true;
    }

    private static boolean isFarEnoughFromSpawn(int xIn, int zIn, double dist) {
        int x = QuartPos.fromSection(xIn);
        int z = QuartPos.toBlock(zIn);
        return x * x + z * z >= dist * dist;
    }


    public boolean isDisabledCompletely() {
        return disabledCompletely;
    }

    public boolean isInvalid() {
        return dimensions == null && !disabledCompletely;
    }

    public int getRarityOffset() {
        return alexscavesRarityOffset;
    }

    public static final class Builder {
        private boolean disabledCompletely;
        private double offsetAmount = 0.15D;
        private double biomeSize = 75D;
        private double separationDistance = this.biomeSize + 225D;
        private int distanceFromSpawn;
        private float[] continentalness;
        private float[] erosion;
        private float[] humidity;
        private float[] temperature;
        private float[] weirdness;
        private float[] depth;
        private String[] dimensions;
        private static int rarityOffset;

        public Builder() {
        }

        public Builder disabledCompletely(boolean disabledCompletely) {
            this.disabledCompletely = disabledCompletely;
            return this;
        }

        public Builder offsetAmount(double offsetAmount) {
            this.offsetAmount = offsetAmount;
            return this;
        }

        public Builder biomeSize(double biomeSize) {
            this.biomeSize = biomeSize * 0.25D;
            return this;
        }

        public Builder separationDistance(double separationDistance) {
            this.separationDistance = this.biomeSize + separationDistance * 0.25D;
            return this;
        }

        public Builder distanceFromSpawn(int distanceFromSpawn) {
            this.distanceFromSpawn = distanceFromSpawn;
            return this;
        }

        public Builder continentalness(float... continentalness) {
            this.continentalness = continentalness;
            return this;
        }

        public Builder erosion(float... erosion) {
            this.erosion = erosion;
            return this;
        }

        public Builder humidity(float... humidity) {
            this.humidity = humidity;
            return this;
        }

        public Builder temperature(float... temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder weirdness(float... weirdness) {
            this.weirdness = weirdness;
            return this;
        }

        public Builder depth(float... depth) {
            this.depth = depth;
            return this;
        }

        public Builder dimensions(String... dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public BiomeGenerationNoiseCondition build() {
            return new BiomeGenerationNoiseCondition(disabledCompletely, offsetAmount, biomeSize, separationDistance, distanceFromSpawn, rarityOffset++, continentalness, erosion, humidity, temperature, weirdness, depth, dimensions);
        }
    }
}
