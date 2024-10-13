package com.b04ka.cavelib.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforgespi.Environment;

import java.util.function.Supplier;

public class CLUtils {
    public static Block getBlockFromId(String name){
       return BuiltInRegistries.BLOCK.get(ResourceLocation.bySeparator(name, ':'));
    }

    public static Fluid getFluidFromId(String name){
        return BuiltInRegistries.FLUID.get(ResourceLocation.bySeparator(name, ':'));
    }

    public static <T> T forgeUnsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        switch (Environment.get().getDist()) {
            case CLIENT -> {
                return clientTarget.get().get();
            }
            case DEDICATED_SERVER -> {
                return serverTarget.get().get();
            }
            default -> throw new IllegalArgumentException("UNSIDED?");
        }
    }

}
