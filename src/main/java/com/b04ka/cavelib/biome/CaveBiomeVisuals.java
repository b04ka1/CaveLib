package com.b04ka.cavelib.biome;


import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CaveBiomeVisuals {
    protected record BiomeProperty
            (ResourceKey<Biome> biome, float ambientLight, float fogNearness, float waterFogFarness, float skyOverride,
             Vec3 lightColorOverride) {
    }

    protected static List<BiomeProperty> caveBiomesMap = List.of();
    private static final Vec3 DEFAULT_LIGHT_COLOR = new Vec3(1, 1, 1);

    public static float getBiomeAmbientLight(Holder<Biome> value) {
        for (BiomeProperty biomeProperty : caveBiomesMap) {
            if (value.is(biomeProperty.biome)) {
                return biomeProperty.ambientLight;
            }
        }
        return 0.0F;
    }

    public static float getBiomeFogNearness(Holder<Biome> value) {
        for (BiomeProperty biomeProperty : caveBiomesMap) {
            if (value.is(biomeProperty.biome)) {
                return biomeProperty.fogNearness;
            }
        }
        return 1.0F;
    }

    public static float getBiomeWaterFogFarness(Holder<Biome> value) {
        for (BiomeProperty biomeProperty : caveBiomesMap) {
            if (value.is(biomeProperty.biome)) {
                return biomeProperty.waterFogFarness;
            }
        }
        return 1.0F;
    }

    public static float getBiomeSkyOverride(Holder<Biome> value) {
        for (BiomeProperty biomeProperty : caveBiomesMap) {
            if (value.is(biomeProperty.biome)) {
                return biomeProperty.skyOverride;
            }
        }
        return 0.0F;
    }

    public static Vec3 getBiomeLightColorOverride(Holder<Biome> value) {
        for (BiomeProperty biomeProperty : caveBiomesMap) {
            if (value.is(biomeProperty.biome)) {
                return biomeProperty.lightColorOverride;
            }
        }
        return DEFAULT_LIGHT_COLOR;
    }

    public static float calculateBiomeSkyOverride(Entity player) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        if (i == 0) {
            return CaveBiomeVisuals.getBiomeSkyOverride(player.level().getBiome(player.blockPosition()));
        } else {
            return BiomeSampler.sampleBiomesFloat(player.level(), player.position(), CaveBiomeVisuals::getBiomeSkyOverride);
        }
    }

    public Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private ResourceKey<Biome> biome;
        private float ambientLight = 0.0F;
        private float fogNearness = 1.0F;
        private float waterFogFarness = 1.0F;
        private float skyOverride = 0.0F;
        private Vec3 lightColorOverride = DEFAULT_LIGHT_COLOR;

        private Builder() {
        }

        public Builder setBiome(ResourceKey<Biome> biome) {
            this.biome = biome;
            return this;
        }

        public Builder setAmbientLight(float ambientLight) {
            this.ambientLight = ambientLight;
            return this;
        }

        public Builder setFogNearness(float fogNearness) {
            this.fogNearness = fogNearness;
            return this;
        }

        public Builder setWaterFogFarness(float waterFogFarness) {
            this.waterFogFarness = waterFogFarness;
            return this;
        }

        public Builder setSkyOverride(float skyOverride) {
            this.skyOverride = skyOverride;
            return this;
        }

        public Builder setLightColorOverride(Vec3 lightColorOverride) {
            this.lightColorOverride = lightColorOverride;
            return this;
        }

        public Builder setBiome(ResourceKey<Biome> biome, boolean condition) {
            if (condition) {
                this.biome = biome;
            }
            return this;
        }

        public Builder setAmbientLight(float ambientLight, boolean condition) {
            if (condition) {
                this.ambientLight = ambientLight;
            }
            return this;
        }

        public Builder setFogNearness(float fogNearness, boolean condition) {
            if (condition) {
                this.fogNearness = fogNearness;
            }
            return this;
        }

        public Builder setWaterFogFarness(float waterFogFarness, boolean condition) {
            if (condition) {
                this.waterFogFarness = waterFogFarness;
            }
            return this;
        }

        public Builder setSkyOverride(float skyOverride, boolean condition) {
            if (condition) {
                this.skyOverride = skyOverride;
            }
            return this;
        }

        public Builder setLightColorOverride(Vec3 lightColorOverride, boolean condition) {
            if (condition) {
                this.lightColorOverride = lightColorOverride;
            }
            return this;
        }

        public void build() {
            caveBiomesMap.add(new BiomeProperty(biome, ambientLight, fogNearness, waterFogFarness, skyOverride, lightColorOverride));
        }
    }

}
