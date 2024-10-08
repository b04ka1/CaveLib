package com.b04ka.cavelib.misc;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

public class CitadelSurfaceRulesManager {
    private static final List<SurfaceRules.RuleSource> OVERWORLD_REGISTRY = new ArrayList();
    private static final List<SurfaceRules.RuleSource> NETHER_REGISTRY = new ArrayList();
    private static final List<SurfaceRules.RuleSource> END_REGISTRY = new ArrayList();
    private static final List<SurfaceRules.RuleSource> CAVE_REGISTRY = new ArrayList();

    public CitadelSurfaceRulesManager() {
    }

    public static void registerOverworldSurfaceRule(SurfaceRules.ConditionSource condition, SurfaceRules.RuleSource rule) {
        registerOverworldSurfaceRule(SurfaceRules.ifTrue(condition, rule));
    }

    public static void registerOverworldSurfaceRule(SurfaceRules.RuleSource rule) {
        OVERWORLD_REGISTRY.add(rule);
    }

    public static void registerNetherSurfaceRule(SurfaceRules.ConditionSource condition, SurfaceRules.RuleSource rule) {
        registerNetherSurfaceRule(SurfaceRules.ifTrue(condition, rule));
    }

    public static void registerNetherSurfaceRule(SurfaceRules.RuleSource rule) {
        NETHER_REGISTRY.add(rule);
    }

    public static void registerEndSurfaceRule(SurfaceRules.ConditionSource condition, SurfaceRules.RuleSource rule) {
        registerEndSurfaceRule(SurfaceRules.ifTrue(condition, rule));
    }

    public static void registerEndSurfaceRule(SurfaceRules.RuleSource rule) {
        END_REGISTRY.add(rule);
    }

    public static void registerCaveSurfaceRule(SurfaceRules.ConditionSource condition, SurfaceRules.RuleSource rule) {
        registerCaveSurfaceRule(SurfaceRules.ifTrue(condition, rule));
    }

    public static void registerCaveSurfaceRule(SurfaceRules.RuleSource rule) {
        CAVE_REGISTRY.add(rule);
    }

    public static SurfaceRules.RuleSource mergeRules(SurfaceRules.RuleSource prev, List<SurfaceRules.RuleSource> toMerge) {
        ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();
        builder.addAll(toMerge);
        builder.add(prev);
        return SurfaceRules.sequence(builder.build().toArray((size) -> new SurfaceRules.RuleSource[size]));
    }

    public static SurfaceRules.RuleSource mergeOverworldRules(SurfaceRules.RuleSource rulesIn) {
        return mergeRules(rulesIn, OVERWORLD_REGISTRY);
    }
}