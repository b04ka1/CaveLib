package com.b04ka.cavelib.deprecated;

import com.b04ka.cavelib.CaveLib;
import com.b04ka.cavelib.misc.VoronoiGenerator;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class BiomeGenerationConfig {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    public static final LinkedHashMap<ResourceKey<Biome>, BiomeGenerationNoiseCondition> BIOMES = new LinkedHashMap<>();

    public static void addBiome(ResourceKey<Biome> biome, BiomeGenerationNoiseCondition condition) {
        BIOMES.put(biome, getConfigData(biome.location().toLanguageKey(), condition));
    }


    @Nullable
    public static ResourceKey<Biome> getBiomeForEvent(EventReplaceBiome event) {
        long worldSeed = event.getWorldSeed();
        int x = event.getX();
        int z = event.getZ();
        for (Map.Entry<ResourceKey<Biome>, BiomeGenerationNoiseCondition> entry : BIOMES.entrySet()) {
            BiomeGenerationNoiseCondition condition = entry.getValue();
            double separation = condition.getSeparationDistance();
            VoronoiGenerator.VoronoiInfo voronoiInfo = BiomeRarity.getRareBiomeInfoForQuad(
                    condition.getOffsetAmount(),
                    condition.getBiomeSize(),
                    separation,
                    worldSeed,
                    x,
                    z
            );
            if (voronoiInfo != null) {
                int offsetId = BiomeRarity.getRareBiomeOffsetId(voronoiInfo);
                if (offsetId == condition.getRarityOffset() && condition.test(event, voronoiInfo, separation)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public static int getBiomeCount() {
        return BIOMES.size();
    }

    public static boolean isBiomeDisabledCompletely(ResourceKey<Biome> biome) {
        BiomeGenerationNoiseCondition noiseCondition = BIOMES.get(biome);
        return noiseCondition != null && noiseCondition.isDisabledCompletely();
    }

    private static <T> T getOrCreateConfigFile(File configDir, String configName, T defaults, Type type, Predicate<T> isInvalid) {
        File configFile = new File(configDir, configName + ".json");
        if (!configFile.exists()) {
            try {
                FileUtils.write(configFile, GSON.toJson(defaults));
            } catch (IOException e) {
                CaveLib.LOGGER.error("Biome Generation Config: Could not write " + configFile, e);
            }
        }
        try {
            T found = GSON.fromJson(FileUtils.readFileToString(configFile), type);
            if (isInvalid.test(found)) {
                try {
                    FileUtils.write(configFile, GSON.toJson(defaults));
                } catch (IOException e) {
                    CaveLib.LOGGER.error("Biome Generation Config: Could not write " + configFile, e);
                }
            } else {
                return found;
            }
        } catch (Exception e) {
            CaveLib.LOGGER.error("Biome Generation Config: Could not load " + configFile, e);
        }

        return defaults;
    }

    private static File getConfigDirectory() {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path jsonPath = Paths.get(configPath.toAbsolutePath().toString(), "cavelib_biome_generation");
        return jsonPath.toFile();
    }

    private static BiomeGenerationNoiseCondition getConfigData(String fileName, BiomeGenerationNoiseCondition defaultConfigData) {
        BiomeGenerationNoiseCondition configData = getOrCreateConfigFile(getConfigDirectory(), fileName, defaultConfigData, new TypeToken<BiomeGenerationNoiseCondition>() {
        }.getType(), BiomeGenerationNoiseCondition::isInvalid);
        return configData;
    }
}
