package com.b04ka.cavelib.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class CLUtils {
    public static Block getBlockFromId(String name){
       return BuiltInRegistries.BLOCK.get(ResourceLocation.bySeparator(name, ':'));
    }

}
