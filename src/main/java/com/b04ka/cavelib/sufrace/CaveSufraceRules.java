package com.b04ka.cavelib.sufrace;

import com.b04ka.cavelib.misc.CitadelSurfaceRulesManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

import java.util.LinkedHashMap;
import java.util.Map;

public class CaveSufraceRules {

    private static Map<ResourceKey<Biome>,SurfaceRules.RuleSource> ruleMap = new LinkedHashMap<>();

    public static void setup(){
        for (ResourceKey<Biome> biome: ruleMap.keySet()){
            CitadelSurfaceRulesManager.registerOverworldSurfaceRule(SurfaceRules.isBiome(biome), ruleMap.get(biome));
        }
    }

    public static void addRule(ResourceKey<Biome> biome, SurfaceRules.RuleSource rule){
        ruleMap.put(biome, rule);
    }

    public static SurfaceRules.RuleSource bedrock() {
        SurfaceRules.RuleSource bedrock = SurfaceRules.state(Blocks.BEDROCK.defaultBlockState());
        SurfaceRules.ConditionSource bedrockCondition = SurfaceRules.verticalGradient("bedrock", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5));
        return SurfaceRules.ifTrue(bedrockCondition, bedrock);
    }
}
