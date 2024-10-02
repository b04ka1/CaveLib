package com.b04ka.cavelib.proxy;

import com.b04ka.cavelib.event.ClientEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public class ClientProxy extends CommonProxy{
    public static float lastBiomeAmbientLightAmountPrev = 0;
    public static float lastBiomeAmbientLightAmount = 0;
    public static Vec3 lastBiomeLightColorPrev = Vec3.ZERO;
    public static Vec3 lastBiomeLightColor = Vec3.ZERO;
    public static float clSkyOverrideAmount;
    public static Vec3 clSkyOverrideColor = Vec3.ZERO;


    @Override
    public void clientInit() {
        NeoForge.EVENT_BUS.register(new ClientEvent());
    }
}
