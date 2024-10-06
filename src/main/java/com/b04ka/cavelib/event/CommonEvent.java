package com.b04ka.cavelib.event;

import com.b04ka.cavelib.deprecated.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommonEvent {

    @SubscribeEvent
    @Deprecated(forRemoval = true, since = "1.21")
    public void onReplaceBiome(EventReplaceBiome event) {
        ResourceKey<Biome> biome = BiomeGenerationConfig.getBiomeForEvent(event);
        if (biome != null) {
            Holder<Biome> biomeHolder = event.getBiomeSource().getResourceKeyMap().get(biome);
            if (biomeHolder != null) {
                event.setBiomeToGenerate(biomeHolder);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        BiomeRarity.init();
        //moved from citadel
        RegistryAccess registryAccess = event.getServer().registryAccess();
        Registry<Biome> allBiomes = registryAccess.registryOrThrow(Registries.BIOME);
        Registry<LevelStem> levelStems = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
        Map<ResourceKey<Biome>, Holder<Biome>> biomeMap = new HashMap<>();
        for (ResourceKey<Biome> biomeResourceKey : allBiomes.registryKeySet()) {
            Optional<Holder.Reference<Biome>> holderOptional = allBiomes.getHolder(biomeResourceKey);
            holderOptional.ifPresent(biomeHolder -> biomeMap.put(biomeResourceKey, biomeHolder));
        }
        for (ResourceKey<LevelStem> levelStemResourceKey : levelStems.registryKeySet()) {
            Optional<Holder.Reference<LevelStem>> holderOptional = levelStems.getHolder(levelStemResourceKey);
            if (holderOptional.isPresent() && holderOptional.get().value().generator().getBiomeSource() instanceof BiomeSourceAccessor expandedBiomeSource) {
                expandedBiomeSource.setResourceKeyMap(biomeMap);
                if (levelStemResourceKey.equals(LevelStem.OVERWORLD)) {
                    ImmutableSet.Builder<Holder<Biome>> biomeHolders = ImmutableSet.builder();
                    List<ResourceKey<Biome>> allBiomes1 = ExpandedBiomes.biomes.values()
                            .stream()
                            .flatMap(List::stream)
                            .toList();
                    for (ResourceKey<Biome> biomeResourceKey : allBiomes1) {
                        allBiomes.getHolder(biomeResourceKey).ifPresent(biomeHolders::add);
                    }
                    expandedBiomeSource.expandBiomesWith(biomeHolders.build());
                }
            }
        }
    }

}
