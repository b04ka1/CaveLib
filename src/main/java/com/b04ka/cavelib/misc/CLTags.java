package com.b04ka.cavelib.misc;

import com.b04ka.cavelib.CaveLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class CLTags {
    public static final TagKey<Biome> HAS_NO_ANCIENT_CITIES_AND_TRIAL_CHAMBERS_IN = registerBiomeTag("has_no_ancient_cities_and_trial_chambers_in");

    private static TagKey<Biome> registerBiomeTag(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(CaveLib.MODID, name));
    }
}
